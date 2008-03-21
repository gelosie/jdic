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

import org.w3c.dom.*;
import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

/**
 * The node button for XML tree document/node view.
 * @author uta
 */    
class BrXMLButton extends JPanel {
    protected static ImageIcon ico_element = new ImageIcon(BrXMLButton.class.getResource("images/img_element.png"));
    protected static ImageIcon ico_attribute = new ImageIcon(BrXMLButton.class.getResource("images/img_attribute.png"));
    protected static ImageIcon ico_text = new ImageIcon(BrXMLButton.class.getResource("images/img_cdata.png"));
    protected static ImageIcon ico_pi = new ImageIcon(BrXMLButton.class.getResource("images/img_pi.png"));
    protected static ImageIcon ico_document = new ImageIcon(BrXMLButton.class.getResource("images/img_root.png"));
    protected static ImageIcon ico_unknown = new ImageIcon(BrXMLButton.class.getResource("images/img_unknown.png"));
    protected static ImageIcon ico_comment = new ImageIcon(BrXMLButton.class.getResource("images/img_comment.png"));
    protected static ImageIcon ico_entity = new ImageIcon(BrXMLButton.class.getResource("images/img_entity.png"));


    public static Icon getIconForNodeType(Node n) {
        if (n instanceof Document)
            return ico_document;
        else if (n instanceof Element)
            return ico_element;
        else if (n instanceof Attr)
            return ico_attribute;
        else if (n instanceof Text)
            return ico_text;
        else if (n instanceof ProcessingInstruction)
            return ico_element;
        else if (n instanceof Comment)
            return ico_comment;
        else if (n instanceof Entity)
            return ico_entity;
        else if (n instanceof EntityReference)
            return ico_entity;
        else 
            return ico_unknown;
    }


    protected Node masterNode;
    JLabel lbNode = new JLabel();
    JButton bnNode = new JButton();

    public BrXMLButton(Node masterNode) {
        super(new BorderLayout());
        String fullText = masterNode.getNodeName();
        NamedNodeMap attrs = masterNode.getAttributes();
        if (attrs != null && attrs.getLength() > 0) {
            String as = "";            
            final int length = attrs.getLength();                    
            for( int i = 0; i < length; i++ ){
                Node attr = attrs.item(i);
                if( attr.getNodeName().equals("_uniqueID") ){
                    continue;
                }
                if( 0!=as.length() ){
                    as += ", ";
                }
                as += (attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
            }
            if( 0!=as.length() ){
                fullText += " {" + as + "}";
            }    
        }
        //fullText += " path:" + BrXMLTree.getNodePath("", masterNode);
        int w = SwingUtilities.computeStringWidth(getFontMetrics(getFont()), fullText + " ");
        w = (120 * w) / 100;
        this.masterNode = masterNode;
        add(lbNode, BorderLayout.CENTER);
        add(bnNode, BorderLayout.WEST);

        bnNode.setPreferredSize(new Dimension(18, 18));
        lbNode.setPreferredSize(new Dimension(w, 18));
        bnNode.setIcon(getIconForNodeType(masterNode));
        lbNode.setText(fullText);

        short t = masterNode.getNodeType();
        String st = "";

        st = BrXMLTree.getNodeTypeName(t, st);

        bnNode.setToolTipText("<HTML>" + masterNode.getNodeName() + "<BR>" + st);

        bnNode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new BrXMLInfoNodeDialog(BrXMLButton.this.masterNode).setVisible(true);
            }
        });
    }
}

/**
 * The XML tree control in propery dialog for HTML document/node view.
 * @author uta
 */    
public class BrXMLTree extends JTree {

    public static String getNodeTypeName(short t, String st) {
        //TODO: make it localizable
        if(t == Node.ATTRIBUTE_NODE)
            st = "The node is an Attr.";
        else if(t == Node.CDATA_SECTION_NODE)
            st = "The node is a CDATASection";
        else if(t == Node.COMMENT_NODE)
            st = "The node is a Comment";
        else if(t == Node.DOCUMENT_FRAGMENT_NODE)
            st = "The node is a DocumentFragment";
        else if(t == Node.DOCUMENT_NODE)
            st = "The node is a Document.";
        else if(t == Node.DOCUMENT_POSITION_CONTAINED_BY)
            st = "The node is contained by the reference node.";
        else if(t == Node.DOCUMENT_POSITION_CONTAINS)
            st = "The node contains the reference node.";
        else if(t == Node.DOCUMENT_POSITION_DISCONNECTED)
            st = "The two nodes are disconnected.";
        else if(t == Node.DOCUMENT_POSITION_FOLLOWING)
            st = "The node follows the reference node";
        else if(t == Node.DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC)
            st = "The determination of preceding versus following is implementation-specific.";
        else if(t == Node.DOCUMENT_POSITION_PRECEDING)
            st = "The node precedes the reference node.";
        else if(t == Node.DOCUMENT_TYPE_NODE)
            st = "The node is a DocumentType.";
        else if(t == Node.ELEMENT_NODE)
            st = "The node is an Element";
        else if(t == Node.ENTITY_NODE)
            st = "The node is an Entity.";
        else if(t == Node.ENTITY_REFERENCE_NODE)
            st = "The node is an EntityReference.";
        else if(t == Node.PROCESSING_INSTRUCTION_NODE)
            st = "The node is a ProcessingInstruction.";
        else if(t == Node.TEXT_NODE)
            st = "The node is a Text node.";
        return st;
    }
    
