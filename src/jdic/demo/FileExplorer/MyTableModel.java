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

import javax.swing.table.AbstractTableModel;
import java.io.File;


/**
 * JDIC API demo class.
 * <p>
 * A redefined TableModel class.
 */

public class MyTableModel extends AbstractTableModel {
    // Object columnNames[] = {"Name", "Size", "Type", "Modified"};

    Object columnNamesFile[] = {"Name", "Size", "Type", "Modified"};
    // ===
    // For Windows "My Computer" node only.
    // ===
    Object columnNamesMyComputer[] = {"Name", "Type", "Size", "FreeSpace"};
    // Object columnNames[] = getColumnNames();
    Object columnNames[] = columnNamesFile;

    Object data[][] = getTableData();

    public int getRowCount() {
        return (data == null) ? 0 : data.length;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    // Make the table uneditable.
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    // ===
    // For Windows "My Computer" node only.
    // ===
    public void setColumnNames() {
        columnNames = getColumnNames();
    }

    public void setTableData() {
        data = getTableData();
    }

    // Gets the column names
    private Object[] getColumnNames() {
        MyTreeNode selectedTreeNode = FileExplorer.selectedTreeNode;

        if (selectedTreeNode == null) {
            return null;
        }

        File selectedDir = (File) selectedTreeNode.getUserObject();

        if (selectedDir.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
            return columnNamesMyComputer;
        } else {
            return columnNamesFile;
        }
    }

    // Gets the table data from the left tree.
    private Object[][] getTableData() {
        MyTreeNode selectedTreeNode = FileExplorer.selectedTreeNode;

        if (selectedTreeNode == null) {
            return null;
        }

        File selectedDir = (File) selectedTreeNode.getUserObject();

        // ===
        // For Windows "My Computer" node only.
        // ===
        if (selectedDir.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
            File[] drivers = MyUtility.getRoots();
            int driverNum = drivers.length;
            Object data[][] = new Object[driverNum][columnNames.length];

            // Remove A: drive from the initial list of drives, since whenever the
            // JTree is repaint, it tries to read floppy drive A:\.
            int firstDriverNum = 0;

            if (drivers[firstDriverNum].getPath().toLowerCase().startsWith("a:")) {
                firstDriverNum = 1;
            }
      
            int curDriverNum = 0;

            for (int i = firstDriverNum; i < driverNum; i++) {
                data[curDriverNum][0] = new DiskObject(drivers[i].getAbsolutePath(),
                        DiskObject.TYPE_DRIVER);
                data[curDriverNum][1] = "";
                data[curDriverNum][2] = " " + DiskObject.TYPE_DRIVER;
                data[curDriverNum][3] = (new java.util.Date(drivers[i].lastModified())).toString();
                curDriverNum++;
            }

            return data;
        } else {

            File[] files = selectedDir.listFiles();

            // The selected dir or driver might have no children.
            if (files == null) {
                return null;
            }

            int fileNum = files.length;
            Object data[][] = new Object[fileNum][columnNames.length];
            int curFileNum = 0;

            // Filter out the files and put ahead of the directories.
            for (int i = 0; i < fileNum; i++) {
                File file = files[i];

                if (!file.isDirectory()) {
                    data[curFileNum][0] = new DiskObject(file.getName(),
                            DiskObject.TYPE_FILE);
                    data[curFileNum][1] = MyUtility.length2KB(file.length());
                    data[curFileNum][2] = " " + DiskObject.TYPE_FILE;
                    data[curFileNum][3] = (new java.util.Date(file.lastModified())).toString();
                    curFileNum++;
                }
            }

            for (int i = 0; i < fileNum; i++) {
                File file = files[i];

                if (file.isDirectory()) {
                    data[curFileNum][0] = new DiskObject(file.getName(),
                            DiskObject.TYPE_FOLDER);
                    data[curFileNum][1] = "";
                    data[curFileNum][2] = " " + DiskObject.TYPE_FOLDER;
                    data[curFileNum][3] = (new java.util.Date(file.lastModified())).toString();
                    curFileNum++;
                }
            }

            return data;
        }
    }
}
