package com.terradue.warhol;

/*
 *    Copyright 2011-2012 Terradue srl
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import static com.terradue.warhol.lang.Preconditions.checkArgument;
import static org.sonatype.spice.jersey.client.ahc.AhcHttpClient.create;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.ssl.KeyMaterial;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.resumable.ResumableIOExceptionFilter;
import com.sun.jersey.api.client.Client;
import com.terradue.warhol.settings.Authentication;
import com.terradue.warhol.settings.DataSource;
import com.terradue.warhol.settings.HttpAuthentication;
import com.terradue.warhol.settings.HttpSettings;
import com.terradue.warhol.settings.SslAuthentication;

final class RestClientFactory
{

    public static Client newRestClient( DataSource dataSource )
    {
        DefaultAhcConfig config = new DefaultAhcConfig();

        Builder builder = config.getAsyncHttpClientConfigBuilder();

        // basic http settings
        HttpSettings settings = dataSource.getHttpSettings();
        builder.setRequestTimeoutInMs( settings.getConnectionTimeout() * 60 * 60 * 1000 ) // 45 minutes
               .setAllowPoolingConnection( settings.isAllowPoolingConnection() )
               .addIOExceptionFilter( new ResumableIOExceptionFilter() )
               .setMaximumConnectionsPerHost( settings.getHostMaximumConnection() )
               .setMaximumConnectionsTotal( settings.getTotalMaximumConnection() )
               .setFollowRedirects( settings.isFollowRedirects() );

        // authentication
        Authentication authentication = dataSource.getAuthentication();
        if ( authentication instanceof HttpAuthentication )
        {
            builder.setRealm( buildHttpAuthentication( (HttpAuthentication) authentication ) );
        }

        // ==== SSL ====

        // private key
        KeyManager[] keyManagers;

        if ( authentication instanceof SslAuthentication )
        {
            keyManagers = initKeyManager( (SslAuthentication) authentication );
        }
        else
        {
            keyManagers = new KeyManager[] {};
        }

        TrustManager[] trustManagers;

        // server certificates
        if ( settings.isCheckCertificate() )
        {
            trustManagers = new TrustManager[] {};
        }
        else
        {
            trustManagers = new TrustManager[] { new RelaxedTrustManager() };
        }

        // SSL context
        SSLContext context = null;
        try
        {
            context = SSLContext.getInstance( "TLS" );
            context.init( keyManagers, trustManagers, null );
            builder.setSSLContext( context );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Impossible to initialize SSL context", e );
        }

        return create( config );
    }

    private static Realm buildHttpAuthentication( HttpAuthentication authentication )
    {
        final Realm.RealmBuilder realmBuilder = new Realm.RealmBuilder();

        switch ( authentication.getScheme() )
        {
            case BASIC:
                realmBuilder.setScheme( AuthScheme.BASIC );
                break;

            case DIGEST:
                realmBuilder.setScheme( AuthScheme.DIGEST );
                break;

            case KERBEROS:
                realmBuilder.setScheme( AuthScheme.KERBEROS );
                break;

            case NONE:
                realmBuilder.setScheme( AuthScheme.NONE );
                break;

            case NTLM:
                realmBuilder.setScheme( AuthScheme.NTLM );
                break;

            case SPNEGO:
                realmBuilder.setScheme( AuthScheme.SPNEGO );
                break;

            default:
                // do nothing
                break;
        }

        return realmBuilder.setPrincipal( authentication.getUsername() )
                            .setPassword( authentication.getPassword() )
                            .setUsePreemptiveAuth( authentication.isPreemptive() )
                            .build();
    }

    private static KeyManager[] initKeyManager( SslAuthentication sslAuthentication )
    {
        if ( sslAuthentication.getProxyCertificate() != null )
        {
            return fromSslProxy( sslAuthentication.getProxyCertificate() );
        }

        return fromSslKeyAndCertificate( sslAuthentication.getPublicCertificate(),
                                         sslAuthentication.getPrivateKey(),
                                         sslAuthentication.getPassword() );
    }

    private static KeyManager[] fromSslProxy( String proxyCertificateLocation )
    {
        return fromSslKeyAndCertificate( proxyCertificateLocation, proxyCertificateLocation, null );
    }

    private static KeyManager[] fromSslKeyAndCertificate( String publicCertificateLocation, String provateKeyLocation, String sslPassword )
    {
        File publicCertificate = checkFile( publicCertificateLocation );
        File privateKey = checkFile( provateKeyLocation );

        char[] password;
        if ( sslPassword != null )
        {
            password = sslPassword.toCharArray();
        }
        else
        {
            password = new char[] {};
        }

        try
        {
            final KeyStore store = new KeyMaterial( publicCertificate, privateKey, password ).getKeyStore();
            store.load( null, password );

            // initialize key and trust managers -> default behavior
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( "SunX509" );
            // password for key and store have to be the same IIRC
            keyManagerFactory.init( store, password );
            return keyManagerFactory.getKeyManagers();
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Impossible to initialize SSL certificate/key", e );
        }
    }

    private static File checkFile( String fileLocation )
    {
        checkArgument( fileLocation != null, "Impossible to read the certificate from a null location!" );

        File file = new File( fileLocation );
        checkArgument( file.exists(), "File %s not found, please verify it exists", file );
        checkArgument( !file.isDirectory(), "File %s must be not a directory", file );
        return file;
    }

    private RestClientFactory()
    {
        // do nothing
    }

    private static final class RelaxedTrustManager
        implements X509TrustManager
    {

        @Override
        public void checkClientTrusted( X509Certificate[] chain, String authType )
            throws CertificateException
        {
            // do nothing
        }

        @Override
        public void checkServerTrusted( X509Certificate[] chain, String authType )
            throws CertificateException
        {
            // do nothing
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

    }

}
