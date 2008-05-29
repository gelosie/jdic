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
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
                              

/**
 * The class is responsible for JDICplus native libraries loading.
 * @author uta
 */

public class NativeLoadMgr { 
    private static HashSet<String> loadedLibraries = new HashSet<String>();
    //private static Method mdNativeExtractor_loadLibruary;
    private static boolean initNativeLoader = false;
    private static Class clNativeExtractor = null;    
    
    public static void init()
    {
        try {
            System.err.println("{Native loader");
            clNativeExtractor = NativeExtractor.class;
            System.err.println("}Native loader");
        } catch (Throwable e) {
            //that is ok!
        }
        if (System.getProperty("javawebstart.version") != null){
            System.err.println("Web start!");
        } else {
            try {
                // Find the root path of this class.
                if( null==clNativeExtractor 
                    && NativeLoadMgr.class.getClassLoader() instanceof URLClassLoader)
                {
                    String binaryPath = (
                        new URL(
                            NativeLoadMgr.class.getProtectionDomain().getCodeSource().getLocation(),
                            "."
                        )
                    ).openConnection().getPermission().getName();
                    binaryPath = (new File(binaryPath)).getCanonicalPath();
                    String newLibPath = binaryPath + File.separator +  "bin" + File.pathSeparator +
                                        System.getProperty("java.library.path");
                    System.err.print("New binary path:" + newLibPath);
                    System.setProperty("java.library.path", newLibPath);
                    Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                    fieldSysPath.setAccessible(true);
                    if (fieldSysPath != null) {
                        fieldSysPath.set(System.class.getClassLoader(), null);
                    }
                }   
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Class can not be instantiated.
     */
    private NativeLoadMgr() {}
    
    /**
     * Loads the native dynamic libruary from the appropriate folder.
     * @param libname the native dynamic libruary name without an extension
     */
    public static synchronized boolean loadLibrary(String libname) {
        if(!initNativeLoader){
            init();
            initNativeLoader = true;            
        }
        if( !loadedLibraries.contains(libname) ){
            loadedLibraries.add(libname);
            try {
                if(null!=clNativeExtractor){
                    NativeExtractor.loadLibruary(libname);                    
                } else {
                    System.loadLibrary(libname);
                }    
            } catch(Throwable e) {
                e.printStackTrace();      
            }
            return true;
        }    
        return false;
    }
}
