// Copyright © 2004 Sun Microsystems, Inc. All rights reserved. Use is
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * Utility methods
 *
 * @author  Mark Roth
 */
public final class Utilities {

    /** Utility class, private constructor */
    private Utilities() {
    }
    
    /**
     * Copies the given source resource (must be in classpath) to the 
     * given destination file, substituting all occurrences of 
     * <start-delimeter>x<end-delimeter> with
     * substitute.getProperty( "x" ).
     *
     * @param dest The destination file
     * @param source the source resource
     * @param substitute The map of keys and values
     * @param startDelimeter The start delimeter
     * @param endDelimeter The end delimeter
     * @param dosFormat If true, uses DOS-style line terminators.  Else
     *     uses Unix-style line terminators.
     * @throws IOException if an error occurs while copying
     */
    public static void copyFileAndSubstitute( File dest, String source, 
        Properties substitute, String startDelimeter, String endDelimeter,
        boolean dosFormat)
        throws IOException
    {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter( new FileWriter( dest ) );
            in = new BufferedReader( new InputStreamReader(
                Utilities.class.getResourceAsStream( source ) ) );
            String line;
            while( (line = in.readLine()) != null ) {
                StringBuffer result = new StringBuffer( line );
                int index = 0;
                while( (index = result.indexOf( 
                    startDelimeter, index )) != -1 ) 
                {
                    int end = result.indexOf( endDelimeter, index );
                    if( end == -1 ) break;
                    String key = result.substring( index + 
                        startDelimeter.length(), end );
                    String value = substitute.getProperty( key );
                    if( value != null ) {
                        result.replace( index, end + 
                            endDelimeter.length(), value );
                    }
                    index += 1;
                }
                out.print( result.toString() );
                if(dosFormat) {
                    out.print("\r\n");
                }
                else {
                    out.print("\n");
                }
            }
        }
        finally {
            if( out != null ) out.close();
            if( in != null ) in.close();
        }
    }
    
    /**
     * Copies the given source resource (must be in classpath) to the 
     * given destination file, substituting all occurrences of 
     * [[x      ]] with
     * substitute.getProperty("x").  There can be an arbitrary number
     * of spaces between the delimeters.  Only Strings can be substituted.
     * An ASCII 0 will be inserted after the end of the string.
     *
     * @param dest The destination file
     * @param source the source resource
     * @param substitute The map of keys and values
     * @throws IOException if an error occurs while copying
     */
    public static void copyBinaryFileAndSubstitute(File dest, String source, 
        Properties substitute)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(
            Utilities.class.getResourceAsStream(source));
        FileOutputStream out = null;
        
        try {
            out = new FileOutputStream(dest);
            int c;
            while((c = in.read()) != -1) {
                if(c == '[') {
                    c = in.read();
                    if(c == '[') {
                        // Read until ]]
                        StringBuffer buffer = new StringBuffer();
                        do {
                            c = in.read();
                            if(c == ']') {
                                c = in.read();
                                if(c == ']') {
                                    break;
                                }
                                else if(c == -1) {
                                    throw new IOException("Unterminated [[");
                                }
                                else {
                                    buffer.append(']');
                                    buffer.append((char)c);
                                }
                            }
                            else if(c == -1) {
                                throw new IOException("Unterminated [[");
                            }
                            else {
                                buffer.append((char)c);
                            }
                        } while(true);
                        String tag = buffer.toString().trim();
                        String value = substitute.getProperty(tag);
                        if(value == null) {
                            // no match
                            out.write('[');
                            out.write('[');
                            out.write(buffer.toString().getBytes());
                            out.write(']');
                            out.write(']');
                        }
                        else {
                            out.write(value.getBytes());
                            out.write(0);
                            long len = 3 + buffer.length() - value.length();
                            if(len < 0) {
                                throw new IOException(
                                  "String for tag ('" + tag + 
                                  "') is too long for buffer: '" + value + "'");
                            }
                            for(int i = 0; i < len; i++) {
                                out.write(0);
                            }
                        }
                    }
                    else {
                        out.write('[');
                        out.write(c);
                    }
                }
                else {
                    out.write(c);
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if( out != null ) out.close();
            if( in != null ) in.close();
        }
    }
    
    /**
     * Copies the given source resource (must be in classpath) to the 
     * given destination file, performing no substitution.
     *
     * @param dest The destination file
     * @param source the source resource
     * @throws IOException if an error occurs while copying
     */
    public static void copyFile(File dest, String source)
        throws IOException
    {
        FileOutputStream out = null;
        InputStream in = null;
        try {
            out = new FileOutputStream(dest);
            in = Utilities.class.getResourceAsStream(source);
            byte[] buffer = new byte[1024];
            int count;
            while((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
        finally {
            if(in != null) in.close();
            if(out != null) out.close();
        }
    }
}
