// Copyright � 2004 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

package org.jdesktop.jdic.screensaver.autogen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Steps through each screensaver by looking at the XML files present in
 * the configuration directory.  Autogenerates the appropriate files for
 * each platform.
 *
 * Each platform is handled differently.
 *
 * For Windows, a binary (produced by the ant-native-win32 task in the
 * SDK workspace) is copied, and parameters in the binary are substituted.
 * This allows the building of .scr files without needing to purchase
 * the Visual C++ compiler.
 * 
 * For Unix, the target is a Makefile and a collection of source files,
 * so that the screensaver can be compiled for any target Unix platform.
 *
 * The following substitutions are performed when templates are copied over:
 * <ul>
 *   <li>[[jar]] - Name of the JAR containing the screensaver.</li>
 *   <li>[[name]] - Name of the screensaver.</li>
 *   <li>[[class]] - Class implementing xscreensaver.</li>
 *   <li>[[config]] - Copy of config XML as hex data.</li>
 *   <li>[[options]] - Xrm options string for xscreensaver.</li>
 *   <li>[[exe]] - The name of the final executable.</li>
 *   <li>[[source]] - Filename of source file, without extension.</li>
 * </ul>
 *
 * The tasks in the body of the <foreachscreensaver> task are executed
 * for each screensaver, and the above substitutions are performed for
 * the child tasks as well.
 *
 * @author Mark Roth
 */
