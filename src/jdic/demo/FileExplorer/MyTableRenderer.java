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

import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * JDIC API demo class.
 * <p>
 * A redefined TableCellRenderer class.
 */

public class MyTableRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value != null && value instanceof DiskObject) {
            // Set the Name column as left aligned.
            DiskObject diskObject = (DiskObject) value;

            ((JLabel) component).setText(diskObject.name);
            ((JLabel) component).setHorizontalAlignment(JLabel.LEFT);

            if (diskObject.type.equals(DiskObject.TYPE_DRIVER)) {
                ((JLabel) component).setIcon(FileExplorer.driverIcon);
            } else if (diskObject.type.equals(DiskObject.TYPE_FOLDER)) {
                ((JLabel) component).setIcon(FileExplorer.folderIcon);
            } else {
                ((JLabel) component).setIcon(FileExplorer.fileIcon);
            }

            return component;
        } else if (value != null && value instanceof String) {
            // Set the Size column as right aligned.
            ((JLabel) component).setHorizontalAlignment(JLabel.RIGHT);

            return component;
        }

        return null;
    }
}
