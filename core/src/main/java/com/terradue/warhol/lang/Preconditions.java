package com.terradue.warhol.lang;

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

import static java.lang.String.format;

public final class Preconditions
{

    private Preconditions()
    {
        // do nothing
    }

    public static <T> T checkNotNull( T reference, String errorMessageTemplate, Object... errorMessageArgs )
    {
        if ( reference == null )
        {
            // If either of these parameters is null, the right thing happens anyway
            throw new NullPointerException( format( errorMessageTemplate, errorMessageArgs ) );
        }
        return reference;
    }

    public static <T> T checkNotNullArgument( T reference, String errorMessageTemplate, Object... errorMessageArgs )
    {
        if ( reference == null )
        {
            // If either of these parameters is null, the right thing happens anyway
            throw new IllegalArgumentException( format( errorMessageTemplate, errorMessageArgs ) );
        }
        return reference;
    }

    public static void checkArgument( boolean expression, String errorMessageTemplate, Object... errorMessageArgs )
    {
        if ( !expression )
        {
            throw new IllegalArgumentException( format( errorMessageTemplate, errorMessageArgs ) );
        }
    }

    public static void checkState( boolean expression, String errorMessageTemplate, Object... errorMessageArgs )
    {
        if ( !expression )
        {
            throw new IllegalStateException( format( errorMessageTemplate, errorMessageArgs ) );
        }
    }

}
