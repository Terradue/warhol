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
import static com.terradue.warhol.lang.Preconditions.checkState;

import java.io.File;

import com.terradue.warhol.settings.LocalStorage;

public final class LocalStorageConnector
{

    private final File baseDir;

    public LocalStorageConnector( LocalStorage localStorage )
    {
        checkArgument( localStorage != null, "Impossible to create a new LocalStorage connector from a null LocalStorage" );
        baseDir = new File( localStorage.getBaseDir() );
        checkState( baseDir.exists(),
                    "Impossible to create a new LocalStorage connector, base local storage directory %s does not exist",
                    baseDir );
        checkState( baseDir.isDirectory(),
                    "Impossible to create a new LocalStorage connector, base local storage %s is not a file",
                    baseDir );
    }

    public File getSeriesDir( String series )
    {
        File seriesFile = new File( baseDir, series );

        if ( !seriesFile.exists() )
        {
            checkState( seriesFile.mkdirs(),
                        "Impossible to create %s directory on FileSystem, please verify you have enough rights",
                        seriesFile );
        }

        return seriesFile;
    }

}