public class ForEachScreensaver 
    extends Task 
{

    /** The directory containing screensaver configuration files */
    private File confDir;
    
    /** 
     * The directory in which to generate the final binaries and 
     * generated source.
     */
    private File outDir;
    
    /** The list of exec tasks */
    private ArrayList execs = new ArrayList();
    
    /** The platform we're running on */
    private String os;
    
    public void setConfDir( File confDir ) {
        this.confDir = confDir;
    }
    
    public File getConfDir() {
        return this.confDir;
    }
    
    public void setOutDir( File outDir ) {
        this.outDir = outDir;
    }
    
    public File getOutDir() {
        return this.outDir;
    }
    
    public void addExec( LocalExec exec ) {
        this.execs.add( exec );
    }
    
    public java.lang.String getOs() {
        return os;
    }
    
    public void setOs(java.lang.String os) {
        this.os = os;
    }
    
    public void execute() throws BuildException {
        if( confDir == null ) {
            throw new BuildException( "confDir parameter is required." );
        }
        
        if( outDir == null ) {
            throw new BuildException( "outDir parameter is required." );
        }
        
        if( !outDir.exists() || !outDir.isDirectory() ) {
            throw new BuildException( "outDir is not a directory or " +
                "does not exist." );
        }
        
        if( os == null ) {
            throw new BuildException( "os parameter is required." );
        }
        
        File[] confFiles = confDir.listFiles( 
            new FilenameFilter() {
                public boolean accept( File dir, String filename ) {
                    return filename.endsWith( ".xml" ) ||
                           filename.endsWith( ".XML" );
                }
            } );
            
        // Keep track of all screensavers so we can build our Makefile
        Properties savers = new Properties();
        
        try {
            DocumentBuilderFactory builderFactory = 
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            for( int i = 0; i < confFiles.length; i++ ) {
                log( "Processing " + confFiles[i].getName() + " for " + os + 
                    "..." );

                Properties variables = new Properties();
                
                try {
                    InputStream in = new FileInputStream( confFiles[i] );
                    Document document = builder.parse( new InputSource( in ) );
                    HackConfigScanner scanner = new HackConfigScanner( 
                        document );
                    scanner.visitDocument();
                    in.close();
                    HackConfig config = scanner.getHackConfig();
                    log( "  - Command name: " + config.getName() );
                    String exeName = config.getName() + "-bin";
                    variables.setProperty( "[[exe]]", exeName );
                    
                    // Find -jar and -class arguments:
                    String jarArg = null;
                    String classArg = null;
                    ArrayList options = config.getOptions();
                    for( int j = 0; j < options.size(); j++ ) {
                        HackConfig.Option opt =
                            (HackConfig.Option)options.get( j );
                        if( opt instanceof HackConfig.CommandOption ) {
                            HackConfig.CommandOption cmd = 
                                (HackConfig.CommandOption)opt;
                            String arg = cmd.getArg();
                            if( arg.startsWith( "-jar " ) ) {
                                jarArg = arg.substring( 5 );
                            }
                            else if( arg.startsWith( "-class " ) ) {
                                classArg = arg.substring( 7 );
                            }
                        }
                    }
                    
                    if( jarArg == null ) {
                        throw new BuildException( "Error: " + 
                            confFiles[i] + " has no command with " +
                            "-jar argument" );
                    }
                    
                    log( "  - JAR: " + jarArg );
                    
                    if( classArg == null ) {
                        throw new BuildException( "Error: " + 
                            confFiles[i] + " has no command with " +
                            "-class argument" );
                    }
                    
                    log( "  - Class: " + classArg );
                    
                    String filename = confFiles[i].getName();
                    int index = filename.lastIndexOf( '.' );
                    variables.setProperty( "[[source]]", 
                        filename.substring( 0, index ) );
                    filename = filename.substring( 0, index );
                    
                    if(os.equals("unix")) {
                        // On Unix, we need to generate:
                        //   1. A shell script wrapper
                        //   2. The source code
                        // The shell script wrapper is needed because 
                        // XrmOptionDescRec needs to be set right when we 
                        // add customization options but LD_LIBRARY_PATH 
                        // needs to be set as well (on Linux)
                        generateUnixShellWrapper(outDir, filename, jarArg, 
                            classArg, exeName);
                        generateUnixSource(outDir, filename, jarArg, classArg, 
                            config.getName(), options);
                        savers.setProperty(filename, exeName);
                    }
                    else if(os.equals("win32")) {
                        generateWindowsExecutable(outDir, filename, jarArg,
                            classArg, config.getName(), confFiles[i]);
                    }
                }
                catch( IOException e ) {
                    throw new BuildException( e );
                }
                catch( SAXException e ) {
                    throw new BuildException( e );
                }
                
                for( int j = 0; j < execs.size(); j++ ) {
                    LocalExec t = (LocalExec)execs.get( j );
                    t.setVariables( variables );
                    t.execute();
                }
            }
            if(os.equals("unix")) {
                generateUnixMakefile(outDir, savers);
            }
        }
        catch( ParserConfigurationException e ) {
            throw new BuildException( e );
        }
        catch( IOException e ) {
            throw new BuildException( e );
        }
    }

    private void generateUnixShellWrapper( File outDir, String filename, 
        String jarArg, String classArg, String exeName ) 
        throws IOException
    {
        File outFile = new File( outDir, filename );
        Properties substitute = new Properties();
        substitute.setProperty("jar", jarArg);
        substitute.setProperty("class", classArg);
        substitute.setProperty("exe", exeName);
        Utilities.copyFileAndSubstitute(outFile, 
            "/org/jdesktop/jdic/screensaver/autogen/resources/unix/" +
            "saverbeans.sh.template", substitute, "[[", "]]", false);
    }

    private void generateUnixMakefile(File outDir, Properties savers)
        throws IOException
    {
        File outFile = new File( outDir, "Makefile" );
        Properties substitute = new Properties();
        StringBuffer targets = new StringBuffer();
        Iterator iter = savers.keySet().iterator();
        // key = filename, value = exeName
        StringBuffer exes = new StringBuffer();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            String value = savers.getProperty(key);
            exes.append(value);
            exes.append(' ');
        }
        substitute.setProperty("exes", exes.toString());
        iter = savers.keySet().iterator();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            String value = savers.getProperty(key);
            targets.append(value + ":\n");
            targets.append(
                "\t${cc} ${ccargs} -o " + key + ".o " + key + ".c\n" +
                "\t${link} ${linkargs} -o " + value + " " + key + 
                    ".o ${linkobj}\n" + 
                "\t${strip} " + value + "\n" +
                "\tchmod a+x " + key + "\n\n");
        }
        substitute.setProperty("targets", targets.toString());
        Utilities.copyFileAndSubstitute(outFile, 
            "/org/jdesktop/jdic/screensaver/autogen/resources/unix/" +
            "Makefile.template", substitute, "[[", "]]", false);
    }
    
    private void generateUnixSource( File outDir, String filename,
        String jarArg, String classArg, String screensaverName,
        ArrayList options )
        throws IOException
    {
        // Go through all options and create a list of valid
        // commandline parameters:
        ArrayList allParams = new ArrayList();
        for( int j = 0; j < options.size(); j++ ) {
            HackConfig.Option opt =
                (HackConfig.Option)options.get( j );
            HackConfig.Parameter[] params = opt.getParameters();
            // Add each parameter if it doesn't already exist
            for( int k = 0; k < params.length; k++ ) {
                if( !allParams.contains( params[k] ) ) {
                    allParams.add( params[k] );
                }
            }
        }
        
        // Create an XrmOptionDescRec entry for each valid 
        // commandline parameter, except for jar and class.
        StringBuffer optionsBuffer = new StringBuffer();
        String[] kind = new String[] { 
            "XrmoptionIsArg", "XrmoptionSepArg" };
        for( int j = 0; j < allParams.size(); j++ ) {
            HackConfig.Parameter p = 
                (HackConfig.Parameter)allParams.get( j );
            if( !p.isStandardScreenhackParameter() ) {
                optionsBuffer.append( "    { " + 
                    "\"-" + p.getName() + "\", " +
                    "\"." + p.getName() + "\", " +
                    kind[p.getType()] + ", 0 },\n" );
            }
        }
            
        File outFile = new File( outDir, filename + ".c" );
        Properties substitute = new Properties();
        substitute.setProperty( "name", screensaverName );
        substitute.setProperty( "jar", jarArg );
        substitute.setProperty( "class", classArg );
        substitute.setProperty( "options", optionsBuffer.toString() );
        Utilities.copyFileAndSubstitute( outFile, 
            "/org/jdesktop/jdic/screensaver/autogen/resources/unix/" +
            "saverbeans-unix.c.template", substitute, "[[", "]]", false);
    }

    private void generateWindowsExecutable( File outDir, String exeName,
        String jarArg, String classArg, String screensaverName, 
        File configFile )
        throws IOException
    {
        // Windows needs the XML config file data so we can construct the
        // settings dialog from it.  Include as a char[]:
        StringBuffer configData = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(configFile));
        String line;
        while((line = in.readLine()) != null) {
            configData.append(line);
            configData.append('\n');
        }
        in.close();
        Properties substitute = new Properties();
        substitute.setProperty("jar", jarArg);
        substitute.setProperty("name", screensaverName);
        substitute.setProperty("class", classArg.replace('.', '/'));
        substitute.setProperty("config", configData.toString());
        File outFile = new File(outDir, exeName + ".scr");
        Utilities.copyBinaryFileAndSubstitute(outFile, 
            "/org/jdesktop/jdic/screensaver/autogen/resources/win32/" +
            "saverbeans-win32.scr", substitute);
    }

    /**
     * Embedded exec task that replaces all occurrences of [x] with
     * the value supplied in a Properties table.
     */
    public static class LocalExec extends Task {
        private ArrayList args = new ArrayList();
        private File dir;
        private String executable;
        private boolean failonerror;
        private Properties variables = new Properties();
        
        public void addArg( LocalArg arg ) {
            args.add( arg );
        }
        
        public void setVariables( Properties variables ) {
            this.variables = variables;
        }
        
        public File getDir() {
            return dir;
        }
        
        public void setDir(File dir) {
            this.dir = dir;
        }
        
        public java.lang.String getExecutable() {
            return executable;
        }
        
        public void setExecutable(java.lang.String executable) {
            this.executable = executable;
        }
        
        public boolean isFailonerror() {
            return failonerror;
        }
        
        public void setFailonerror(boolean failonerror) {
            this.failonerror = failonerror;
        }
        
        public void execute() {
            ExecTask execTask = (ExecTask)project.createTask( "exec" );
            execTask.setDir( dir );
            execTask.setExecutable( executable );
            execTask.setFailonerror( failonerror );
            for( int i = 0; i < args.size(); i++ ) {
                Commandline.Argument arg = execTask.createArg();
                LocalArg localArg = (LocalArg)args.get( i );
                String line = localArg.getLine();
                line = replaceVars( line );
                arg.setLine( line );
            }
            execTask.init();
            execTask.execute();
        }
        
        private String replaceVars( String line ) {
            int index;
            Iterator iter = variables.keySet().iterator();
            while( iter.hasNext() ) {
                String var = (String)iter.next();
                String value = (String)variables.get( var );
                while( (index = line.indexOf( var )) != -1 ) {
                    line = line.substring( 0, index ) + value + 
                        line.substring( index + var.length() );
                }
            }
            return line;
        }
    }
    
    public static class LocalArg {
        private String line;
        
        public java.lang.String getLine() {
            return line;
        }
        
        public void setLine(java.lang.String line) {
            this.line = line;
        }
    }
}
