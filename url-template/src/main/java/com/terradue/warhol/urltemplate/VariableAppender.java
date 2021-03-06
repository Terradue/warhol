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

import static java.lang.String.format;

import java.util.Map;

final class VariableAppender
    implements Appender
{

    private final String variableName;

    private final boolean optional;

    public VariableAppender( String variableName, boolean optional )
    {
        this.variableName = variableName;
        this.optional = optional;
    }

    @Override
    public void append( StringBuilder buffer, Map<String, String> variables )
    {
        String variableValue = variables.get( variableName );
        if ( variableValue != null )
        {
            buffer.append( variableValue );
        }
        else
        {
            checkArgument( optional, "Variable <%s> is not optional, must be specified", variableName );
        }
    }

    @Override
    public String toString()
    {
        return format( "{%s}" );
    }

}
