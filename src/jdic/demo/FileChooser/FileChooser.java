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
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;

import org.jdesktop.jdic.desktop.*;


/**
 * JDIC API demo main class.
 * <p>
 * Class <code>FileChooser</code> creates a UI interface demonstrating the 
 * usage of the public API of <code>org.jdesktop.jdic.desktop.*</code> classes.
 */
public class FileChooser extends JPanel {
    JPanel fileAndUrlPanel = new JPanel();
    JFileChooser jFileChooser = new JFileChooser();
    JPanel urlPanel = new JPanel();  

    JPanel jVerbPanel = new JPanel(); 
    JPanel jFileButtonPanel = new JPanel();
    JPanel jUrlButtonPanel = new JPanel(); 
    JButton jMailButton = new JButton();
    JButton jPrintButton = new JButton();
    JButton jEditButton = new JButton();
    JButton jOpenButton = new JButton();
    JButton jBrowseButton = new JButton();

    // The two spaces("  ") seperate the url label and the text field.
    JLabel jUrlLabel = new JLabel("URL:  ");
    JTextField jUrlTextField = new JTextField();
  
    File selectedFile;
  
    public FileChooser() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        JFrame frame = new JFrame("JDIC API Demo - FileChooser");

        Container contentPane = frame.getContentPane();

