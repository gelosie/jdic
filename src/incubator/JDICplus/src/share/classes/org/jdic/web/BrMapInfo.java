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

package org.jdic.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;


/**
 * Map custom information holder.
 * @author uta
 */
public class BrMapInfo{
    static HashMap<String, File> scTmpMapFiles = new HashMap<String, File>();
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
    
    /**
     * Extracts HTML resource file with abstarction layer to the file system. 
     * @param stWhichMap String with resource HTML file name 
     * @return the reference to HTML temp file with Map code abstarction layer
     */
    public synchronized static String getHTMLFile(String stWhichMap)
    {
        try {
// no chanse with Google signature...
//            setURL(
//                getClass().getResourceAsStream(stWhichMap),
//                stWhichMap);
            File map = scTmpMapFiles.get(stWhichMap);
            if(null==map){
                File tm = File.createTempFile("aaa", "aaa");
                map =  new File( tm.getParent() + File.separator + stWhichMap);
                map.delete();
                map.deleteOnExit();
                tm.delete();
                scTmpMapFiles.put(stWhichMap, map);
                if(!map.exists()){
                    copyStream(
                            BrMap.class.getResourceAsStream(stWhichMap),
                            new FileOutputStream(map));
                }
            }    
            //return "file:///" + map.getAbsolutePath();            
            return "file:///" + map.getCanonicalPath().replace("\\", "/").replace(" ","%20");
        } catch( Exception e ) {
            e.printStackTrace();
        }        
        return null;
    }
    
    private String stName;
    private String stHTMLProxy;
    private int    iMaxZoomLevel;
    private String stIconName;
    
    public BrMapInfo(
            String _stName, 
            String _stHTMLProxy,
            int _iMaxZoomLevel,
            String _stIconName)
    {
        stName = _stName;
        stHTMLProxy = _stHTMLProxy;
        iMaxZoomLevel = _iMaxZoomLevel;
        stIconName = _stIconName;
    }    
    
    public String getName() { return stName; }
    public String getHTMLProxy() { return stHTMLProxy; }
    public int getMaxZoomLevel() { return iMaxZoomLevel; }
    public String getIconName() {  return stIconName; }    
    public String getHTMLFile() {  return getHTMLFile(stHTMLProxy); }
}
