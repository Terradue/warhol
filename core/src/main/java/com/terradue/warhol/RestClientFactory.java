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

import static org.sonatype.spice.jersey.client.ahc.AhcHttpClient.create;

import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import com.ning.http.client.AsyncHttpClientConfig.Builder;
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
        if ( authentication instanceof SslAuthentication )
        {
            configure( config.getAsyncHttpClientConfigBuilder(), (SslAuthentication) authentication );
        }
        else if ( authentication instanceof HttpAuthentication )
        {
            configure( config.getAsyncHttpClientConfigBuilder(), (HttpAuthentication) authentication );
        }

        return create( config );
    }

    private static void configure( Builder builder, SslAuthentication authentication )
    {

    }

    private static void configure( Builder builder, HttpAuthentication authentication )
    {

    }

    private RestClientFactory()
    {
        // do nothing
    }

}
