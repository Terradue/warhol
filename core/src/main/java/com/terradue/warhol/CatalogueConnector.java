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

import static com.terradue.warhol.lang.Preconditions.checkNotNullArgument;

import org.w3._2005.atom.Feed;

import com.sun.jersey.api.client.Client;
import com.terradue.warhol.settings.Catalogue;
import com.terradue.warhol.traverse.CatalogueTraverseHandler;
import com.terradue.warhol.traverse.CatalogueTraverseHandlerBuilder;

public final class CatalogueConnector
{

    private static final String ATOM_XML = "application/atom+xml";

    private final Catalogue catalogue;

    private final Client restClient;

    public CatalogueConnector( Catalogue catalogue )
    {
        this.catalogue = checkNotNullArgument( catalogue, "Impossible to initialize a CatalogueConnector froma  null Catalogue" );
        restClient = null;
    }

    public CatalogueTraverseHandlerBuilder traverse()
    {
        return new CatalogueTraverseHandlerBuilder()
        {

            @Override
            public void with( CatalogueTraverseHandler traverseHandler )
            {
                traverseHandler = checkNotNullArgument( traverseHandler, "Impossible to traverse the Catalogue <%s> with a null handler" );


            }

        };
    }

    Feed parse( String uri )
    {
        return restClient.resource( uri ).accept( ATOM_XML ).get( Feed.class );
    }

}
