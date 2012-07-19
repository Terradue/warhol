package com.terradue.warhol.auth.ssl;

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

import java.io.File;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.ssl.KeyMaterial;
import org.kohsuke.MetaInfServices;

import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.terradue.warhol.auth.AuthenticationConfiguration;
import com.terradue.warhol.client.settings.SslAuthentication;

@MetaInfServices
public final class SslAuthenticationConfiguration
    implements AuthenticationConfiguration<SslAuthentication>
{

    @Override
    public Class<SslAuthentication> getAuthenticationType()
    {
        return SslAuthentication.class;
    }

    @Override
    public void configure( Builder httpClientConfig, SslAuthentication authentication )
    {
        // client
        KeyManager[] keyManagers;
        if ( authentication.getProxyCertificate() != null )
        {
            keyManagers = fromSslProxy( authentication.getProxyCertificate() );
        }
        else if ( authentication.getPublicCertificate() != null && authentication.getPrivateKey() != null )
        {
            keyManagers = fromSslKeyAndCertificate( authentication.getPublicCertificate(),
                                                    authentication.getPrivateKey(),
                                                    authentication.getPassword() );
        }
        else
        {
            keyManagers = new KeyManager[] {};
        }

        // server
        TrustManager[] trustManagers;
        if ( authentication.isCheckCertificate() )
        {
            trustManagers = new TrustManager[] {};
        }
        else
        {
            trustManagers = new TrustManager[] { new RelaxedTrustManager() };
        }

        SSLContext context = null;
        try
        {
            context = SSLContext.getInstance( "TLS" );
            context.init( keyManagers, trustManagers, null );
            httpClientConfig.setSSLContext( context );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Impossible to initialize SSL context", e );
        }
    }

    private KeyManager[] fromSslProxy( String proxyCertificateLocation )
    {
        return fromSslKeyAndCertificate( proxyCertificateLocation, proxyCertificateLocation, null );
    }

    private KeyManager[] fromSslKeyAndCertificate( String publicCertificateLocation, String provateKeyLocation, String sslPassword )
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
        File file = new File( fileLocation );
        checkArgument( file.exists(), "File %s not found, please verify it exists", file );
        checkArgument( !file.isDirectory(), "File %s must be not a directory", file );
        return file;
    }

}
