/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
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

/**
 * JDIC API demo class.
 * <p>
 * The utility class convers the length to KB numbers.
 */
import java.io.File;
import java.util.Vector;
class MyUtility {
    private static String osName = System.getProperty("os.name").toLowerCase();
    private static File roots[] = null;
    public static String length2KB(long length) {
        long KB = 1024;
        long MB = 1024 * 1024;

        if (length == 0) {
            return String.valueOf("0 KB ");
        }

        String kbStr = "";
        long mbCount = length / MB;

        long kbCount = ((length - mbCount * MB) + 1023) / KB;

        if (mbCount != 0) {
            kbStr += String.valueOf(mbCount + ",");
        }

        if (kbCount == 0) {
            if (mbCount == 0) {
                kbStr += String.valueOf("0 KB ");
            } else {
                kbStr += String.valueOf("000 KB ");
            }
        } else {
            kbStr += String.valueOf(kbCount + " KB ");
        }

        return kbStr;
    }
    
    static class FileSystemRoot extends File {
        public FileSystemRoot(File f) {
            super(f, "");
        }
        
        public FileSystemRoot(String s) {
            super(s);
        }
        
        public boolean isDirectory() {
            return true;
        }
    }
    
    public static File[] getRoots() {
        if (roots == null) {
            constructRoots();
        }
        return roots;
    }
    
    private static void constructRoots() {
        if (osName.startsWith("windows")) {
            Vector rootsVector = new Vector();
        
            // Create the A: drive whether it is mounted or not
            FileSystemRoot floppy = new FileSystemRoot("A" + ":" + "\\");
            rootsVector.addElement(floppy);

            // Run through all possible mount points and check
            // for their existance.
            for (char c = 'C'; c <= 'Z'; c++) {
                char device[] = {c, ':', '\\'};
                String deviceName = new String(device);
                File deviceFile = new FileSystemRoot(deviceName);
                if (deviceFile != null && deviceFile.exists()) {
                    rootsVector.addElement(deviceFile);
                }
            }
            roots = new File[rootsVector.size()];
            rootsVector.copyInto(roots);
        } else {
            roots = File.listRoots();
        }
    }
}
