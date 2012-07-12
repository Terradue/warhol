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
import static com.terradue.warhol.lang.Preconditions.checkNotNullArgument;

import java.util.HashMap;
import java.util.Map;

import org.w3._2005.atom.Feed;

import com.sun.jersey.api.client.Client;
import com.terradue.warhol.settings.Catalogue;
import com.terradue.warhol.settings.Settings;
import com.terradue.warhol.traverse.TraverseHandler;
import com.terradue.warhol.traverse.TraverseHandlerBuilder;

public final class CatalogueSystem
{
    private static final String ATOM_XML = "application/atom+xml";

    private final Map<String, Catalogue> cataloguesIndex = new HashMap<String, Catalogue>();

    private final Client restClient;

    private final Settings settings;

    public CatalogueSystem( Client restClient, Settings settings )
    {
        this.restClient = checkNotNullArgument( restClient, "Impossible to initialize a CatalogueSystem with a null REST client" );
        this.settings = checkNotNullArgument( settings, "Impossible to initialize a CatalogueSystem from a null Settings reference" );

        for ( Catalogue catalogue : settings.getCatalogues().getCatalogue() )
        {
            cataloguesIndex.put( catalogue.getId(), catalogue );
        }
    }

    public TraverseHandlerBuilder traverse( String catalogueId )
    {
        catalogueId = checkNotNullArgument( catalogueId, "Catalogue ID must be not null." );
        checkArgument( cataloguesIndex.containsKey( catalogueId ), "catalogue <%s> not present in the current system, only %s available",
                                                                   catalogueId, cataloguesIndex.keySet() );
        return new TraverseHandlerBuilder()
        {

            @Override
            public void with( TraverseHandler traverseHandler )
            {
                traverseHandler = checkNotNullArgument( traverseHandler, "Impossible to traverse the Catalogue <%s> with a null handler" );
            }

        };
    }

    private Feed parse( String uri )
    {
        return restClient.resource( uri ).accept( ATOM_XML ).get( Feed.class );
    }

}
