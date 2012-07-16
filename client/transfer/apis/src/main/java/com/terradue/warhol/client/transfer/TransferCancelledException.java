package com.terradue.warhol.client.transfer;

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

public final class TransferCancelledException
    extends TransferException
{

    private static final long serialVersionUID = 2755867836442279828L;

    public TransferCancelledException( String messagePattern, Object... arguments )
    {
        super( messagePattern, arguments );
    }

    public TransferCancelledException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
