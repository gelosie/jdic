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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Iterator;

import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;
import org.jdesktop.jdic.filetypes.AssociationAlreadyRegisteredException;
import org.jdesktop.jdic.filetypes.AssociationNotRegisteredException;
import org.jdesktop.jdic.filetypes.RegisterFailedException;


/**
 * JDIC API demo main class.
 * <p>
 * The <code>FileTypes</code> class creates a dialog to retrieve/register/unregister
 * file type associations in user level or system level.
 */

public class FileTypes extends JDialog {
    final static int SELECT_REGISTER_SYSTEM = 1;
    final static int SELECT_REGISTER_USER = 2;
    final static int SELECT_UNREGISTER_SYSTEM = 3;
    final static int SELECT_UNREGISTER_USER = 4;
    final static int SELECT_GET_EXT = 5;
    final static int SELECT_GET_MIME = 6;
    final static int SELECT_NONE = 0;
    int selectOption = SELECT_NONE;

    ButtonGroup buttonOperationGroup = new ButtonGroup();
    DefaultListModel actionsListModel = new DefaultListModel();
    JPanel jAssociationContentPanel = new JPanel();
    JPanel jAssociationButtonPanel = new JPanel();
    JPanel jFieldsPanel = new JPanel();
    JPanel jOptionsPanel = new JPanel();
    JPanel jNoActionsFieldPanel = new JPanel();
    JPanel jActionsFieldPanel = new JPanel();
    JPanel jNewActionPanel = new JPanel();

    JLabel jDescriptionLabel = new JLabel();
    JLabel jNameLabel = new JLabel();
    JLabel jMimeTypeLabel = new JLabel();
    JLabel jFileExtensionListLabel = new JLabel();
    JLabel jIconFileLabel = new JLabel();

    JTextField jDescriptionTextField = new JTextField();
    JTextField jNameTextField = new JTextField();
    JTextField jMimeTypeTextField = new JTextField();
    JTextField jFileExtensionListTextField = new JTextField();
    JTextField jIconFileTextField = new JTextField();

    JLabel jActionsLabel = new JLabel();
    JTextField jNewActionTextField = new JTextField();
    JButton jAddNewActionButton = new JButton();
    JList jActionsList = new JList(actionsListModel);

    JRadioButton jRadioButtonUnregisterUser = new JRadioButton();
    JRadioButton jRadioButtonRegisterUser = new JRadioButton();
    JRadioButton jRadioButtonRegisterSys = new JRadioButton();
    JRadioButton jRadioButtonUnregisterSys = new JRadioButton();

    JButton jCancelButton = new JButton();
    JButton jApplyButton = new JButton();

    TitledBorder titledBorderOptions;
    TitledBorder titledBorderFields;

    JPanel jPanelGetByExt = new JPanel();
    JRadioButton jRadioButtonGetExt = new JRadioButton();
    JTextField jTextFieldGetExt = new JTextField();
    JPanel jPanelGetByMime = new JPanel();
    JRadioButton jRadioButtonGetMime = new JRadioButton();
    JTextField jTextFieldGetMime = new JTextField();
    JPanel jPanelGetExt = new JPanel();
    JPanel jPanelGetMime = new JPanel();

    public FileTypes() throws HeadlessException {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws HeadlessException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        FileTypes FileTypes = new FileTypes();

        FileTypes.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        FileTypes.setVisible(true);
    }