        contentPane.setLayout(new GridLayout(1, 1));
        contentPane.add(new FileChooser());

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void jbInit() throws Exception {
        jFileChooser.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.addPropertyChangeListener(new FileChooser_jFileChooser_propertyChangeAdapter(this));
        jUrlTextField.addActionListener(new FileChooser_jUrlTextField_actionAdapter(this));
        jMailButton.setEnabled(true);
        jMailButton.setText("Mail to ...");
        jMailButton.addActionListener(new FileChooser_jCreateMailButton_actionAdapter(this));
        jPrintButton.setEnabled(false);
        jPrintButton.setText("Print");
        jPrintButton.addActionListener(new FileChooser_jPrintButton_actionAdapter(this));
        jEditButton.setEnabled(false);
        jEditButton.setText("Edit");
        jEditButton.addActionListener(new FileChooser_jEditButton_actionAdapter(this));
        jOpenButton.setEnabled(false);
        jOpenButton.setText("Open");
        jOpenButton.addActionListener(new FileChooser_jOpenButton_actionAdapter(this));
        jBrowseButton.setText("Browse");
        jBrowseButton.setToolTipText("Browse the given URL with system default browser");
        jBrowseButton.addActionListener(new FileChooser_jBrowseButton_actionAdapter(this));

        jFileButtonPanel.setLayout(new GridLayout(8, 1, 10, 10));
        // Here, it aligns the top of jFileButtonPanel with the top of the file list 
        // box in the jFileChooser.
        jFileButtonPanel.setBorder(BorderFactory.createEmptyBorder(42, 10, 0, 10));
        jFileButtonPanel.add(jOpenButton, null);
        jFileButtonPanel.add(jEditButton, null);
        jFileButtonPanel.add(jPrintButton, null);
        // jFileButtonPanel.add(new JSeparator());
        jFileButtonPanel.add(jMailButton, null);
        jUrlButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jUrlButtonPanel.setLayout(new GridLayout());
        jUrlButtonPanel.add(jBrowseButton, null);     
        jVerbPanel.setLayout(new BorderLayout());
        jVerbPanel.add(jFileButtonPanel, BorderLayout.CENTER);
        jVerbPanel.add(jUrlButtonPanel, BorderLayout.SOUTH);

        urlPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 11, 0));
        urlPanel.setLayout(new BorderLayout());
        urlPanel.add(jUrlLabel, BorderLayout.WEST);
        urlPanel.add(jUrlTextField, BorderLayout.CENTER);

        fileAndUrlPanel.setLayout(new BorderLayout());
        fileAndUrlPanel.add(jFileChooser, BorderLayout.CENTER);
        fileAndUrlPanel.add(urlPanel, BorderLayout.SOUTH);

        // JSeparator jSeparator = new JSeparator();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.setPreferredSize(new Dimension(screenSize.width * 6 / 10,
                screenSize.height * 5 / 10));
        this.setLayout(new BorderLayout());
        this.add(fileAndUrlPanel, BorderLayout.CENTER);
        // this.add(jSeparator, BorderLayout.CENTER);
        this.add(jVerbPanel, BorderLayout.EAST);
    }

    void jOpenButton_actionPerformed(ActionEvent e) {
        try {
            Desktop.open(selectedFile);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, "Exception: " + de.toString(),
                    "Error message", JOptionPane.ERROR_MESSAGE);
        }
    }

    void jBrowseButton_actionPerformed(ActionEvent e) {
        String inputUrl = jUrlTextField.getText();
        if (inputUrl == null) {
            return;
        }

        try {
            Desktop.browse(new URL(inputUrl));
        } catch (MalformedURLException de) {
            JOptionPane.showMessageDialog(this, "Invalid URL: " + inputUrl,
                    "Exception", JOptionPane.ERROR_MESSAGE); 
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jEditButton_actionPerformed(ActionEvent e) {
        try {
            Desktop.edit(selectedFile);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jPrintButton_actionPerformed(ActionEvent e) {
        try {
            Desktop.print(selectedFile);
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void jCreateMailButton_actionPerformed(ActionEvent e) {
        if (selectedFile == null) {
            // No selected file as attachment, just launch the system mailer.
            try {
                Desktop.mail();
            } catch (DesktopException de) {
                JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Has selected file as attachment, construct a message specifing the 
            // attachment and launch the system mailer.

            Message msg = new Message();

            List attachList = new ArrayList();

            attachList.add(selectedFile.toString());
            // attachList.add(selectedFile);
            try {
                msg.setAttachments(attachList);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(this, ioe.toString(), "Exception",
                        JOptionPane.ERROR_MESSAGE);
            }
                  
            try {
                Desktop.mail(msg);
            } catch (DesktopException de) {
                JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void jFileChooser_propertyChange(PropertyChangeEvent e) {
    	if(JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())){
            jOpenButton.setEnabled(false);
            jEditButton.setEnabled(false);
            jPrintButton.setEnabled(false);
    	}else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
            // The selected file should always be the same as newFile
            selectedFile = jFileChooser.getSelectedFile();
            if (selectedFile != null) {
                // A file is selected, enable button "Open".
                jOpenButton.setEnabled(true);
                // To check that whether "Edit" and "Print" buttons should be enabled.
                if (Desktop.isEditable(selectedFile)) {
                    jEditButton.setEnabled(true);
                } else {
                    jEditButton.setEnabled(false);
                }
                if (Desktop.isPrintable(selectedFile)) {
                    jPrintButton.setEnabled(true);
                } else {
                    jPrintButton.setEnabled(false);
                }

            } else {
                // A directory is selected, then disable buttons
                // "Open", "Edit" and "Print".
                jOpenButton.setEnabled(false);
                jEditButton.setEnabled(false);
                jPrintButton.setEnabled(false);
            }
        }
    }
    
    void jUrlTextField_actionPerformed(ActionEvent e) {
        String inputUrl = jUrlTextField.getText();
        if (inputUrl == null) {
            return;
        }

        try {
            Desktop.browse(new URL(inputUrl));
        } catch (MalformedURLException de) {
            JOptionPane.showMessageDialog(this, "Invalid URL: " + inputUrl,
                    "Exception", JOptionPane.ERROR_MESSAGE); 
        } catch (DesktopException de) {
            JOptionPane.showMessageDialog(this, de.toString(), "Exception",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}


class FileChooser_jOpenButton_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jOpenButton_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jOpenButton_actionPerformed(e);
    }
}


class FileChooser_jBrowseButton_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jBrowseButton_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jBrowseButton_actionPerformed(e);
    }
}


class FileChooser_jEditButton_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jEditButton_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jEditButton_actionPerformed(e);
    }
}


class FileChooser_jPrintButton_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jPrintButton_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jPrintButton_actionPerformed(e);
    }
}


class FileChooser_jCreateMailButton_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jCreateMailButton_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jCreateMailButton_actionPerformed(e);
    }
}


class FileChooser_jFileChooser_propertyChangeAdapter implements java.beans.PropertyChangeListener {
    FileChooser adaptee;

    FileChooser_jFileChooser_propertyChangeAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void propertyChange(PropertyChangeEvent e) {
        adaptee.jFileChooser_propertyChange(e);
    }
}

class FileChooser_jUrlTextField_actionAdapter implements java.awt.event.ActionListener {
    FileChooser adaptee;

    FileChooser_jUrlTextField_actionAdapter(FileChooser adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jUrlTextField_actionPerformed(e);
    }
}