    public final static BrXMLTreeModel emptyTree = new BrXMLTreeModel(
            "<?xml version='1.0'?><empty_DOM_model></empty_DOM_model>"
    );
    
    public void empty()
    {
        setModel(emptyTree);  
    } 

    public BrXMLTree(BrXMLTreeModel newModel) {
        super(newModel);
        init();
    }


    public BrXMLTree(String  stXML) {
        this(new BrXMLTreeModel(stXML));
    }

    public BrXMLTree() {
        this(emptyTree);
    }

    public void setXMLSource(String  stXML){
        setModel(new BrXMLTreeModel(stXML));
    }
    
    void init(){
        setCellEditor( new TreeCellEditor() {
            public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
                if (!(value instanceof Node)) return new JLabel(value.toString());
                return new BrXMLButton((Node) value);
            }

            public Object getCellEditorValue() {
                return null;
            }

            public boolean isCellEditable(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent ev = (MouseEvent) anEvent;
                    return ev.getClickCount() == 2;
                }
                return false;
            }

            public boolean shouldSelectCell(EventObject anEvent) {
                return false;
            }

            public boolean stopCellEditing() {
                return false;
            }

            public void cancelCellEditing() {
            }

            public void addCellEditorListener(CellEditorListener l) {
            }

            public void removeCellEditorListener(CellEditorListener l) {
            }
        });

        setCellRenderer(new TreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof Node)) {
                    JLabel jLabel = new JLabel(value.toString());
                    if (hasFocus || selected)
                        jLabel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                    else
                        jLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                    return jLabel;
                }

                BrXMLButton brXMLButton = new BrXMLButton((Node) value);

                if (hasFocus || selected)
                    brXMLButton.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                else
                    brXMLButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                return brXMLButton;
            }
        });
        setEditable(true);
    }


    @Override
    public String getToolTipText(MouseEvent e) {
        int selRow = getRowForLocation(e.getX(), e.getY());
        TreePath selPath = getPathForLocation(e.getX(), e.getY());
        if (selRow != -1) {
            Node org_node = (Node) selPath.getLastPathComponent();
            short t = org_node.getNodeType();
            String st = "";
            st = getNodeTypeName(t, st);
            st = "<HTML>" + org_node.getNodeName() + "<BR>" + st + "<BR>";
            if (org_node instanceof Text)
                return st + "\"" + createWrap(((Text) org_node).getTextContent()) + "\"";

            if (org_node instanceof Comment)
                return st + "<!--" + createWrap(((Comment) org_node).getNodeValue()) + "-->";

            if (org_node instanceof Element) {
                String h = "";
                Element element = (Element) org_node;
                NamedNodeMap atrs = element.getAttributes();
                for (int i = 0; i < atrs.getLength(); i++)
                    h += atrs.item(i).getNodeName() + " = \"" + createWrap(atrs.item(i).getNodeValue()) + "\"<BR>";
                h += "<TABLE BORDER=\"1\"><TR><TD align=\"CENTER\"><B>Value:</B></TD></TR><TR><TD>" + element.getTextContent() + "</TD></TR></TABLE>";
                return st + h;
            }
        }
        return null;
    }


    private String createWrap(String what) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < what.length(); i++) {
            if (i > 0 && i % 60 == 0)
                buf.append("<BR>");
            buf.append(what.charAt(i));
        }
        return buf.toString();
    }

    public static String getNodeTypeName(Node l_attr, String st) {
        return getNodeTypeName(l_attr.getNodeType(), st);
    }

    public static String getNodePath(String downPath, Node nd) {
        Node pr = nd.getParentNode();
        if(null==pr){
            return downPath;
        }
        int index = 0;
        for( 
            Node cld = pr.getFirstChild(); 
            null!=cld && !cld.equals(nd);
            cld = cld.getNextSibling()
        ){
            ++index;
        }
        if( 0!=downPath.length() ){
            downPath = "/" + downPath;
        }
        return getNodePath( index + downPath, pr);
    }
    
    public static String getNodePath(Node nd) {
        return getNodePath("", nd);
    }
    
    public static String getNodeTypeName(Node l_attr) {
        return getNodeTypeName(l_attr.getNodeType(), "");
    }
}