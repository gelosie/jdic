/*
 * Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.jdic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Native DLL loader class
 * @author uta
 */
public class NativeExtractor {
    private static File tmpBaseDir = new File(
            System.getProperty("java.io.tmpdir")
            + File.separator + "jdicplus");

    public static void copyStream(
        InputStream in,
        OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void compareStreams(
        InputStream in1,
        InputStream in2) throws IOException
    {
        while( true ){
            int buf1 = in1.read();
            int buf2 = in2.read();
            if(buf1==buf2){
                if(buf1==-1)
                    break;
            }else{
                throw new IOException("wrong JDICplus versions!");
            }     
        }     
    }
    
    public static void loadLibruary(final String libName) {
        tmpBaseDir.mkdir();
        InputStream is = NativeExtractor.class.getClassLoader().getResourceAsStream(libName + ".dll");
        try {
            final File dll = new File(tmpBaseDir.getCanonicalPath()
                    + File.separator + libName + ".dll");
            dll.delete();
            
            if( dll.exists() ){
                InputStream  iso =  new FileInputStream(dll);
                compareStreams(is, iso);
                is.close();
                iso.close();
            } else {
                OutputStream os =  new FileOutputStream(dll);
                copyStream(is, os);
                is.close();
                os.close();
            }    
            AccessController.doPrivileged( new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    try {
                        dll.setExecutable(true);
                        dll.setReadable(true);
                        dll.setWritable(true);
                        System.load(dll.getCanonicalPath());
                        System.err.println("SUCCESS!!!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}