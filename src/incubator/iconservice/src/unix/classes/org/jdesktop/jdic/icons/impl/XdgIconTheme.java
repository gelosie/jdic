/*
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

package org.jdesktop.jdic.icons.impl;

import java.io.File;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

/**
* An icon theme based upon <a href="http://freedesktop.org/Standards/icon-theme-spec">
* freedesktop icon theme specification</a>
*/
public class XdgIconTheme {

    private String name;
    private long lastReadTime;
    private long lastModTime;
    private List directories;
    private List inherits;

    XdgIconTheme(String name) {
        this.name= name==null ?"hicolor" :name;
        directories= new ArrayList();
        inherits= new ArrayList();
        readIndex();
        if(inherits.size()==0 && !name.equals("hicolor")) {
            inherits.add(new XdgIconTheme("hicolor"));
        }
    }

    String getName() {
        return name;
    }

    File getIcon(String iconName, int size) {
        Fit fit= new Fit(iconName, size);
        fit.findIconThatFits(this);
        if(fit.bestFit!=null)
            return fit.bestFit;

        return getIconFile(iconName);
    }

    private static String home;
    private static String getHome() {
        if(home==null) {
            home= (String)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    // need permission: PropertyPermission("user.home", "read")
                    return System.getProperty("user.home");
                }
            });
        }
        return home;
    }

    private static File getIconFile(String fileName) {
        if(fileName==null || fileName.length()==0)
            return null;

        if(fileName.charAt(0)=='/')
            return new File(fileName);

        File f= new File(getHome()+"/.icons/"+fileName);
        if(f.canRead())
            return f;

        for(Iterator it= XdgDirectory.getDataDirs("/icons/"+fileName); it.hasNext(); ) {
            f= (File)it.next();
            if(f.canRead())
                return f;
        }

        f= new File("/usr/share/pixmaps/"+fileName);
        if(f.canRead())
            return f;

        return null;
    }

    private File getThemeFile(String fileName) {
        return getIconFile(name+"/"+fileName);
    }

    static private Pattern comma= Pattern.compile(",");

    private void readIndex() {
        long currentTime= System.currentTimeMillis();
        if(currentTime-lastReadTime <5000) {
            return;
        }
        lastReadTime= currentTime;

        File indexTheme= getThemeFile("index.theme");
        if(indexTheme==null) {
            indexTheme= getThemeFile("index.desktop");
            if(indexTheme==null) {
                return;
            }
        }

        long modTime= indexTheme.lastModified();
        if(lastModTime==modTime) {
            return;
        }
        lastModTime= modTime;

        inherits.clear();
        directories.clear();

        try {
            XdgDesktopReader index= new XdgDesktopReader(indexTheme);

            XdgDesktopReader.Group group= index.getGroup("Icon Theme");
            if(group==null)
                return;
    
            Iterator dirs= group.getStrings("Directories", comma);
            if(dirs!=null) {
                while(dirs.hasNext()) {
                    String dirName= dirs.next().toString();
        
                    XdgDesktopReader.Group dirGroup= index.getGroup(dirName);
                    if(dirGroup==null)
                        continue;
        
                    try {
                        Directory dir= new Directory(dirName, dirGroup);
                        directories.add(dir);
                    }
                    catch(Exception ex) {
                    }
                }
            }
    
            Iterator inh= group.getStrings("Inherits", comma);
            if(inh!=null) {
                while(inh.hasNext()) {
                    String inheritTheme= inh.next().toString();
                    inherits.add(new XdgIconTheme(inheritTheme));
                }
            }
        }
        catch(IOException ignore) {
        }
    }

    private static class Fit {

        final String iconName;
        final int size;

        int minError;
        File bestFit;

        Map beenHere;    // themeName to theme

        Fit(String iconName, int size) {
            this.iconName= iconName;
            this.size= size;
            minError= Integer.MAX_VALUE;
            beenHere= new HashMap();
        }

        boolean findIconThatFits(XdgIconTheme theme) {

            beenHere.put(theme.getName(), theme);

            for(Iterator dirs= theme.directories.iterator(); dirs.hasNext(); ) {
                Directory directory= (Directory)dirs.next();
                int err= directory.error(size);
                if(err==0) {
                    File f= directory.getDirectoryIcon(iconName);
                    if(f!=null) {
                        bestFit= f;
                        return true;
                    }
                }
                if(err<minError) {
                    File f= directory.getDirectoryIcon(iconName);
                    if(f!=null) {
                        minError= err;
                        bestFit= f;
                    }
                }
            }

            for(Iterator inh= theme.inherits.iterator(); inh.hasNext(); ) {
                XdgIconTheme parent= (XdgIconTheme)inh.next();
                if(beenHere.get(parent.getName())!=null)
                    continue;

                if(findIconThatFits(parent))
                    return true;
            }

            return false;
        }
    };

    private class Directory {
        int maxSize;
        int minSize;
        final String dirName;

        Directory(String dirName, XdgDesktopReader.Group group) {
            this.dirName= dirName;

            Integer i= group.getInteger("Size");
            if(i==null)
                throw new IllegalArgumentException();
            int size= i.intValue();
            minSize= maxSize= size;

            String s= group.getString("Type");
            if(s!=null) {
                if(s.equals("Fixed")) {
                    i= group.getInteger("Threshold");
                    int threshold= i==null ?2 :i.intValue();
                    minSize= size-threshold;
                    maxSize= size+threshold;
                }
                else if(s.equals("Scalable")) {
                    i= group.getInteger("MaxSize");
                    maxSize= i==null ?size :i.intValue();
        
                    i= group.getInteger("MinSize");
                    minSize= i==null ?size :i.intValue();
                }
            }
        }

        int error(int requestedSize) {
            if (requestedSize < minSize)
                return minSize - requestedSize;

            if (requestedSize > maxSize)
                return requestedSize - maxSize;

            return 0;
        }

        File getDirectoryIcon(String iconName) {
            return getThemeFile(dirName+"/"+iconName);
        }
    }

}                                                          
