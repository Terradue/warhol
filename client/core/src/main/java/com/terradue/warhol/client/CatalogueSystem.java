package com.terradue.warhol.client;

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

import com.terradue.warhol.client.traverse.CatalogueTraverseHandler;
import com.terradue.warhol.client.traverse.CatalogueTraverseHandlerBuilder;
import com.terradue.warhol.settings.Catalogue;
import com.terradue.warhol.settings.Settings;

public final class CatalogueSystem
{
    private final Map<String, Catalogue> cataloguesIndex = new HashMap<String, Catalogue>();

    public CatalogueSystem( Settings settings )
    {
        checkArgument( settings != null, "Impossible to initialize a CatalogueSystem from a null Settings reference" );

        for ( Catalogue catalogue : settings.getCatalogues().getCatalogue() )
        {
            cataloguesIndex.put( catalogue.getId(), catalogue );
        }
    }

    public CatalogueTraverseHandlerBuilder traverse( String catalogueId )
    {
        catalogueId = checkNotNullArgument( catalogueId, "Catalogue ID must be not null." );
        checkArgument( cataloguesIndex.containsKey( catalogueId ), "catalogue <%s> not present in the current system, only %s available",
                                                                   catalogueId, cataloguesIndex.keySet() );
        return new CatalogueTraverseHandlerBuilder()
        {

            @Override
            public void with( CatalogueTraverseHandler traverseHandler )
            {
                traverseHandler = checkNotNullArgument( traverseHandler, "Impossible to traverse the Catalogue <%s> with a null handler" );
            }

        };
    }

}
