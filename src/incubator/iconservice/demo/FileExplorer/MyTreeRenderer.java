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

import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import javax.swing.JTree;
import java.io.File;


/**
 * JDIC API demo class.
 * <p>
 * A redefined TreeCellRenderer class.
 */

public class MyTreeRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean isSelected, boolean isExpanded,
            boolean leaf, int row, boolean hasFocus) {
        Component component = super.getTreeCellRendererComponent(tree, value,
                isSelected, isExpanded, leaf, row, hasFocus);

        if (value != null && value instanceof MyTreeNode) {
            MyTreeNode treeNode = (MyTreeNode) value;
            // ===
            // For Windows "My Computer" node only.
            // ===
            File selectedDir = (File) treeNode.getUserObject();

            if (selectedDir.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
                setIcon(FileExplorer.computerIcon);
            } else if (selectedDir.getParent() == null) {
                setIcon(FileExplorer.driverIcon);
            } else {
                setIcon(FileExplorer.folderIcon);
            }

            return component;
        }

        return this;
    }
}
