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
 * The class represents an disk object.
 */
public class DiskObject {
    public static String TYPE_COMPUTER = "Computer";
    public static String TYPE_DRIVER = "Driver";
    public static String TYPE_FOLDER = "Folder";
    public static String TYPE_FILE = "File";

    public String name;
    public String type;

    public DiskObject(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String toString() {
        return name;
    }
}
