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

import static com.terradue.warhol.lang.Preconditions.checkArgument;
import static com.terradue.warhol.lang.Preconditions.checkNotNullArgument;

import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class UrlTemplate
{

    public static UrlTemplate parseTemplate( String template )
    {
        template = checkNotNullArgument( template, "Impossible to parse a null template" );

        final List<Appender> appenders = new LinkedList<Appender>();
        final Set<String> variableNames = new HashSet<String>();

        int prev = 0;
        int pos;
        while ( ( pos = template.indexOf( '{', prev ) ) >= 0 )
        {
            if ( pos > 0 )
            {
                appenders.add( new TextAppender( template.substring( prev, pos ) ) );

                int endName = template.indexOf( '}', pos );
                checkArgument( endName >= 0, "Syntax error in property: %s", template );

                boolean optional = false;

                String variableName = template.substring( pos + 1, endName );

                if ( '?' == variableName.charAt( variableName.length() - 1 ) )
                {
                    variableName = variableName.substring( 0, variableName.length() - 1 );
                    optional = true;
                }

                appenders.add( new VariableAppender( variableName, optional ) );
                variableNames.add( variableName );
                prev = endName + 1;
            }
        }
        if ( prev < template.length() )
        {
            appenders.add( new TextAppender( template.substring( prev ) ) );
        }

        return new UrlTemplate( template, appenders, unmodifiableSet( variableNames ) );
    }

    private final String template;

    private final List<Appender> appenders;

    private final Set<String> variableNames;

    UrlTemplate( String template, List<Appender> appenders, Set<String> variableNames )
    {
        this.template = template;
        this.appenders = appenders;
        this.variableNames = variableNames;
    }

    public String getTemplate()
    {
        return template;
    }

    public Set<String> getVariableNames()
    {
        return variableNames;
    }

    public UrlBuilder createNewUrl()
    {
        return new DefaultUrlBuilder( appenders );
    }

    @Override
    public String toString()
    {
        return template;
    }

}
