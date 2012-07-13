package com.terradue.warhol.urltemplate;

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

import static com.terradue.warhol.urltemplate.UrlTemplate.parseTemplate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class UrlTemplateTestCase
{

    @Test
    public void verifyVariableNames()
    {
        UrlTemplate template = parseTemplate( "http://example.com/people/{firstName}-{lastName}/SSN" );

        assertTrue( template.getVariableNames().contains( "firstName" ) );
        assertTrue( template.getVariableNames().contains( "lastName" ) );
        assertFalse( template.getVariableNames().contains( "doesNotExist" ) );
    }

    @Test
    public void variablesInterpolation()
    {
        UrlTemplate template = parseTemplate( "http://example.com/people/{firstName}-{lastName}/SSN" );

        String generatedUrl = template.createNewUrl()
                                      .bind( "firstName" ).to( "simo" )
                                      .bind( "lastName" ).to( "tripo" )
                                      .generate();

        assertEquals( "http://example.com/people/simo-tripo/SSN", generatedUrl );
    }

    @Test
    public void variablesNotInterpolated()
    {
        UrlTemplate template = parseTemplate( "http://example.com/people/{firstName}-{lastName}/SSN" );

        String generatedUrl = template.createNewUrl().generate();

        assertEquals( "http://example.com/people/-/SSN", generatedUrl );
    }

}
