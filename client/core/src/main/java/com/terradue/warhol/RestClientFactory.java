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

import static java.util.ServiceLoader.load;
import static com.terradue.warhol.lang.Preconditions.checkNotNull;
import static org.sonatype.spice.jersey.client.ahc.AhcHttpClient.create;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.resumable.ResumableIOExceptionFilter;
import com.sun.jersey.api.client.Client;
import com.terradue.warhol.auth.AuthenticationConfiguration;
import com.terradue.warhol.settings.Authentication;
import com.terradue.warhol.settings.DataSource;
import com.terradue.warhol.settings.HttpSettings;

final class RestClientFactory
{

    private static final RestClientFactory INSTANCE = new RestClientFactory();

    public static RestClientFactory getRestClientFactoryInstance()
    {
        return INSTANCE;
    }

    private final Map<Class<? extends Authentication>, AuthenticationConfiguration<? extends Authentication>> registry =
                  new ConcurrentHashMap<Class<? extends Authentication>, AuthenticationConfiguration<? extends Authentication>>();

    private RestClientFactory()
    {
        for ( AuthenticationConfiguration<? extends Authentication> configuration : load( AuthenticationConfiguration.class ) )
        {
            registry.put( configuration.getAuthenticationType(), configuration );
        }
    }

    public Client newRestClient( DataSource dataSource )
    {
        DefaultAhcConfig config = new DefaultAhcConfig();

        Builder httpClientConfig = config.getAsyncHttpClientConfigBuilder();

        // basic http settings
        HttpSettings httpSettings = checkNotNull( dataSource.getHttpSettings(), "HTTP settings cannot be null in DataSource" );
        httpClientConfig.setRequestTimeoutInMs( httpSettings.getConnectionTimeout() * 60 * 60 * 1000 ) // 45 minutes
                        .setAllowPoolingConnection( httpSettings.isAllowPoolingConnection() )
                        .addIOExceptionFilter( new ResumableIOExceptionFilter() )
                        .setMaximumConnectionsPerHost( httpSettings.getHostMaximumConnection() )
                        .setMaximumConnectionsTotal( httpSettings.getTotalMaximumConnection() )
                        .setFollowRedirects( httpSettings.isFollowRedirects() );

        // authentication(s)
        for ( Authentication authentication : dataSource.getAuthentications().getAuthentication() )
        {
            configure( httpClientConfig, authentication );
        }

        return create( config );
    }

    private <A extends Authentication> void configure( Builder httpClientConfig, A authentication )
    {
        @SuppressWarnings( "unchecked" ) // safe cast because of the registration method
        AuthenticationConfiguration<A> configuration = (AuthenticationConfiguration<A>) registry.get( authentication.getClass() );

        if ( configuration != null )
        {
            configuration.configure( httpClientConfig, authentication );
        }
    }

}
