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

package org.jdesktop.jdic.packager.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.MissingFieldException;
import com.sun.javaws.jnl.XMLUtils;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLParser;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.InformationDesc;


/**
 * This class parses a given jnlp file to extract information.
 */
public final class JnlpParser {
    /**
     * JnlpParser creator.
     *
     */
    private JnlpParser() {
    }

    /**
     * Constructs a LaunchDesc object form a stream.
     * <p>
     * The InputStream object must support the reset() method
     *
     * The factory method can potentially understand several
     * different formats, such as a property file based one,
     * and a XML based one
     * </p>
     * @param jnlpFilePath The given jnlp file path.
     * @return The LaunchDesc object.
     */
    private static LaunchDesc getLaunchDesc(String jnlpFilePath) {
        LaunchDesc tLaunchDesc = null;
        try {
            tLaunchDesc = LaunchDescFactory.buildDescriptor(jnlpFilePath);
        } catch (Exception e) {
            return null;
        }
        return tLaunchDesc;
    }

    /**
     * Parses the href of the given jnlp file.
     * @param jnlpFile The given jnlp file.
     * @return The given jnlp href.
     */
    public static String parseJnlpHref(File jnlpFile) {
        String href = null;
        try {
            FileInputStream fis = new FileInputStream(jnlpFile);
            int fLen = (int) jnlpFile.length();
            byte[] bits = new byte[fLen];

            fis.read(bits, 0, fLen);
            fis.close();

            String content = new String(bits);
            XMLNode root = (new XMLParser(content)).parse();

            href = XMLUtils.getAttribute(root, "", "href");
        } catch (Exception e) {
           return null;
        }
        return href;
    }

    /**
     * Parses the specified property value from the given jnlp file.
     * @param jnlpFile The given jnlp File.
     * @param infoName The name of the specified jnlp field.
     * @return The specified property value.
     */
    public static String parseJnlpInfo(File jnlpFile, String infoName) {
        String infoString = null;
        LaunchDesc launchDesc = getLaunchDesc(jnlpFile.toString());
        InformationDesc infoDesc = launchDesc.getInformation();

        if (infoName.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_CODEBASE)
            == 0) {
            URL url = launchDesc.getCodebase();
            infoString = url.toString();
        } else if (
            infoName.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_TITLE)
                == 0) {
            infoString = infoDesc.getTitle();
        } else if (
            infoName.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_VENDOR)
                == 0) {
            infoString = infoDesc.getVendor();
        }
        return infoString;
    }

    /**
     * Parses the referenced file list from the given jnlp file.
     * @param jnlpFile The given jnlp file.
     * @return The list containing the reference files.
     */
    public static List parseJnlpRefFiles(File jnlpFile) {
        final List refFileList = new ArrayList();

        try {
            FileInputStream fis = new FileInputStream(jnlpFile);
            int fLen = (int) jnlpFile.length();
            byte[] bits = new byte[fLen];

            fis.read(bits, 0, fLen);
            fis.close();

            String content = new String(bits);
            XMLNode root = (new XMLParser(content)).parse();

            // Check the platform-independent jar and nativelib resources.
            XMLUtils.visitElements(root, "<resources><jar>",
                                   new XMLUtils.ElementVisitor() {
                public void visitElement(XMLNode  e)
                        throws BadFieldException, MissingFieldException {
                    String attr = XMLUtils.getAttribute(e, "", "href");
                    if (attr != null) {
                        refFileList.add(attr);
                    }
                }
            });

            XMLUtils.visitElements(root, "<resources><nativelib>",
                                   new XMLUtils.ElementVisitor() {
                public void visitElement(XMLNode  e)
                       throws BadFieldException, MissingFieldException {
                    String attr = XMLUtils.getAttribute(e, "", "href");
                    if (attr != null) {
                        refFileList.add(attr);
                    }
                }
            });

            XMLUtils.visitElements(root, "<information><icon>",
                                   new XMLUtils.ElementVisitor() {
                public void visitElement(XMLNode  e)
                            throws BadFieldException, MissingFieldException {
                    String attr = XMLUtils.getAttribute(e, "", "href");
                    if (attr != null) {
                        refFileList.add(attr);
                    }
                }
            });

            // Check the jar and nativelib resources for this particular OS.
            String osName = System.getProperty("os.name");
            XMLNode osNode = XMLUtils.getElementWithAttribute(root, "<resources>", "os", osName);            
            if (osNode != null) {
                XMLUtils.visitElements(osNode, "<jar>",
                                       new XMLUtils.ElementVisitor() {
                    public void visitElement(XMLNode  e)
                                throws BadFieldException, MissingFieldException {
                        String attr = XMLUtils.getAttribute(e, "", "href");
                        if (attr != null) {
                            refFileList.add(attr);
                        }
                    }
                });

                XMLUtils.visitElements(osNode, "<nativelib>",
                                       new XMLUtils.ElementVisitor() {
                    public void visitElement(XMLNode  e)
                                throws BadFieldException, MissingFieldException {
                        String attr = XMLUtils.getAttribute(e, "", "href");
                        if (attr != null) {
                            refFileList.add(attr);
                        }
                    }
                });
            }
        } catch (Exception e) {
            return null;
        }

        if (refFileList.isEmpty()) {
            return null;
        } else {
            return refFileList;
        }
    }
}
