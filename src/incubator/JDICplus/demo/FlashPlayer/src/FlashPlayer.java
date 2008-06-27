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
 * Sample Flash Player implementation.
 * @author uta
 */
public class FlashPlayer extends JFrame
{
    InputStream getFlashHTMLSource(
        String stFlashURL,
        String stWidth,
        String stHeight,
        String stQuality,
        String stBkColor
    ){
       String stFormat =
                "<html><body border=\"no\" scroll=\"no\" style=\"margin: 0px 0px 0px 0px;\">"+
                "<object style=\"margin: 0px 0px 0px 0px; width:%s; height:%s\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0\"> " +
                "  <param name=\"movie\" value=\"%s\"> " +
                "  <param name=\"quality\" value=\"%s\"> " +
                "  <param name=bgcolor VALUE=%s>" +
                "</object>" +
                "</body></html>";
        return new StringBufferInputStream( String.format(
                stFormat,
                stWidth, stHeight,
                stFlashURL,
                stQuality,
                stBkColor));
    }

    InputStream getFlashHTMLSource(String stFlashURL)
    {
        return getFlashHTMLSource(
            stFlashURL,
            "100%", "100%",
            "high",
            "#D9D9D9"
        );
    }

    FlashPlayer() {
        BrComponent.DESIGN_MODE = false;
        BrComponent.setDefaultPaintAlgorithm(BrComponent.PAINT_NATIVE);
                
        setTitle("Flash Player");

        JPanel rootPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        rootPanel.setLayout(gridbag);


        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;

        final BrComponent player = new BrComponent();
        player.setBounds(0, 0, 500, 363);
        player.setPreferredSize(new Dimension(425, 363));
        final String stGame = "http://flashportal.ru/monstertruckcurfew.swfi";
        final String stMovie = 
            "<html><body border=\"no\" scroll=\"no\" style=\"margin: 0px 0px 0px 0px;\">"+
            "<object style=\"margin: 0px 0px 0px 0px; width:100%; height:100%\"" +
            "        value=\"http://www.youtube.com/v/mlTKOsBOVMw&hl=en\">" + 
            "<param name=\"wmode\" value=\"transparent\"> " +
            "<embed style=\"margin: 0px 0px 0px 0px; width:100%; height:100%\" src=\"http://www.youtube.com/v/mlTKOsBOVMw&hl=en\" type=\"application/x-shockwave-flash\" wmode=\"transparent\"></embed>" +
            "</object>" +
            "</body></html>";
        player.setHTML( (InputStream) new StringBufferInputStream(stMovie), "" );
                                                                                    
        c.gridwidth = GridBagConstraints.REMAINDER; //end row REMAINDER
        gridbag.setConstraints(player, c);
        rootPanel.add(player);

        final JTextField help = new JTextField("Please, use \u2190,\u2191,\u2192,\u2193 keys!");
        help.setPreferredSize(new Dimension(220, 10));
        help.setBounds(50,10,220,16);
        player.add(help, BorderLayout.LINE_END);

        //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last in row
        c.weightx = 0.0;//reset to the default
        c.weighty = 0.0;

        //
        {
            JPanel p2 = new JPanel();
            gridbag.setConstraints(p2, c);
            rootPanel.add(p2);

            JButton edSampleGame = new JButton("Sample Game (SWF)");
            edSampleGame.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        player.setHTML( getFlashHTMLSource(stGame), stGame );
                        help.setText("Please, use \u2190,\u2191,\u2192,\u2193 keys!");
                    }
                }
            );
            p2.add(edSampleGame, BorderLayout.LINE_START);

            JButton edSampleMovie = new JButton("Sample Movie (FLV)");
            edSampleMovie.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        player.setHTML(
                            (InputStream) new StringBufferInputStream(
                            stMovie), "");
                        help.setText("Enjoy!");
                    }
                }
            );
            p2.add(edSampleMovie, BorderLayout.LINE_START);

            JButton edSave = new JButton("Open flash file...");
            edSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                          "Flash Files", "swf");
                        fc.setFileFilter(filter);
                        if( JFileChooser.APPROVE_OPTION == fc.showDialog(FlashPlayer.this, "Play") ){
                            String stGame = fc.getSelectedFile().getAbsolutePath();
                            player.setHTML(getFlashHTMLSource(stGame), stGame);
                            help.setText("Enjoy!");
                        }
                    }
                }
            );
            p2.add(edSave, BorderLayout.LINE_END);
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
    }

    public static void main(String [] args) {
        FlashPlayer test = new FlashPlayer();
    }
}

