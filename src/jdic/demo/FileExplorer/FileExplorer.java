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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.TableColumn;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.io.IOException;
import java.io.File;

import org.jdesktop.jdic.desktop.*;


/**
 * JDIC API demo main class.
 * <p>
 * The main class for the demo. Class <code>FileExplorer</code> creates a UI 
 * interface demonstrating the usage of the public API of <code>org.jdesktop.jdic.desktop.*
 * </code> classes.
 */
public class FileExplorer extends JPanel {
    public static ImageIcon computerIcon;
    public static ImageIcon driverIcon;
    public static ImageIcon folderIcon;
    public static ImageIcon fileIcon;
    public static ImageIcon browseIcon;

    public static String MY_COMPUTER_FOLDER_PATH = System.getProperty("java.io.tmpdir")
            + File.separator + "My Computer";

    // Currently selected tree node in the left disk tree.
    public static MyTreeNode selectedTreeNode = null;
    // Currently selected file in the right table.
    File selectedFile = null;

    BorderLayout borderLayout1 = new BorderLayout();
    JPopupMenu jDesktopPopupMenu = new JPopupMenu();
    JMenuItem jMenuItemOpen = new JMenuItem();
    JMenuItem jMenuItemEdit = new JMenuItem();
    JMenuItem jMenuItemPrint = new JMenuItem();
    JMenuItem jMenuItemBrowse = new JMenuItem();
    JMenuItem jMenuItemMail = new JMenuItem();

    JSplitPane jSplitPane = new JSplitPane();
    JTree jTreeDisk = new JTree(createTreeModel());

    JScrollPane tableScrollPane = new JScrollPane();
    MyTableModel tableModel = new MyTableModel();
    MyTable jTable = new MyTable();

    JPanel jAddressPanel = new JPanel();
    JLabel jAddressLabel = new JLabel();
    JTextField jAddressTextField = new JTextField();
    JButton jBrowseButton = new JButton();

    MyStatusBar statusBar = new MyStatusBar();

    public FileExplorer() {
        try {
            System.setSecurityManager(null);
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("JDIC API Demo - FileExplorer");

        Container contentPane = frame.getContentPane();

        contentPane.setLayout(new GridLayout(1, 1));
        contentPane.add(new FileExplorer());

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void jbInit() throws Exception {
        try {
            computerIcon = new ImageIcon(FileExplorer.class.getResource("images/Computer.gif"));
            driverIcon = new ImageIcon(FileExplorer.class.getResource("images/Driver.gif"));
            folderIcon = new ImageIcon(FileExplorer.class.getResource("images/Folder.gif"));
            fileIcon = new ImageIcon(FileExplorer.class.getResource("images/File.gif"));
            browseIcon = new ImageIcon(FileExplorer.class.getResource("images/Right.gif"));
        } catch (Exception e) {
            System.out.println("ERROR loading image files !");
        }

        this.setLayout(borderLayout1);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.setPreferredSize(new Dimension(screenSize.width * 4 / 5,
                screenSize.height * 7 / 10));

        jMenuItemOpen.setText("Open");
        jMenuItemOpen.addActionListener(new
                FileExplorer_jMenuItemOpen_actionAdapter(this));
        jMenuItemEdit.setText("Edit");
        jMenuItemEdit.addActionListener(new
                FileExplorer_jMenuItemEdit_actionAdapter(this));
        jMenuItemPrint.setText("Print");
        jMenuItemPrint.addActionListener(new
                FileExplorer_jMenuItemPrint_actionAdapter(this));
        jMenuItemBrowse.setText("Browse");
        jMenuItemBrowse.addActionListener(new
                FileExplorer_jMenuItemBrowse_actionAdapter(this));
        jMenuItemMail.setText("Mail to ...");
        jMenuItemMail.addActionListener(new
                FileExplorer_jMenuItemMail_actionAdapter(this));

        jTreeDisk.addTreeExpansionListener(new
                FileExplorer_jTreeDisk_treeExpansionAdapter(this));
        jAddressLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        jAddressLabel.setToolTipText("");
        jAddressLabel.setText("Address");

        jBrowseButton.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0,
                2, 0, 2),
                new EtchedBorder()));
        jBrowseButton.setToolTipText("Browse the given URL with system default browser");
        jBrowseButton.setIcon(browseIcon);
        jBrowseButton.setText("Browse ");
        jBrowseButton.addActionListener(new
                FileExplorer_jBrowseButton_actionAdapter(this));
        jAddressPanel.setLayout(new BorderLayout());

