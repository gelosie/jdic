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
import org.jdic.web.BrComponent;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Sample HTML Editor implementation.
 * @author uta
 */
public class HTMLEditor extends JFrame
{
    HTMLEditor() {
        BrComponent.DESIGN_MODE = false;
        setTitle("HTML Editor");

        Panel rootPanel = new Panel();
        rootPanel.setBounds(0, 0, 800, 600);
        rootPanel.setPreferredSize(new Dimension(800, 600));

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        rootPanel.setLayout(gridbag);


        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        String stHTML = "<html>\n" +
                "<style>\n" +
                    "p, ul, ol, blockquote, br, body {margin-top: 0; margin-bottom: 0}\n" +
                "</style>\n" +                
                "<body><font size='+3' color='red'>Simple <font color='blue'>HTML</font> editor</font><br/>\n" +
                "Press:\n" +
                "<ul>\n" +
                "<li>Ctrl+'B' for <b>Bold</b></li>\n" +
                "<li>Ctrl+'I' for <i>Italic</i></li>\n" +
                "<li>Ctrl+'U' for <u>Underline</u></li>\n" +
                "<li>Ctrl+'K' for <a href=\"http://sun.com\">Link \n" +
                "</ul>" +
                "<TABLE style=\"BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; BORDER-BOTTOM: medium none\" cellSpacing=0 cellPadding=0 border=1>\n" +
                "<TBODY>\n" +
                "<TR>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; \n" +
                "PADDING-RIGHT: 5.75pt; BORDER-TOP: windowtext 1pt solid; PADDING-LEFT: 5.75pt; \n" +
                "BACKGROUND-ATTACHMENT: scroll; PADDING-BOTTOM: 0in; BORDER-LEFT: windowtext 1pt solid; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid; \n" +
                "BACKGROUND-REPEAT: repeat; BACKGROUND-COLOR: silver\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center><B>Capability</B></P></TD>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: windowtext 1pt solid; PADDING-LEFT: 5.75pt; BACKGROUND-ATTACHMENT: scroll; \n" +
                "PADDING-BOTTOM: 0in; BORDER-LEFT: #d4d0c8; WIDTH: 221.4pt; PADDING-TOP: 0in; \n" +
                "BORDER-BOTTOM: windowtext 1pt solid; BACKGROUND-REPEAT: repeat; \n" +
                "BACKGROUND-COLOR: silver\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center><B>Support</B></P></TD></TR>\n" +
                "<TR>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: #d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: windowtext 1pt solid; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center><U><B><FONT color=#ff0000>\n" +
                "<I>Rich Text Support</I></FONT></B></U></P></TD>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: #d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: #d4d0c8; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center>Yes!</P></TD></TR>\n" +
                "<TR>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: #d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; \n" +
                "BORDER-LEFT: windowtext 1pt solid; WIDTH: 221.4pt; \n" +
                "PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center>Bulleted/Numbered Lists</P></TD>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: #d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; \n" +
                "BORDER-LEFT: #d4d0c8; WIDTH: 221.4pt; PADDING-TOP: 0in; \n" +
                "BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<UL type=disc>\n" +
                "<LI style=\"MARGIN: 5pt 0in\" align=\"center\">Yes!</LI></UL></TD></TR>\n" +
                "<TR>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; \n" +
                "BORDER-TOP: #d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: windowtext 1pt solid; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center>Embedded Pictures</P></TD>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; BORDER-TOP: #d4d0c8; \n" +
                "PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: #d4d0c8; WIDTH: 221.4pt; \n" +
                "PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center><FONT face=Arial size=10>\n" +
                "<IMG id=img102 src=\"http://129.159.125.68/jdicp/ws/images/small.gif\"></FONT></P>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center><FONT face=Arial>(Yes!)</FONT></P></TD></TR>\n" +
                "<TR>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; BORDER-TOP: \n" +
                "#d4d0c8; PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: windowtext 1pt solid; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center>Embedded Tables</P></TD>\n" +
                "<TD style=\"BORDER-RIGHT: windowtext 1pt solid; PADDING-RIGHT: 5.75pt; BORDER-TOP: #d4d0c8; \n" +
                "PADDING-LEFT: 5.75pt; PADDING-BOTTOM: 0in; BORDER-LEFT: #d4d0c8; \n" +
                "WIDTH: 221.4pt; PADDING-TOP: 0in; BORDER-BOTTOM: windowtext 1pt solid\" vAlign=top width=295>\n" +
                "<P style=\"MARGIN: 5pt 0in\" align=center>Yes!</P></TD></TR></TBODY></TABLE>\n" +
                "</body></html>";

        final BrComponent hted = new BrComponent();
        hted.setBounds(0, 0, 500, 300);
        hted.setPreferredSize(new Dimension(500, 300));
        hted.setHTML( new StringBufferInputStream(stHTML), "Editor" );
        hted.setEditable(true);



        //c.gridwidth = GridBagConstraints.REMAINDER; //end row REMAINDER
        gridbag.setConstraints(hted, c);
        rootPanel.add(hted);

        final JTextArea txed = new JTextArea(stHTML);
        //final TextField txed = new TextField(stHTML);
        //txed.setBounds(0, 0, 500, 300);
        //txed.setPreferredSize(new Dimension(500, 300));
        txed.setEditable(true);
        txed.setAutoscrolls(true);
        JScrollPane scrollPane = new JScrollPane(txed);

        
        c.gridwidth = GridBagConstraints.REMAINDER; //end row REMAINDER
        gridbag.setConstraints(scrollPane, c);
        rootPanel.add(scrollPane);


        //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last in row
        c.weightx = 0.0;//reset to the default
        c.weighty = 0.0;

        //
        {
            JPanel p2 = new JPanel();
            gridbag.setConstraints(p2, c);
            rootPanel.add(p2);

            JButton edX2Text = new JButton("XHTML>>");
            edX2Text.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                         txed.setText( hted.getXHTML() );
                    }
                }
            );
            p2.add(edX2Text, BorderLayout.CENTER);

            JButton ed2Text = new JButton("HTML>>");
            ed2Text.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                         txed.setText( hted.getHTML() );
                    }
                }
            );
            p2.add(ed2Text, BorderLayout.CENTER);

            JButton ed2HTML = new JButton("<<");
            ed2HTML.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        hted.setHTML( txed.getText() );
                    }
                }
            );
            p2.add(ed2HTML, BorderLayout.CENTER);


            JButton edSave = new JButton("Open HTML file...");
            edSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser();
                        FileNameExtensionFilter filter1 = new FileNameExtensionFilter(
                          "HTML Files", "html");
                        fc.setFileFilter(filter1);
                        FileNameExtensionFilter filter2 = new FileNameExtensionFilter(
                          "Text Files", "txt");
                        fc.addChoosableFileFilter(filter2);
                        if( JFileChooser.APPROVE_OPTION == fc.showDialog(HTMLEditor.this, "Open") ){
                            String stSrc = fc.getSelectedFile().getAbsolutePath();
                            hted.setURL(stSrc);
                        }
                    }
                }
            );
            p2.add(edSave, BorderLayout.PAGE_END);
        }

        add(rootPanel);
        pack();
        setVisible(true);

        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if( !hted.hasFocus() ){
//                    hted.requestFocus();
                }
            }
        });
    }

    public static void main(String [] args) {
        HTMLEditor test = new HTMLEditor();
    }
}

