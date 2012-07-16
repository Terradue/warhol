package com.terradue.warhol.client.transfer.http;

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

import static com.terradue.warhol.client.ahc.AhcConfigurator.getAhcConfiguratorInstance;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.terradue.warhol.client.transfer.BaseTransferrer;
import com.terradue.warhol.client.transfer.TransferCancelledException;
import com.terradue.warhol.client.transfer.Transferrer;
import com.terradue.warhol.settings.RemoteStorage;

@MetaInfServices( Transferrer.class )
public final class HttpTransferrer
    extends BaseTransferrer
{

    private final Logger logger = getLogger( getClass() );

    private AsyncHttpClient httpClient = null;

    @Override
    public void init( RemoteStorage remoteStorage )
    {
        Builder asyncHttpClientConfig = new Builder();

        getAhcConfiguratorInstance().configure( asyncHttpClientConfig, remoteStorage );

        httpClient = new AsyncHttpClient( asyncHttpClientConfig.build() );
    }

    @Override
    public File transfer( String fileLocation, File targetDir )
    {
        String fileName = fileLocation.substring( fileLocation.lastIndexOf( '/' ) + 1 );

        File targetFile = new File( targetDir, fileName );

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Downloading {} to {}...", fileLocation, targetDir );
        }

        try
        {
            return httpClient.prepareGet( fileLocation ).execute( new HttpTransferHandler( targetFile ) ).get();
        }
        catch ( Exception e )
        {
            throw new TransferCancelledException( "Impossible to download %s product: %s", fileLocation, e.getMessage() );
        }
    }

}
