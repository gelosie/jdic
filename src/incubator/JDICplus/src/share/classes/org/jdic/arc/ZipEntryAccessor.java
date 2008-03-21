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

package org.jdic.arc;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.zip.ZipEntry;

/**
 * The class is responsible reflection access to protected fields of 
 * <code>ZipEntry</code> class. 
 * @author uta
 */

public class ZipEntryAccessor {
    private static Class componentClass;
    private static Field fieldMethod;
    private static Field fieldName;

    static {
        AccessController.doPrivileged( new PrivilegedAction() {
            public Object run() {
                try {
                    componentClass = Class.forName("java.util.zip.ZipEntry");
                    fieldMethod  = componentClass.getDeclaredField("method");
                    fieldMethod.setAccessible(true);
                    fieldName  = componentClass.getDeclaredField("name");
                    fieldName.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    System.out.println("Unable to create ZipEntry : ");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    System.out.println("Unable to create ZipEntry : ");
                    e.printStackTrace();
                }
                // to please javac
                return null;
            }
        });
    }

    /**
     * Set <code>method</code> field of <code>ze</code> to value METHOD_MSZIP, 
     * METHOD_QUANTUM, METHOD_LZX.
     * @param ze the entry for modify
     * @param method the desired compression method
     */
    public static void setMethod(ZipEntry ze, int method) {
        try {
            fieldMethod.setInt(ze, method);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Set <code>name</code> field of <code>ze</code>.
     * @param ze the entry for modify
     * @param name the desired entry name
     */
    public static void setName(ZipEntry ze, String name) {
        try {
            fieldName.set(ze, name);
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
