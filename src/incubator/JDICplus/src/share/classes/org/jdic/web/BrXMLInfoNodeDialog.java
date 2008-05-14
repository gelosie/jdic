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
package org.jdic.web;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;


/**
 * The propery dialog for HTML document node.
 * @author uta
 */    
public class BrXMLInfoNodeDialog extends JDialog {

    static XMLSerializer domSer = new XMLSerializer();

    Node masterNode;

    JTabbedPane tabsInfo = new JTabbedPane();
    JTable tabAttrs = new JTable();

    private static final String[] COL_NAMES = new String[] {
        "nsURI",
        "nsPrefix",
        "name",
        "value"
    };
    class BrXMLAttrsTableModel extends AbstractTableModel {
        public int getRowCount() {
            if( masterNode == null ||
                masterNode.getAttributes() == null )
            {
                return 0;
            }
            return masterNode.getAttributes().getLength();
        }

        public int getColumnCount() {
            return COL_NAMES.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Attr at = (Attr) masterNode.getAttributes().item(rowIndex);
            switch(columnIndex){
            case 0:
                return at.getPrefix();
            case 1:
                return at.getNamespaceURI();
            case 2:
                return at.getName();
            }
            return at.getValue();
        }

        @Override
        public String getColumnName(int column) {
            return COL_NAMES[column];
        }
    }

    JTextPane txtPane = new JTextPane();
    BrXMLTree treeTailOfTree = new BrXMLTree();
    JTextPane txtPaneAsIs = new JTextPane();
    BrComponent txtPaneAsHtml = new BrComponent();

    public BrXMLInfoNodeDialog(Node masterNode) throws HeadlessException {
        this.masterNode = masterNode;
        setModal(true);
        getContentPane().add(tabsInfo);

        tabAttrs.setModel(new BrXMLAttrsTableModel());
        txtPane.setText(masterNode.getNodeValue());
        String tt = masterNode.getTextContent();
        txtPane.setText(tt);
        txtPane.setEditable(false);
        treeTailOfTree.setModel(new BrXMLTreeModel(masterNode));
        //if(masterNode instanceof Element){
            try {
                final String encode = "UTF-16";
                //final String encode = "UTF-8";
                ByteArrayOutputStream chaw = new ByteArrayOutputStream(4096);
                OutputStreamWriter outw = new OutputStreamWriter(chaw, encode);

                OutputFormat xmlFmt = new OutputFormat("xhtml", encode, true);
                xmlFmt.setOmitXMLDeclaration(true);
                        
                domSer.setOutputFormat(xmlFmt);
                domSer.setOutputCharStream(outw);
                domSer.serialize((Element) masterNode);
                outw.flush();
                
                txtPaneAsIs.setEditable(true);
                String t = chaw.toString(encode);
                txtPaneAsIs.setText(t);
                tabsInfo.add("As XML", new JScrollPane(txtPaneAsIs));
                
                chaw.reset();
                
                OutputFormat htmlFmt = new OutputFormat("html", encode, true);
                htmlFmt.setOmitXMLDeclaration(true);
                
                domSer.setOutputFormat(htmlFmt);
                domSer.setOutputCharStream(outw);
                domSer.serialize((Element) masterNode);
                outw.flush();

                String sb = new String(chaw.toByteArray(), encode);
                txtPaneAsHtml.setHTML(new ByteArrayInputStream(sb.getBytes("UTF-8")), "");
                tabsInfo.add("As HTML", txtPaneAsHtml);
            } catch (Exception e) {
                e.printStackTrace();
            }
        //}
        tabsInfo.add("Attributes", new JScrollPane(tabAttrs));
        tabsInfo.add("SubTree", new JScrollPane(treeTailOfTree));
        tabsInfo.add("Content", new JScrollPane(txtPane));
        
        setTitle(masterNode.getNamespaceURI() + ":" + masterNode.getPrefix() + ":" + masterNode.getNodeName());
        pack();

        setSize(400, 600);
        setLocationRelativeTo(null);
    }
}
