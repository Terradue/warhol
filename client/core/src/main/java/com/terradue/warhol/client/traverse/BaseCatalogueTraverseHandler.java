package com.terradue.warhol.client.traverse;

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

import org.w3._2005.atom.Entry;

/**
 * Base {@link CatalogueTraverseHandler} implementation which methods do nothing.
 */
public abstract class BaseCatalogueTraverseHandler
    implements CatalogueTraverseHandler
{

    @Override
    public void onSeries( Entry series )
    {
        // do nothing
    }

    @Override
    public void onDataSet( Entry product )
    {
        // do nothing
    }

}
