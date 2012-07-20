package com.terradue.warhol.client.cli;

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

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.lang.System.out;
import static java.util.ServiceLoader.load;
import static org.slf4j.LoggerFactory.getILoggerFactory;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.Date;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.terradue.warhol.client.CatalogueSystem;
import com.terradue.warhol.client.settings.Settings;

public final class Warhol
{

    private final Logger logger = getLogger( getClass() );

    @Parameter( names = { "-h", "--help" }, description = "Display help information." )
    private boolean printHelp;

    @Parameter( names = { "-v", "--version" }, description = "Display version information." )
    private boolean showVersion;

    @Parameter( names = { "-X", "--verbose" }, description = "Produce execution debug output." )
    private boolean debug;

    @Parameter(
        names = { "-s", "--settings" },
        description = "Force the use of an alternate settings file.",
        converter = FileConverter.class
    )
    private File settingsFile = new File( getProperty( "basedir" ), "etc/settings.xml" );

    public int execute( String...args )
    {
        JCommander commander = new JCommander( this );
        commander.setProgramName( getProperty( "app.name" ) );

        for ( Command command : load( Command.class ) )
        {
            commander.addCommand( command );
        }

        commander.parse( args );

        if ( printHelp )
        {
            commander.usage();
            return 0;
        }

        if ( showVersion )
        {
            printVersionInfo();
            return 0;
        }

        // check the settings file!

        if ( !settingsFile.exists() )
        {
            out.printf( "Specified settings file %s does not exist, please specify a valid one.%n", settingsFile );
            return 1;
        }
        else if ( settingsFile.isDirectory() )
        {
            out.printf( "Specified settings file %s must not be a directory.%n", settingsFile );
            return 1;
        }

        // check the input the command

        Map<String, JCommander> commands = commander.getCommands();
        String parsedCommand = commander.getParsedCommand();

        if ( parsedCommand == null )
        {
            out.println( "No command specified, read the usage first." );
            commander.usage();
            return 1;
        }

        if ( !commands.containsKey( parsedCommand ) )
        {
            out.printf( "Invalid input command, read the usage first.%n" );
            commander.usage();
            return 1;
        }

        // so, init logging stuff

        if ( debug )
        {
            setProperty( "logging.level", "DEBUG" );
        }
        else
        {
            setProperty( "logging.level", "INFO" );
        }

        // assume SLF4J is bound to logback in the current environment

        final LoggerContext lc = (LoggerContext) getILoggerFactory();

        try
        {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext( lc );
            // the context was probably already configured by default configuration
            // rules
            lc.reset();
            configurator.doConfigure( getClass().getClassLoader().getResourceAsStream( "logback-config.xml" ) );
        }
        catch ( JoranException je )
        {
            // StatusPrinter should handle this
        }

        logger.info( "" );
        logger.info( "------------------------------------------------------------------------" );
        logger.info( "T2 Warhol :: {}", parsedCommand );
        logger.info( "------------------------------------------------------------------------" );
        logger.info( "" );

        int exit = 0;
        long start = currentTimeMillis();

        Throwable error = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance( "com.terradue.warhol.client.settings" );
            Unmarshaller xmlUnmarshaller = context.createUnmarshaller();
            Settings settings = Settings.class.cast( xmlUnmarshaller.unmarshal( settingsFile ) );

            CatalogueSystem catalogueSystem = new CatalogueSystem( settings );

            Command.class.cast( commands.get( parsedCommand ).getObjects().get( 0 ) ).execute( catalogueSystem );
        }
        catch ( Throwable t )
        {
            exit = 1;
            error = t;
        }
        finally
        {
            logger.info( "" );
            logger.info( "------------------------------------------------------------------------" );
            logger.info( "T2 Warhol {}", ( exit > 0 ) ? "FAILURE" : "SUCCESS" );

            if ( exit > 0 )
            {
                logger.info( "" );

                if ( debug )
                {
                    logger.error( "Execution terminated with errors", error );
                }
                else
                {
                    logger.error( "Execution terminated with errors: {}", error.getMessage() );
                }

                logger.info( "" );
            }

            logger.info( "Total time: {}s", ( ( currentTimeMillis() - start ) / 1000 ) );
            logger.info( "Finished at: {}", new Date() );

            final Runtime runtime = getRuntime();
            final int megaUnit = 1024 * 1024;
            logger.info( "Final Memory: {}M/{}M",
                         ( runtime.totalMemory() - runtime.freeMemory() ) / megaUnit,
                         runtime.totalMemory() / megaUnit );

            logger.info( "------------------------------------------------------------------------" );
        }

        return exit;
    }

    private static void printVersionInfo()
    {
        out.printf( "T2 Warhol %s%n",
                    getProperty( "warhol.version" ) );
        out.printf( "Java version: %s, vendor: %s%n",
                    getProperty( "java.version" ),
                    getProperty( "java.vendor" ) );
        out.printf( "Java home: %s%n",
                    getProperty( "java.home" ) );
        out.printf( "Default locale: %s_%s, platform encoding: %s%n",
                    getProperty( "user.language" ),
                    getProperty( "user.country" ),
                    getProperty( "sun.jnu.encoding" ) );
        out.printf( "OS name: \"%s\", version: \"%s\", arch: \"%s\", family: \"%s\"%n",
                    getProperty( "os.name" ),
                    getProperty( "os.version" ),
                    getProperty( "os.arch" ),
                    getOsFamily() );
    }

    private static final String getOsFamily()
    {
        String osName = System.getProperty( "os.name" ).toLowerCase();
        String pathSep = System.getProperty( "path.separator" );

        if ( osName.contains( "windows" ) )
        {
            return "windows";
        }
        else if ( osName.contains( "os/2" ) )
        {
            return "os/2";
        }
        else if ( osName.contains( "z/os" ) || osName.contains( "os/390" ) )
        {
            return "z/os";
        }
        else if ( osName.contains( "os/400" ) )
        {
            return "os/400";
        }
        else if ( pathSep.equals( ";" ) )
        {
            return "dos";
        }
        else if ( osName.contains( "mac" ) )
        {
            if ( osName.endsWith( "x" ) )
            {
                return "mac"; // MACOSX
            }
            return "unix";
        }
        else if ( osName.contains( "nonstop_kernel" ) )
        {
            return "tandem";
        }
        else if ( osName.contains( "openvms" ) )
        {
            return "openvms";
        }
        else if ( pathSep.equals( ":" ) )
        {
            return "unix";
        }

        return "undefined";
    }

    public static void main( String[] args )
    {
        exit( new Warhol().execute( args ) );
    }

}