        jAddressTextField.addActionListener(new
                FileExplorer_jAddressTextField_actionAdapter(this));

        jAddressPanel.add(jAddressLabel, BorderLayout.WEST);
        jAddressPanel.add(jAddressTextField, BorderLayout.CENTER);
        jAddressPanel.add(jBrowseButton, BorderLayout.EAST);
        jAddressPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        jTreeDisk.addMouseListener(new FileExplorer_jTreeDisk_mouseAdapter(this));
        jTreeDisk.addTreeWillExpandListener(new
                FileExplorer_jTreeDisk_treeWillExpandAdapter(this));
        jTreeDisk.setCellRenderer(new MyTreeRenderer());
        jTreeDisk.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTreeDisk.setSelectionRow(0);
        jTreeDisk.setBackground(Color.white);
        jTreeDisk.setAlignmentX((float) 0.5);
        jTreeDisk.setShowsRootHandles(false);
        jTreeDisk.addTreeSelectionListener(new
                FileExplorer_jTreeDisk_treeSelectionAdapter(this));

        jTable.setBorder(null);
        jTable.setModel(tableModel);
        TableColumn column = jTable.getColumnModel().getColumn(0);

        column.setCellRenderer(new MyTableRenderer());
        column = jTable.getColumnModel().getColumn(1);
        column.setCellRenderer(new MyTableRenderer());

        jTable.setShowHorizontalLines(false);
        jTable.setShowVerticalLines(false);
        jTable.addMouseListener(new FileExplorer_jTable_mouseAdapter(this));

        tableScrollPane.setViewportView(jTable);

        jSplitPane.setBorder(null);
        jSplitPane.add(new JScrollPane(jTreeDisk), JSplitPane.LEFT);
        jSplitPane.add(tableScrollPane, JSplitPane.RIGHT);

        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        statusBar.lblDesc.setText("JDIC API Demo - FileExplorer");

