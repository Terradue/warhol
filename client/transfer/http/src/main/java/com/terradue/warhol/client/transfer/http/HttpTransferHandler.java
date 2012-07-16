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

import static com.ning.http.client.AsyncHandler.STATE.CONTINUE;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.List;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.terradue.warhol.client.transfer.ProductNotFoundException;
import com.terradue.warhol.client.transfer.TransferCancelledException;
import com.terradue.warhol.client.transfer.TransferException;

final class HttpTransferHandler
    implements AsyncHandler<File>
{

    private static final String CONTENT_LENGTH = "Content-Length";

    private final File targetFile;

    private final WritableByteChannel targetChannel;

    private long contentLength = -1;

    private long downloadCounter = 0;

    public HttpTransferHandler( File targetFile )
    {
        this.targetFile = targetFile;
        try
        {
            targetChannel = new FileOutputStream( targetFile ).getChannel();
        }
        catch ( FileNotFoundException e )
        {
            // TODO should not happen!
            throw new TransferException( "File <%s> cannot be downloaded because cannot be found: <%s>", e.getMessage() );
        }
    }

    @Override
    public STATE onStatusReceived( HttpResponseStatus responseStatus )
        throws Exception
    {
        if ( HTTP_OK != responseStatus.getStatusCode() )
        {
            throw new ProductNotFoundException( "Impossible to save product on %s, server replied %s %s",
                                                responseStatus.getStatusCode(), responseStatus.getStatusText() );
        }
        return CONTINUE;
    }

    @Override
    public STATE onHeadersReceived( HttpResponseHeaders headers )
        throws Exception
    {
        List<String> contentLength = headers.getHeaders().get( CONTENT_LENGTH );

        if ( !contentLength.isEmpty() )
        {
            this.contentLength = Long.valueOf( contentLength.iterator().next() ).longValue();
        }

        return CONTINUE;
    }

    @Override
    public STATE onBodyPartReceived( HttpResponseBodyPart bodyPart )
        throws Exception
    {
        downloadCounter += bodyPart.getBodyPartBytes().length;
        targetChannel.write( bodyPart.getBodyByteBuffer() );
        return CONTINUE;
    }

    @Override
    public File onCompleted()
        throws Exception
    {
        targetChannel.close();

        if ( contentLength != downloadCounter )
        {
            throw new TransferCancelledException( "Expected %s bytes but downloaded just %s bytes",
                                                  contentLength, downloadCounter );
        }

        return targetFile;
    }

    @Override
    public void onThrowable( Throwable t )
    {
        throw new TransferCancelledException( "Impossible to transfer current product, see nested exceptions", t );
    }

}
