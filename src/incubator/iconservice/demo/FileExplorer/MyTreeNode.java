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

import java.io.File;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JOptionPane;


/**
 * JDIC API demo class.
 * <p>
 * A redefined MutableTreeNode class.
 */

class MyTreeNode extends DefaultMutableTreeNode {
    private static String osName = System.getProperty("os.name").toLowerCase();
    private boolean explored = false;

    private void errorDisplayingPermission() {
        JOptionPane.showMessageDialog(null,
                "You do not have the permissions necessary to view the contents of \""
                + getFile() + "\"", "Error Displaying Folder",
                JOptionPane.ERROR_MESSAGE);
    }

    private void errorDisplayingNoDisk() {
        File currentFile = getFile();
        String fileName = currentFile.getName();
        String errorMsg =
            "The file : "
                + fileName
                + " does not exist any"
                + " longer!";
        if (osName.startsWith("windows")) {
            //Windows platform, check if it's disk
            File parentFile = currentFile.getParentFile();
            if (currentFile.getParentFile() == null) {
                //parent file is null, should be a disk
                errorMsg = "Please insert disk into driver:  " + currentFile;
            }
        }
        JOptionPane.showMessageDialog(
            null,
            errorMsg,
            "Error Displaying Folder",
            JOptionPane.ERROR_MESSAGE);
    }

    // Assocoates a file object with this node.
    public MyTreeNode(File file) {
        setUserObject(file);
    }

    public boolean getAllowsChildren() {
        return isDirectory();
    }

    public boolean isLeaf() {
        return !isDirectory();
    }

    public File getFile() {
        return (File) getUserObject();
    }

    public boolean isExplored() {
        return explored;
    }

    public boolean isDirectory() {
        File file = getFile();

        return file.isDirectory();
    }

    public void explore() {
        File file = getFile();
      
        if (!isDirectory()) {
            return;
        }

        //Check if the file exists
        if (!file.exists()) {
            errorDisplayingNoDisk();
            return;
        }
        
        // Check if the file is readable.
        if (!file.canRead()) {
            errorDisplayingPermission();
            return;
        }
        
        if (!isExplored()) {
            File[] children = file.listFiles();

            if (children != null) {
                for (int i = 0; i < children.length; ++i) {
                    if (children[i].isDirectory()) {
                        add(new MyTreeNode(children[i]));
                    }
                }
            }

            explored = true;
        }
    }

    public String toString() {
        File file = (File) getUserObject();
        String filename = file.toString();

        int index = filename.lastIndexOf(File.separator);

        return (index != -1 && index != filename.length() - 1)
                ? filename.substring(index + 1)
                : filename;
    }

    /**
     * Gets the files and subdirectories under this file object.
     */
    public int getChildrenCount() {
        File file = getFile();

        //Check if the file exists
        if (!file.exists()) {
            errorDisplayingNoDisk();
            return 0;
        }

        if (!file.canRead()) {
            errorDisplayingPermission();        
            return 0;
        }
        if (!isDirectory()) {
            return 0;
        } else {
            File[] children = file.listFiles();
      
            return (children != null) ? children.length : 0;
        }
    }

    /**
     * Gets size of this file object.
     */
    public long getSize() {
        File file = getFile();

        if (!file.canRead()) {
            return 0;    
        }
      
        if (!isDirectory()) {
            return (file.length());
        }

        File[] children = file.listFiles();

        long size = 0;

        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                size += children[i].length();
            }
        }

        return size;
    }
}
