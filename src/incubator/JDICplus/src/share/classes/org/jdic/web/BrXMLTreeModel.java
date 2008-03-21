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

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;

/**
 * The tree model class for XML tree view.
 * @author uta
 */
public class BrXMLTreeModel implements TreeModel {
    Node root;

    public BrXMLTreeModel(String stXML) {
        System.setProperty(
                DOMImplementationRegistry.PROPERTY,
                "org.apache.xerces.dom.DOMImplementationSourceImpl");
        try {
            DOMImplementationRegistry registry =
                    DOMImplementationRegistry.newInstance();
            DOMImplementationLS domImpl =
                   (DOMImplementationLS)registry.getDOMImplementation("LS");
            LSParser parser =domImpl.createLSParser(
                    DOMImplementationLS.MODE_SYNCHRONOUS,
                    "http://www.w3.org/2001/XMLSchema");
            LSInput lsInput = domImpl.createLSInput();
            lsInput.setStringData(stXML);
            Document document = parser.parse(lsInput);
            this.root = document;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();  
        }
    }

    public BrXMLTreeModel(Node root) {
        this.root = root;
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        Node nparent = (Node) parent;
        return nparent.getChildNodes().item(index);
    }

    public int getChildCount(Object parent) {
        Node nparent = (Node) parent;
        return nparent.getChildNodes().getLength();
    }

    public boolean isLeaf(Object node) {
        Node nparent = (Node) node;
        return nparent.getChildNodes().getLength() == 0;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public int getIndexOfChild(Object parent, Object child) {
        Node nparent = (Node) parent;
        NodeList nli = nparent.getChildNodes();
        for (int i = 0; i < nli.getLength(); i++)
            if (nli.item(i) == child)
                return i;
        return -1;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }
}