        this.add(jAddressPanel, BorderLayout.NORTH);
        this.add(jSplitPane, BorderLayout.CENTER);
        this.add(statusBar, BorderLayout.SOUTH);
    }
    

    private DefaultTreeModel createTreeModel() {
        // Using "My Computer" as root.
        MyTreeNode rootNode = null;

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("windows")) {
            // Create a temp "My Computer" folder.
            File MY_COMPUTER_FOLDER_FILE = new File(MY_COMPUTER_FOLDER_PATH);

            MY_COMPUTER_FOLDER_FILE.mkdirs();
            // Delete temp file when program exits.
            MY_COMPUTER_FOLDER_FILE.deleteOnExit();

            rootNode = new MyTreeNode(MY_COMPUTER_FOLDER_FILE);

            MyTreeNode parent;
            File[] roots = MyUtility.getRoots();

            // Remove A: drive from the initial list of drives, since whenever the
            // JTree is repaint, it tries to read floppy drive A:\.
            // for (int i = 0; i < roots.length; i++) {
            int readfirst = 0;

            if (roots[readfirst].getPath().toLowerCase().startsWith("a:")) {
                readfirst = 1;
            }
            for (int i = readfirst; i < roots.length; i++) {
                parent = new MyTreeNode(roots[i]);

                parent.explore();

                rootNode.add(parent);
            }

            // By default, use the first non-floppy disk here.
            selectedTreeNode = rootNode;
            selectedFile = null;
        } else if (osName.startsWith("linux") || osName.startsWith("sunos") 
            || osName.startsWith("freebsd") || osName.startsWith("mac os")) {
            File rootFile = new File("/");

            rootNode = new MyTreeNode(rootFile);

            rootNode.explore();
            selectedTreeNode = (MyTreeNode) rootNode;
            selectedFile = selectedTreeNode.getFile();
        } else {
            throw new UnsupportedOperationException("The demo is not "
                    + "supported on this platform.");
        }

        return new DefaultTreeModel(rootNode);
    }

    void jTreeDisk_valueChanged(TreeSelectionEvent e) {
        selectedTreeNode = (MyTreeNode)
                jTreeDisk.getLastSelectedPathComponent();

        selectedFile = null;

        // Refresh the address field.
        if (selectedTreeNode != null) {
            File selectedDir = (File) selectedTreeNode.getUserObject();

            // Update the address text field and status bar.
            updateStatusInfo();
        }

        // Refresh the file table.
        tableModel.setColumnNames();
        tableModel.setTableData();
        jTable.repaint();       

        // Revalidate the JScrollPane component for the changed JTable.
        // Display the scrollbar if necessary.
        tableScrollPane.revalidate();         
    }

    void jTreeDisk_treeExpanded(TreeExpansionEvent e) {
        DefaultTreeModel treeModel = (DefaultTreeModel) jTreeDisk.getModel();
        TreePath newPath = new TreePath(treeModel.getPathToRoot(selectedTreeNode));

        jTreeDisk.setSelectionPath(newPath);
        jTreeDisk.scrollPathToVisible(newPath);
    }

    void jTreeDisk_mouseClicked(MouseEvent e) {
        // Enable both left click and right click selection. Left click selection is
        // auto-enabled, and doesn't need to be enabled specifically.
        TreePath curTreePath = jTreeDisk.getClosestPathForLocation(e.getX(),
                e.getY());

        jTreeDisk.clearSelection();
        jTreeDisk.addSelectionPath(curTreePath);
    }

    void jTreeDisk_treeWillExpand(TreeExpansionEvent e) throws
            ExpandVetoException {

        TreePath path = e.getPath();
        MyTreeNode selectedNode = (MyTreeNode) path.getLastPathComponent();

        if (!selectedNode.isExplored()) {
            selectedNode.explore();
        }
    }

    /**
     * Explores the directory specified by the parent tree node in the left tree
     * and the selected subdirectory in the right table.
     */
    private void exploreDirectory(MyTreeNode parentTreeNode,
            File selectedSubDir) {
        if (!parentTreeNode.isExplored()) {
            parentTreeNode.explore();
        }

        int count = jTreeDisk.getModel().getChildCount(parentTreeNode);

        for (int i = 0; i < count; i++) {
            Object oneChild = jTreeDisk.getModel().getChild(parentTreeNode, i);

            if (oneChild instanceof MyTreeNode) {
                File file = (File) ((MyTreeNode) oneChild).getUserObject();

                if (file.equals(selectedSubDir)) {
                    selectedTreeNode = (MyTreeNode) oneChild;
                    break;
                }
            }
        }

        TreePath newPath = new TreePath(selectedTreeNode.getPath());

        if (jTreeDisk.isExpanded(newPath)) {
            // if the new path is already expanded, just select it.
            jTreeDisk.setSelectionPath(newPath);
            jTreeDisk.scrollPathToVisible(newPath);
        } else {
            jTreeDisk.expandPath(newPath);
        }
    }

    /**
     * Expands the tree to the given path. 
     */
    private void expandPaths(JTree tree, List paths) {
        Iterator iter = paths.iterator();

        if (!iter.hasNext()) {
            return;
        }

        MyTreeNode parentNode = (MyTreeNode) tree.getModel().getRoot();

        if (!parentNode.isExplored()) {
            parentNode.explore();
        }

        // ===
        // For Windows "My Computer" node only.
        // ===
        // Ignore the root node "My Computer", since the path for this node
        // is not in the path list of the expanded node.
        File parentFile = (File) ((MyTreeNode) parentNode).getUserObject();

        if (parentFile.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
            int count = jTreeDisk.getModel().getChildCount(parentNode);
            boolean pathNotFound = true;

            for (int i = 0; i < count; i++) {
                Object oneChild = jTreeDisk.getModel().getChild(parentNode, i);
                String onePath = ((MyTreeNode) oneChild).toString();

                if (onePath.equalsIgnoreCase((String) iter.next())) {
                    parentNode = (MyTreeNode) oneChild;
                    pathNotFound = false;
                    break;
                }
            }
        } else {
            if (!parentFile.equals((String) iter.next())) {
                return;
            }
        }

        boolean pathNotFound = false;

        while (iter.hasNext() && !pathNotFound) {
            if (!parentNode.isExplored()) {
                parentNode.explore();
            }

            String nextPath = (String) iter.next();

            pathNotFound = true;
            int count = jTreeDisk.getModel().getChildCount(parentNode);

            for (int i = 0; i < count; i++) {
                Object oneChild = jTreeDisk.getModel().getChild(parentNode, i);
                String onePath = ((MyTreeNode) oneChild).toString();

                if (onePath.equalsIgnoreCase(nextPath)) {
                    parentNode = (MyTreeNode) oneChild;
                    pathNotFound = false;
                    break;
                }
            }
        }

        if (pathNotFound) {
            return;
        } else {
            selectedTreeNode = parentNode;
            TreePath newPath = new TreePath(selectedTreeNode.getPath());

            if (jTreeDisk.isExpanded(newPath)) {
                // if the new path is already expanded, just select it.
                jTreeDisk.setSelectionPath(newPath);
                jTreeDisk.scrollPathToVisible(newPath);
            } else {
                jTreeDisk.expandPath(newPath);
            }
        }
    }

    /**
     * Explores the specified directory by expanding the right tree to it path,
     * and display it's subdirectories and files in the right table.
     */
    private void exploreDirectory(File selectedDir) {
        // First parse the given directory path into separate path names/fields.
        List paths = new ArrayList();
        String selectedAbsPath = selectedDir.getAbsolutePath();
        int beginIndex = 0;
        int endIndex = selectedAbsPath.indexOf(File.separator);

        // For the first path name, attach the path separator.
        // For Windows, it should be like 'C:\', for Unix, it should be like '/'.
        paths.add(selectedAbsPath.substring(beginIndex, endIndex + 1));
        beginIndex = endIndex + 1;
        endIndex = selectedAbsPath.indexOf(File.separator, beginIndex);
        while (endIndex != -1) {
            // For other path names, do not attach the path separator.
            paths.add(selectedAbsPath.substring(beginIndex, endIndex));
            beginIndex = endIndex + 1;
            endIndex = selectedAbsPath.indexOf(File.separator, beginIndex);
        }
        String lastPath = selectedAbsPath.substring(beginIndex,
                selectedAbsPath.length());

        if ((lastPath != null) && (lastPath.length() != 0)) {
            paths.add(lastPath);
        }

        expandPaths(jTreeDisk, paths);
    }

    /**
     * Updates the status info including the address text field and status bar.
     *
     * It should be called in below cases:
     *   the selected node in the left tree changes;
     *   click on one item in the right file table;
     *   the current status changes from STATUS_FILEEXPLORER to "WebBrowse".
     */
    private void updateStatusInfo() {
        File selectedDir = (File) selectedTreeNode.getUserObject();

        if (selectedDir.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
            // ===
            // For Windows "My Computer" node only.
            // ===
            if (selectedFile == null) {
                jAddressTextField.setText("");
                statusBar.lblObject.setText(selectedTreeNode.getChildrenCount()
                        + " object(s)");
            } else {
                jAddressTextField.setText(selectedFile.getPath());
                statusBar.lblObject.setText("1 object(s) selected");
            }

            statusBar.lblSize.setText("0 Bytes");
        } else {
            if (selectedFile == null) {
                jAddressTextField.setText(selectedDir.getPath());

                statusBar.lblObject.setText(selectedTreeNode.getChildrenCount()
                        + " object(s)");
                statusBar.lblSize.setText(MyUtility.length2KB(selectedTreeNode.getSize()));
            } else {
                jAddressTextField.setText(selectedFile.getPath());

                statusBar.lblObject.setText("1 object(s) selected");
                statusBar.lblSize.setText(MyUtility.length2KB(selectedFile.length()));
            }
        }
    }

    void jTable_maybePopUpMenu(MouseEvent e) {
        if (e.isPopupTrigger() == false || selectedFile == null) {
            return;
        }

        if (!selectedFile.isDirectory()) {
            // For a selected file, all the menu items are visible.
            // Check the availability of the menu items.
            jDesktopPopupMenu.removeAll();
            jDesktopPopupMenu.add(jMenuItemOpen);
            jDesktopPopupMenu.add(jMenuItemEdit);
            jDesktopPopupMenu.add(jMenuItemPrint);
            jDesktopPopupMenu.addSeparator();
            jDesktopPopupMenu.add(jMenuItemBrowse);
            jDesktopPopupMenu.addSeparator();
            jDesktopPopupMenu.add(jMenuItemMail);

            if (Desktop.isEditable(selectedFile)) {
                jMenuItemEdit.setEnabled(true);
            } else {
                jMenuItemEdit.setEnabled(false);
            }

            if (Desktop.isPrintable(selectedFile)) {
                jMenuItemPrint.setEnabled(true);
            } else {
                jMenuItemPrint.setEnabled(false);
            }

            jDesktopPopupMenu.show((Component) jTable, e.getX(), e.getY());
        } else {
            // For a selected directory, only "Open", "Browse" and "Browser in New
            // Window" items are visible.
            jDesktopPopupMenu.removeAll();
            jDesktopPopupMenu.add(jMenuItemOpen);
            jDesktopPopupMenu.addSeparator();
            jDesktopPopupMenu.add(jMenuItemBrowse);

            jDesktopPopupMenu.show((Component) jTable, e.getX(), e.getY());
        }
    }

    void jTable_mouseClicked(MouseEvent e) {
        int curRow = jTable.rowAtPoint(new Point(e.getX(), e.getY()));

        if (curRow == -1) {
            selectedFile = null;
            updateStatusInfo();
            jTable.clearSelection();
            return;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            // Enable right click selection. Left click selection is auto-enabled,
            // and doesn't need to be enabled specifically.
            curRow = jTable.rowAtPoint(new Point(e.getX(), e.getY()));
            jTable.clearSelection();
            jTable.addRowSelectionInterval(curRow, curRow);
        }

        ListSelectionModel lsm = jTable.getSelectionModel();
        int selectedRow = lsm.getMinSelectionIndex();

        DiskObject selectedObject = (DiskObject) tableModel.getValueAt(selectedRow,
                0);
        File selectedDir = (File) selectedTreeNode.getUserObject();

        if (selectedDir.equals(new File(FileExplorer.MY_COMPUTER_FOLDER_PATH))) {
            // ===
            // For Windows "My Computer" node only.
            // ===
            selectedFile = new File(selectedObject.name);
        } else {
            selectedFile = new File(selectedDir.toString() + File.separator
                    + selectedObject.name);
        }

        // Update the address text field and status bar.
        updateStatusInfo();

        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
            // For a left and double click, open the file or directory.
            // Notice!!!. Below code is duplicate of
            // void jMenuItemOpen_actionPerformed(ActionEvent e) {}
            // Popup the context menu.
            if (!selectedFile.isDirectory()) {
                try {
                    Desktop.open(selectedFile);
                } catch (DesktopException de) {
                    JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // The selection is a directory, open it.
                exploreDirectory(selectedTreeNode, selectedFile);
            }
        }
    }

    void jMenuItemOpen_actionPerformed(ActionEvent e) {
        if (!selectedFile.isDirectory()) {
            try {
                Desktop.open(selectedFile);
            } catch (DesktopException de) {
                JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // The selection is a directory, open it.
            exploreDirectory(selectedTreeNode, selectedFile);
        }
    }

    void jMenuItemEdit_actionPerformed(ActionEvent e) {
        try {
            Desktop.edit(selectedFile);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jMenuItemPrint_actionPerformed(ActionEvent e) {
        try {
            Desktop.print(selectedFile);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jMenuItemBrowse_actionPerformed(ActionEvent e) {
        URL fileURL = null;

        try {
            fileURL = selectedFile.toURL();
        } catch (MalformedURLException mue) {
            JOptionPane.showMessageDialog(this, mue.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        try {
            Desktop.browse(fileURL);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jMenuItemMail_actionPerformed(ActionEvent e) {
        Message msg = new Message();
        List attachList = new ArrayList();

        attachList.add(selectedFile.getPath());
        try {
            msg.setAttachments(attachList);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        try {
            Desktop.mail(msg);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jBrowseButton_actionPerformed(ActionEvent e) {
        String addrValue = jAddressTextField.getText();
        String addrValueTrim;

        if ((addrValue != null) && addrValue.trim() != null) {
            addrValueTrim = addrValue.trim();

            URL addressURL = address2URL(addrValueTrim);

            if (addressURL == null) {
                JOptionPane.showMessageDialog(this,
                        "Cannot find '" + addrValueTrim
                        + "'. Make sure the path or Internet address is correct.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Desktop.browse(addressURL);
                } catch (DesktopException de) {
                    JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Return a local File object if the given address string is a local file path.
    // If else, null is returned.
    File address2LocalFile(String address) {
        // Check if the text value is a local path by checking if it starts
        // with a driver name(on Windows) or root path(on Unix).
        File localFile = null;
        File[] roots = MyUtility.getRoots();

        for (int i = 0; i < roots.length; i++) {
            if (address.toLowerCase().startsWith(roots[i].toString().toLowerCase())) {
                localFile = new File(address);
                break;
            }
        }

        return localFile;
    }

    // Return an URL object if the given address string could be converted to
    // an URL. If else, null is returned.
    URL address2URL(String address) {
        URL tempUrl = null;

        try {
            File localFile = address2LocalFile(address);

            if (localFile != null) {
                tempUrl = localFile.toURL();
            } else {
                // Check if the text value is a valid URL.
                try {
                    tempUrl = new URL(address);
                } catch (MalformedURLException e) {
                    if (address.toLowerCase().startsWith("ftp.")) {
                        tempUrl = new URL("ftp://" + address);
                    } else if (address.toLowerCase().startsWith("gopher.")) {
                        tempUrl = new URL("gopher://" + address);
                    } else {
                        tempUrl = new URL("http://" + address);
                    }
                }
            }
        } catch (MalformedURLException mue) {}

        return tempUrl;
    }

    void jAddressTextField_actionPerformed(ActionEvent e) {
        String addrValue = jAddressTextField.getText();

        if ((addrValue == null) || addrValue.trim() == null) {
            return;
        }

        String addrValueTrim = addrValue.trim();

        // Check if the address string is a local file path.
        File localFile = address2LocalFile(addrValueTrim);

        if (localFile != null) {
            if (localFile.isFile()) {
                // It's a local path to a file. Open it.
                try {
                    Desktop.open(localFile);
                } catch (DesktopException de) {
                    JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (localFile.isDirectory()) {
                exploreDirectory(localFile);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Cannot find '" + addrValue
                        + "'. Make sure the path or Internet address is correct.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Check if the address string value is a URL string.
            URL tempUrl = address2URL(addrValueTrim);

            if (tempUrl == null) {
                JOptionPane.showMessageDialog(this,
                        "Cannot find '" + addrValue
                        + "'. Make sure the path or Internet address is correct.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Desktop.browse(tempUrl);
                } catch (DesktopException de) {
                    JOptionPane.showMessageDialog(this, de.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}


class FileExplorer_jTreeDisk_treeSelectionAdapter
        implements javax.swing.event.TreeSelectionListener {
    FileExplorer adaptee;

    FileExplorer_jTreeDisk_treeSelectionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void valueChanged(TreeSelectionEvent e) {
        adaptee.jTreeDisk_valueChanged(e);
    }
}


class FileExplorer_jTable_mouseAdapter extends java.awt.event.MouseAdapter {
    FileExplorer adaptee;

    FileExplorer_jTable_mouseAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.jTable_mouseClicked(e);
    }

    public void mousePressed(MouseEvent e) {
        adaptee.jTable_maybePopUpMenu(e);
    }

    public void mouseReleased(MouseEvent e) {
        adaptee.jTable_maybePopUpMenu(e);
    }
}


class FileExplorer_jTreeDisk_treeExpansionAdapter implements javax.swing.event.TreeExpansionListener {
    FileExplorer adaptee;

    FileExplorer_jTreeDisk_treeExpansionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void treeExpanded(TreeExpansionEvent e) {
        adaptee.jTreeDisk_treeExpanded(e);
    }

    public void treeCollapsed(TreeExpansionEvent e) {}
}


class FileExplorer_jMenuItemOpen_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jMenuItemOpen_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemOpen_actionPerformed(e);
    }
}


class FileExplorer_jMenuItemEdit_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jMenuItemEdit_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemEdit_actionPerformed(e);
    }
}


class FileExplorer_jMenuItemPrint_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jMenuItemPrint_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemPrint_actionPerformed(e);
    }
}


class FileExplorer_jMenuItemBrowse_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jMenuItemBrowse_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemBrowse_actionPerformed(e);
    }
}


class FileExplorer_jMenuItemMail_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jMenuItemMail_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMenuItemMail_actionPerformed(e);
    }
}


class FileExplorer_jBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jBrowseButton_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jBrowseButton_actionPerformed(e);
    }
}


class FileExplorer_jTreeDisk_mouseAdapter extends java.awt.event.MouseAdapter {
    FileExplorer adaptee;

    FileExplorer_jTreeDisk_mouseAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.jTreeDisk_mouseClicked(e);
    }
}


class FileExplorer_jTreeDisk_treeWillExpandAdapter implements javax.swing.event.TreeWillExpandListener {
    FileExplorer adaptee;

    FileExplorer_jTreeDisk_treeWillExpandAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        adaptee.jTreeDisk_treeWillExpand(e);
    }

    public void treeWillCollapse(TreeExpansionEvent e) {}
}


/**
 * Below class is used to eliminate the grey area of the scroll pane
 * that is not filled in the table.
 */
class MyTable extends JTable implements Scrollable {
    public boolean getScrollableTracksViewportHeight() {
        Component parent = getParent();

        if (parent instanceof JViewport) {
            return parent.getHeight() > getPreferredSize().height;
        }

        return false;
    }
}


class FileExplorer_jAddressTextField_actionAdapter implements java.awt.event.ActionListener {
    FileExplorer adaptee;

    FileExplorer_jAddressTextField_actionAdapter(FileExplorer adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jAddressTextField_actionPerformed(e);
    }
}