    private void jbInit() throws Exception {
        this.setResizable(false);
        this.setTitle("JDIC API Demo - File Associations");
        this.setSize(800, 600);

        // Center the dialog.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dlgSize = this.getSize();

        if (dlgSize.height > screenSize.height) {
            dlgSize.height = screenSize.height;
        }
        if (dlgSize.width > screenSize.width) {
            dlgSize.width = screenSize.width;
        }
        this.setLocation((screenSize.width - dlgSize.width) / 2,
                (screenSize.height - dlgSize.height) / 2);

        this.getContentPane().setLayout(new BorderLayout());
        titledBorderOptions = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140)),
                "Association Operations:");
        titledBorderFields = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,
                new Color(148, 145, 140)),
                "Association Fields:");

        jOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        jOptionsPanel.setLayout(new GridLayout(10, 1));
        jOptionsPanel.setBorder(titledBorderOptions);
        jRadioButtonRegisterSys.setText("Register System-level Association");
        jRadioButtonRegisterUser.setText("Register User-level Association");
        jRadioButtonUnregisterSys.setText("Unregister System-level Association");
        jRadioButtonUnregisterUser.setText("Unregister User-level Association");
        jDescriptionLabel.setText("Description:");
        jNameLabel.setText("MIME File Name:");
        jFileExtensionListLabel.setText("File Extension List:");
        jMimeTypeLabel.setText("Mime Type:");
        jIconFileLabel.setText("Icon File:");
        jNoActionsFieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jNoActionsFieldPanel.setLayout(new GridLayout(10, 1));
        jRadioButtonGetExt.setText("Get Association by File Extension");
        jRadioButtonGetExt.addItemListener(new FileTypes_jRadioButtonGetExt_itemAdapter(this));
        jTextFieldGetExt.setPreferredSize(new Dimension(40, 22));
        jTextFieldGetExt.setText("");
        jTextFieldGetExt.setEnabled(false);
        jTextFieldGetExt.setHorizontalAlignment(SwingConstants.LEADING);
        jRadioButtonGetMime.setText("Get Association by Mime Type");
        jRadioButtonGetMime.addItemListener(new FileTypes_jRadioButtonGetMime_itemAdapter(this));
        jTextFieldGetMime.setText("");
        jTextFieldGetMime.setEnabled(false);
        jNoActionsFieldPanel.add(jDescriptionLabel, null);
        jNoActionsFieldPanel.add(jDescriptionTextField, null);
        jNoActionsFieldPanel.add(jNameLabel, null);
        jNoActionsFieldPanel.add(jNameTextField, null);
        jNoActionsFieldPanel.add(jMimeTypeLabel, null);
        jNoActionsFieldPanel.add(jMimeTypeTextField, null);
        jNoActionsFieldPanel.add(jFileExtensionListLabel, null);
        jNoActionsFieldPanel.add(jFileExtensionListTextField, null);
        jNoActionsFieldPanel.add(jIconFileLabel, null);
        jNoActionsFieldPanel.add(jIconFileTextField, null);

        jActionsLabel.setText("Actions:");
        jAddNewActionButton.setText("Add");
        jAddNewActionButton.addActionListener(
            new FileTypes_jAddNewActionButton_actionAdapter(this));
        jNewActionPanel.setLayout(new BorderLayout());
        jNewActionTextField.setText("");
        jNewActionPanel.add(jNewActionTextField, BorderLayout.CENTER);
        jNewActionPanel.add(jAddNewActionButton, BorderLayout.EAST);
        jActionsFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jActionsFieldPanel.setLayout(new BorderLayout());
        jActionsFieldPanel.add(jActionsLabel, BorderLayout.NORTH);
        jActionsFieldPanel.add(jNewActionPanel, BorderLayout.CENTER);
        jActionsFieldPanel.add(new JScrollPane(jActionsList), BorderLayout.SOUTH);

        jFieldsPanel.setBorder(titledBorderFields);
        jFieldsPanel.setLayout(new BorderLayout());
        jFieldsPanel.add(jNoActionsFieldPanel, BorderLayout.CENTER);
        jFieldsPanel.add(jActionsFieldPanel, BorderLayout.SOUTH);
        jRadioButtonRegisterSys.addActionListener(
            new FileTypes_jRadioButtonRegisterSys_actionAdapter(this));
        jRadioButtonRegisterUser.addActionListener(
            new FileTypes_jRadioButtonRegisterUser_actionAdapter(this));
        jRadioButtonUnregisterSys.addActionListener(
            new FileTypes_jRadioButtonUnregisterSys_actionAdapter(this));
        jRadioButtonUnregisterUser.addActionListener(
            new FileTypes_jRadioButtonUnregisterUser_actionAdapter(this));
        buttonOperationGroup.add(jRadioButtonUnregisterUser);
        buttonOperationGroup.add(jRadioButtonUnregisterSys);
        buttonOperationGroup.add(jRadioButtonRegisterUser);
        buttonOperationGroup.add(jRadioButtonRegisterSys);

        jOptionsPanel.add(jRadioButtonRegisterSys, null);
        jOptionsPanel.add(jRadioButtonRegisterUser, null);
        jOptionsPanel.add(jRadioButtonUnregisterSys, null);
        jOptionsPanel.add(jRadioButtonUnregisterUser, null);
        jOptionsPanel.add(jPanelGetByExt, null);
        jOptionsPanel.add(jPanelGetByMime, null);
        jPanelGetByExt.setLayout(new BorderLayout());
        jPanelGetByExt.add(jRadioButtonGetExt, BorderLayout.NORTH);
        jPanelGetByExt.add(jPanelGetExt, BorderLayout.CENTER);
        jPanelGetExt.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanelGetExt.setLayout(new BorderLayout());
        jPanelGetExt.add(jTextFieldGetExt, BorderLayout.CENTER);

        jPanelGetByMime.setLayout(new BorderLayout());
        jPanelGetByMime.add(jRadioButtonGetMime, BorderLayout.NORTH);
        jPanelGetByMime.add(jPanelGetMime, BorderLayout.CENTER);
        jPanelGetMime.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanelGetMime.setLayout(new BorderLayout());
        jPanelGetMime.add(jTextFieldGetMime, BorderLayout.CENTER);

        jAssociationContentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
            0, 5));
        jAssociationContentPanel.setLayout(new BorderLayout());
        jAssociationContentPanel.add(jOptionsPanel, BorderLayout.WEST);
        jAssociationContentPanel.add(jFieldsPanel, BorderLayout.CENTER);

        jCancelButton.setText("Cancel");
        jCancelButton.addActionListener(new FileTypes_jCancelButton_actionAdapter(this));
        jApplyButton.setText("Apply");
        jApplyButton.addActionListener(new FileTypes_jApplyButton_actionAdapter(this));
        jAssociationButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5,
            5, 5));
        jAssociationButtonPanel.add(jApplyButton, null);
        jAssociationButtonPanel.add(jCancelButton, null);

        this.getContentPane().add(jAssociationContentPanel, BorderLayout.CENTER);
        this.getContentPane().add(jAssociationButtonPanel, BorderLayout.SOUTH);

        buttonOperationGroup.add(jRadioButtonGetExt);
        buttonOperationGroup.add(jRadioButtonGetMime);
    }

    /**
     * Get the association fields specified by the user in the right panel.
     * And fill into the constructed Association object.
     */
    private Association getAssociationFields() {
        Association assoc = new Association();

        String description = jDescriptionTextField.getText();
        if ((description != null) && (description.length() != 0)) {
            assoc.setDescription(description);
        }

        String name = jNameTextField.getText();
        if ((name != null) && (name.length() != 0)) {
            assoc.setName(name);
        }

        String mimeType = jMimeTypeTextField.getText();
        if ((mimeType != null) && (mimeType.length() != 0)) {
            assoc.setMimeType(mimeType);
        }

        String fileExtensionListString = jFileExtensionListTextField.getText().trim();
        if ((fileExtensionListString != null)
                && (fileExtensionListString.length() != 0)) {
            String leftExtString = fileExtensionListString;
            int startIndex = 0;
            int nextSpacePos = fileExtensionListString.indexOf(' ');

            while (nextSpacePos != -1) {
                String oneExt = leftExtString.substring(startIndex, nextSpacePos);

                assoc.addFileExtension(oneExt);

                String tempString = leftExtString.substring(nextSpacePos,
                        leftExtString.length());

                leftExtString = tempString.trim();
                nextSpacePos = leftExtString.indexOf(' ');
            }

            if ((leftExtString != null) && (leftExtString.length()) != 0) {
                // one last file extension.
                assoc.addFileExtension(leftExtString);
            }
        }

        String iconFile = jIconFileTextField.getText();
        if ((iconFile != null) && (iconFile.length() != 0)) {
            assoc.setIconFileName(iconFile);
        }

        int actionNum = actionsListModel.getSize();
        if (actionNum != 0) {
            for (int i = 0; i < actionNum; i++) {
                String oneActionString = (String) actionsListModel.getElementAt(i);
                int firstSpacePos = oneActionString.indexOf(' ');
                String verb = oneActionString.substring(0, firstSpacePos);
                String leftStr = oneActionString.substring(firstSpacePos,
                        oneActionString.length());
                String command = leftStr.trim();
                Action oneAction = new Action(verb, command);

                assoc.addAction(oneAction);
            }
        }

        return assoc;
    }

    /**
     * Put the given association into the association fields in the right panel.
     */
    private void putAssociationFields(Association assoc) {
        jDescriptionTextField.setText(assoc.getDescription());

        jNameTextField.setText(assoc.getName());

        jMimeTypeTextField.setText(assoc.getMimeType());

        if (assoc.getFileExtList() == null) {
            jFileExtensionListTextField.setText(null);
        } else {
            Iterator extentionIter = assoc.getFileExtList().iterator();
            String fileExtensionListString = (String) extentionIter.next();

            while (extentionIter.hasNext()) {
                fileExtensionListString += ' ' + (String) extentionIter.next();
            }
            jFileExtensionListTextField.setText(fileExtensionListString);

        }

        jIconFileTextField.setText(assoc.getIconFileName());

        // Clear the old action list first before adding new actions.
        actionsListModel.removeAllElements();
        if (assoc.getActionList() != null) {
            Iterator actionIter = assoc.getActionList().iterator();

            while (actionIter.hasNext()) {
                Action oneAction = (Action) actionIter.next();
                String oneVerb = oneAction.getVerb();
                String oneCommand = oneAction.getCommand();

                String oneActionString = oneVerb + ' ' + oneCommand;

                actionsListModel.addElement(oneActionString);
            }
        }
    }

    void jAddNewActionButton_actionPerformed(ActionEvent e) {
        String newActionString = jNewActionTextField.getText();

        if ((newActionString != null) && (newActionString.length() != 0)
                && (newActionString.indexOf(' ') != -1)) {
            int firstSpacePos = newActionString.indexOf(' ');
            String verb = newActionString.substring(0, firstSpacePos);
            String command = newActionString.substring(firstSpacePos, newActionString.length()).trim();

            actionsListModel.insertElementAt (verb + ' ' + command, 0);
        } else {
            JOptionPane.showMessageDialog(this,
                    "A valid action string should be like: open C:\\temp\\notepad.exe.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    void jApplyButton_actionPerformed(ActionEvent e) {
        if (selectOption == SELECT_NONE) {
            JOptionPane.showMessageDialog(this,
                    "Please select an association operation in the left panel.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            AssociationService assocService = new AssociationService();

            try {
                switch (selectOption) {
                    case SELECT_REGISTER_SYSTEM: {
                            // Get specified association fields, and fill in the constructed Association object.
                            Association assoc = getAssociationFields();
                            assocService.registerSystemAssociation(assoc);
                        }
                        break;
    
                    case SELECT_REGISTER_USER: {
                            Association assoc = getAssociationFields();
                            assocService.registerUserAssociation(assoc);
                        }
                        break;
    
                    case SELECT_UNREGISTER_SYSTEM: {
                            Association assoc = getAssociationFields();
                            assocService.unregisterSystemAssociation(assoc);
                        }
                        break;
    
                    case SELECT_UNREGISTER_USER: {
                            Association assoc = getAssociationFields();
                            assocService.unregisterUserAssociation(assoc);
                        }
                        break;
    
                    case SELECT_GET_EXT: {
                            String inputFileExt = jTextFieldGetExt.getText();
                            Association assoc = null;
                            if ((inputFileExt != null) && (inputFileExt.length() != 0)) {
                                assoc = assocService.getFileExtensionAssociation(inputFileExt);
                            }
    
                            if (assoc == null) {
                                JOptionPane.showMessageDialog(this,
                                        "No association with the given file extension: "
                                        + inputFileExt,
                                        "Warning",
                                        JOptionPane.WARNING_MESSAGE);
                            } else {
                                putAssociationFields(assoc);
                            }
                        }
                        break;
    
                    case SELECT_GET_MIME: {
                            String inputMimeType = jTextFieldGetMime.getText();
                            Association assoc = null;
                            if ((inputMimeType != null) && (inputMimeType.length() != 0)) {                        
                                assoc = assocService.getMimeTypeAssociation(inputMimeType);
                            }
    
                            if (assoc == null) {
                                JOptionPane.showMessageDialog(this,
                                        "No association with the given MIME type: "
                                        + inputMimeType,
                                        "Warning",
                                        JOptionPane.WARNING_MESSAGE);
                            } else {
                                putAssociationFields(assoc);
                            }
                        }
                        break;
    
                    default: {
                            // The switch should never come here.
                            JOptionPane.showMessageDialog(this,
                                    "Demo bug: the demo should NEVER get here.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                } // end of switch
            } catch (AssociationAlreadyRegisteredException arException) {
                JOptionPane.showMessageDialog(this,
                    arException.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
            } catch (AssociationNotRegisteredException nrException) {
                JOptionPane.showMessageDialog(this,
                    nrException.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
            } catch (RegisterFailedException rfException) {
                JOptionPane.showMessageDialog(this,
                    rfException.toString(), "Exception", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void jCancelButton_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    void jRadioButtonRegisterSys_actionPerformed(ActionEvent e) {
        selectOption = SELECT_REGISTER_SYSTEM;
    }

    void jRadioButtonRegisterUser_actionPerformed(ActionEvent e) {
        selectOption = SELECT_REGISTER_USER;
    }

    void jRadioButtonUnregisterSys_actionPerformed(ActionEvent e) {
        selectOption = SELECT_UNREGISTER_SYSTEM;
    }

    void jRadioButtonUnregisterUser_actionPerformed(ActionEvent e) {
        selectOption = SELECT_UNREGISTER_USER;
    }

    void jRadioButtonGetExt_itemStateChanged(ItemEvent e) {
        if (jRadioButtonGetExt.isSelected()) {
            selectOption = SELECT_GET_EXT;
            jTextFieldGetExt.setEnabled(true);
        } else {
            jTextFieldGetExt.setEnabled(false);
        }
    }

    void jRadioButtonGetMime_itemStateChanged(ItemEvent e) {
        if (jRadioButtonGetMime.isSelected()) {
            selectOption = SELECT_GET_MIME;
            jTextFieldGetMime.setEnabled(true);
        } else {
            jTextFieldGetMime.setEnabled(false);
        }
    }
}


class FileTypes_jAddNewActionButton_actionAdapter implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jAddNewActionButton_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jAddNewActionButton_actionPerformed(e);
    }
}


class FileTypes_jApplyButton_actionAdapter implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jApplyButton_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jApplyButton_actionPerformed(e);
    }
}


class FileTypes_jCancelButton_actionAdapter implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jCancelButton_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCancelButton_actionPerformed(e);
    }
}


class FileTypes_jRadioButtonRegisterSys_actionAdapter 
    implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonRegisterSys_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButtonRegisterSys_actionPerformed(e);
    }
}


class FileTypes_jRadioButtonRegisterUser_actionAdapter 
    implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonRegisterUser_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButtonRegisterUser_actionPerformed(e);
    }
}


class FileTypes_jRadioButtonUnregisterSys_actionAdapter 
    implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonUnregisterSys_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButtonUnregisterSys_actionPerformed(e);
    }
}


class FileTypes_jRadioButtonUnregisterUser_actionAdapter 
    implements java.awt.event.ActionListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonUnregisterUser_actionAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButtonUnregisterUser_actionPerformed(e);
    }
}


class FileTypes_jRadioButtonGetExt_itemAdapter implements java.awt.event.ItemListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonGetExt_itemAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e) {
        adaptee.jRadioButtonGetExt_itemStateChanged(e);
    }
}


class FileTypes_jRadioButtonGetMime_itemAdapter implements java.awt.event.ItemListener {
    FileTypes adaptee;

    FileTypes_jRadioButtonGetMime_itemAdapter(FileTypes adaptee) {
        this.adaptee = adaptee;
    }

    public void itemStateChanged(ItemEvent e) {
        adaptee.jRadioButtonGetMime_itemStateChanged(e);
    }
}
