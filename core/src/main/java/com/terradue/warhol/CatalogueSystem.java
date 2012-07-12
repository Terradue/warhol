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

import com.terradue.warhol.settings.Settings;

public final class CatalogueSystem
{

    private final Settings settings;

    CatalogueSystem( Settings settings )
    {
        this.settings = checkNotNullArgument( settings, "Impossible to initialize a CatalogueSystem froma  null Settings reference" );
    }

}
