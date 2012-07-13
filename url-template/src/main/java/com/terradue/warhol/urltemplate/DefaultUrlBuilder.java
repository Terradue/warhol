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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class DefaultUrlBuilder
    implements UrlBuilder
{

    private final Map<String, String> variables = new HashMap<String, String>();

    private final List<Appender> appenders;

    public DefaultUrlBuilder( List<Appender> appenders )
    {
        this.appenders = appenders;
    }

    @Override
    public VariableValueBinder bind( final String variable )
    {
        return new VariableValueBinder()
        {

            @Override
            public UrlBuilder to( String value )
            {
                variables.put( variable, variable );

                return DefaultUrlBuilder.this;
            }

        };
    }

    @Override
    public String generate()
    {
        StringBuilder buffer = new StringBuilder();

        for ( Appender appender : appenders )
        {
            appender.append( buffer, variables );
        }

        return buffer.toString();
    }

}
