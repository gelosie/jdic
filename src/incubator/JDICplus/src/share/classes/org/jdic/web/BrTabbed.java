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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.jdic.web.BrComponent;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import org.jdic.web.event.BrComponentEvent;
import org.jdic.web.event.BrComponentListener;


/**
 * Tabbed <code>BrBrowser</code> component with ability to navigate the link in 
 * new bad instead of new window
 * @author  uta
 */
class JTabPanel extends JPanel {
    // Variables declaration - do not modify                     
    public javax.swing.JLabel lbAddress;
    public javax.swing.JProgressBar pbDownloadDoc;
    public javax.swing.JButton bnClose;
    // End of variables declaration                   
    protected static ImageIcon ico_close = new ImageIcon(JTabPanel.class.getResource("images/img_close_big.png"));

    @Override
    public Dimension getPreferredSize() {
        return new java.awt.Dimension(180, 16);
    }
    
    public JTabPanel(final BrTabbed tbPane, String stTitle){
        super();

        setBorder(null);
        int orientation = tbPane.getTabbedPane().getTabPlacement();
        int box_orientation = (orientation==JTabbedPane.TOP || orientation==JTabbedPane.BOTTOM)
                    ? BoxLayout.LINE_AXIS
                    : BoxLayout.PAGE_AXIS;
	setLayout( new BoxLayout(this, box_orientation) );
        
        setOpaque(false); 
        
        java.awt.Dimension dm = new java.awt.Dimension(180, 16);        
        java.awt.Dimension dm1 = new java.awt.Dimension((int)dm.getWidth()-16, (int)dm.getHeight());        
        java.awt.Dimension dm2 = new java.awt.Dimension(16, 16);        
        
        setMinimumSize(dm);
        setMaximumSize(dm);
        setPreferredSize(dm);
        
        lbAddress = new javax.swing.JLabel();
        bnClose = new javax.swing.JButton();
        pbDownloadDoc = new javax.swing.JProgressBar(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.drawString(getString(), 0, getHeight()-3);
            }
        };

        stTitle = (null==stTitle ? "(empty)" : stTitle);
        
        pbDownloadDoc.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
        pbDownloadDoc.setOpaque(false); 
        pbDownloadDoc.setString(stTitle);
        pbDownloadDoc.setMinimumSize(dm1);
        pbDownloadDoc.setMaximumSize(dm1);
        pbDownloadDoc.setPreferredSize(dm1);
        add(pbDownloadDoc); 
        
        lbAddress.setText(stTitle);
        lbAddress.setOpaque(false); 
        lbAddress.setVisible(false);
        lbAddress.setMinimumSize(dm1);
        lbAddress.setMaximumSize(dm1);
        lbAddress.setPreferredSize(dm1);
        add(lbAddress);
        
        bnClose.setIcon(ico_close);
        bnClose.setFocusable(false);
        bnClose.setContentAreaFilled(false);
        bnClose.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        bnClose.setBorderPainted(false);
        bnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {                
                tbPane.closePage(
                        tbPane.getTabbedPane().indexOfTabComponent(JTabPanel.this));
            }
        });
        bnClose.setMaximumSize(dm2);
        bnClose.setMinimumSize(dm2);
        bnClose.setPreferredSize(dm2);
        add(bnClose);
        
        //add(lbAddress);
    }
}
public class BrTabbed 
        extends JPanel
        implements ChangeListener,
                   BrComponentListener
{
    protected void init(String stUrl){
        initComponents();
        setupTabLabel(brCurrentPage, stUrl);
        jTabbedPane.addChangeListener(this);
    }
    
    /**
     * Creates new BrTabbed object with a web-page 
     * @param stUrl the String that contains the page URL 
     */
    public BrTabbed(String stUrl) {
        init(stUrl);
    }
    
    /**
     * Creates new BrTabbed object with an empty web-page 
     */
    public BrTabbed() {
        init(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane = new javax.swing.JTabbedPane();
        brCurrentPage = new org.jdic.web.BrComponent();

        jTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        javax.swing.GroupLayout brCurrentPageLayout = new javax.swing.GroupLayout(brCurrentPage);
        brCurrentPage.setLayout(brCurrentPageLayout);
        brCurrentPageLayout.setHorizontalGroup(
            brCurrentPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 295, Short.MAX_VALUE)
        );
        brCurrentPageLayout.setVerticalGroup(
            brCurrentPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 175, Short.MAX_VALUE)
        );

        jTabbedPane.addTab("tab1", brCurrentPage);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdic.web.BrComponent brCurrentPage;
    private javax.swing.JTabbedPane jTabbedPane;
    // End of variables declaration//GEN-END:variables
    //private org.jdic.web.BrComponent brCurrentPage;
    
    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport(this);
    
    /**
     * Adds a PropertyChangeListener to the listener list.
     * @param ls The listener to add.
     */
    @Override
    public void addPropertyChangeListener(java.beans.PropertyChangeListener ls) {
        propertyChangeSupport.addPropertyChangeListener(ls);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     * @param ls The listener to remove.
     */
    @Override
    public void removePropertyChangeListener(java.beans.PropertyChangeListener ls) {
        propertyChangeSupport.removePropertyChangeListener(ls);
    }

    
    /**
     * Getter for property CurrentPage 
     * @return the BrComponent object
     */
    public BrComponent getCurrentPage() {
        return brCurrentPage;
    }

    /**
     * Collection of property names that are long-time-calculated.
     */
    private String longCalculatedProps[] = {"HTML", "XHTML", "sprites"};
    
    /**
     * Collection of property names that don't change while changing of 
     * active HTML page.
     */
    HashSet<String> hsPropNotForReplication = new HashSet<String>(
        Arrays.asList(longCalculatedProps)
    );
    
    /**
     * Setter for property CurrentPage
     * @param brCurrentPage
     */
    public void setCurrentPage(BrComponent _brCurrentPage)  {
        BrComponent oldCurrentPage = brCurrentPage;
        brCurrentPage = _brCurrentPage;
        propertyChangeSupport.firePropertyChange(
                "CurrentPage", 
                oldCurrentPage, 
                brCurrentPage);
        
        try {
            java.beans.BeanInfo bi = java.beans.Introspector.getBeanInfo(
                    brCurrentPage.getClass(), 
                    JComponent.class
            );
            
            for( PropertyDescriptor pd: bi.getPropertyDescriptors() ){
                String name = pd.getName();
                if( hsPropNotForReplication.contains(name) )
                    continue;
                Method id = pd.getReadMethod();
                if(null!=id){
                    //System.out.println(name);                    
                    propertyChangeSupport.firePropertyChange(
                            name, 
                            id.invoke(oldCurrentPage),
                            id.invoke(brCurrentPage));
                }    
            }
        } catch (Exception ex) {
            Logger.getLogger(BrTabbed.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void closePage(int iPageIndex){    
        if( isLastTab() ){
            setURL((String)null);
        } else {
            jTabbedPane.removeTabAt(iPageIndex);
        }    
    }
    
    public void closeCurrentPage(){
        closePage(jTabbedPane.getSelectedIndex());
    }
    
   public void setupTabLabel(final BrComponent brComponent, String stUrl){
        final JTabPanel lb = new JTabPanel(this, stUrl);  
        brComponent.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String stPN = evt.getPropertyName();
                if(stPN.equals("windowTitle")) {
                    String stNV = (String)evt.getNewValue();
                    if(null==stNV)
                        stNV = "";
                    String st[] = stNV.split(",");
                    lb.lbAddress.setText( st[0] );
                    lb.pbDownloadDoc.setString( st[0] );
                } else if(stPN.equals("progressBar")) {
                    String stNV = (String)evt.getNewValue();
                    if(null==stNV)
                        stNV = "";
                    String st[] = stNV.split(",");
                    int iMax = Integer.parseInt(st[0]),
                        iPos = Integer.parseInt(st[1]);
                    if(0==iMax){
                        lb.pbDownloadDoc.setVisible(false);
                        lb.lbAddress.setVisible(true);
                        if(brComponent.isDocumentReady()){
                            //bnRefreshActionPerformed(null);
                        }
                    } else {
                        lb.pbDownloadDoc.setMaximum(iMax);
                        lb.pbDownloadDoc.setValue(iPos);
                        lb.pbDownloadDoc.setVisible(true);
                        lb.lbAddress.setVisible(false);
                    }
                }
                if(brComponent==brCurrentPage){
                    propertyChangeSupport.firePropertyChange(
                            evt.getPropertyName(), 
                            evt.getOldValue(), 
                            evt.getNewValue());                
                }
            }
        });     
        brComponent.addBrComponentListener(this);        
        int iPos = jTabbedPane.indexOfComponent(brComponent);
        jTabbedPane.setTabComponentAt(iPos, lb );
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                jTabbedPane.setSelectedComponent(brComponent);
        }});
    }
   
    /**
     * Opens a new tab with URL content. 
     * @param stUrl the String object with URL address
     * @return new browser component 
     */
    public BrComponent openInNewBrowserPanel(String stUrl)
    {
        //System.out.println("{openInNewBrowserPanel: " + stUrl);
        BrComponent brComponent = new BrComponent(stUrl);
        javax.swing.GroupLayout brComponentLayout = new javax.swing.GroupLayout(brComponent);
        brComponent.setLayout(brComponentLayout);
        brComponentLayout.setHorizontalGroup(
            brComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );
        brComponentLayout.setVerticalGroup(
            brComponentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );
        jTabbedPane.addTab(brComponent.getURL(), brComponent);
        setupTabLabel(brComponent, stUrl);
        //System.out.println("}openInNewBrowserPanel: " + stUrl);
        return brComponent;
    }
    
    /**
     * Checks the last browser in tab set.
     * @return <code>true</code> for single tab configuration 
     * else - <code>false</code> 
     */
    public boolean isLastTab()
    {
        return jTabbedPane.getTabCount() <= 1;
    }
    /**
     * Getter for Tabbed container
     * @return the JTabbedPane object
     */
    public JTabbedPane getTabbedPane() {
        return jTabbedPane;
    }
    
    /**
     * Getter for current browser
     * @return the BrComponent object from visible plane
     */
    public BrComponent getVisibleBrowser() {
        try{
            return (BrComponent)getTabbedPane().getSelectedComponent();
        }catch(ClassCastException e){
            //that is OK
        }        
        return null;        
    }

    /**
     * Implementation of the <code>ChangeListener</code> interface
     * @param e the event about changed page 
     */
    public void stateChanged(ChangeEvent e) {
        setCurrentPage(getVisibleBrowser());
    }

    
    //{BrComponentListener
    /**
     * The listener for notifications callbacks.
     */    
    transient BrComponentListener ieListener; //TODO: make it stackable.   
    
    /**
     * Implementation of <code>BrComponentListener</code> interface
     * @param e
     */
    public String sync(BrComponentEvent e) {
        final String[] res = new String[1];
        if(null!=ieListener){
            res[0] = ieListener.sync(e);
        }
        if(null==res[0]){
                switch (e.getID()) {
                    case BrComponentEvent.DISPID_NEWWINDOW2:
                        final BrComponent sbr = (BrComponent)e.getSource();
                        sbr.setActionFiler(0, true);
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() { public void run() {
                                    BrComponent br = openInNewBrowserPanel(null);
                                    if (null != br) {
                                        res[0] = "0," + br.getNativeHandle();
                                    }
                                    sbr.setActionFiler(0, false);
                            }});
                        } catch (Exception ex) {
                            Logger.getLogger(BrTabbed.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                }
        }    
        return res[0];
    }

    public void addBrComponentListener(BrComponentListener l) {
        ieListener = l;
    }

    public void removeBrComponentListener(BrComponentListener l) {
        if( ieListener == l ) {
            ieListener = null;
        }
    }
    //}BrComponentListener
    
    //{Delagates to current page object...  
    public void stop() {
        brCurrentPage.stop();
    }

    public  void setURL(URL url) throws FileNotFoundException {
        brCurrentPage.setURL(url);
    }

    public  void setURL(String _stURL) {
        brCurrentPage.setURL(_stURL);
    }

    public  void setHTML(String stHTML) {
        brCurrentPage.setHTML(stHTML);
    }

    public  void setHTML(InputStream _isHTMLSrc, String _stURL) {
        brCurrentPage.setHTML(_isHTMLSrc, _stURL);
    }

    public  void setEditable(boolean b) {
        brCurrentPage.setEditable(b);
    }

    public void setDebugDrawBorder(boolean debugDrawBorder) {
        brCurrentPage.setDebugDrawBorder(debugDrawBorder);
    }

    public void save(String stHTMLPath) {
        brCurrentPage.save(stHTMLPath);
    }

    public void save() {
        brCurrentPage.save();
    }

    public void refresh() {
        brCurrentPage.refresh();
    }

    public void open(String stHTMLPath) {
        brCurrentPage.open(stHTMLPath);
    }

    public void open() {
        brCurrentPage.open();
    }

    public boolean isToolbarChanged() {
        return brCurrentPage.isToolbarChanged();
    }

    public boolean isGoForwardEnable() {
        return brCurrentPage.isGoForwardEnable();
    }

    public boolean isGoBackEnable() {
        return brCurrentPage.isGoBackEnable();
    }

    public boolean isEditable() {
        return brCurrentPage.isEditable();
    }

    public boolean isDocumentReady() {
        return brCurrentPage.isDocumentReady();
    }

    public boolean isDebugDrawBorder() {
        return brCurrentPage.isDebugDrawBorder();
    }

    public  String getHTML() {
        return brCurrentPage.getHTML();
    }

    public void forward() {
        brCurrentPage.forward();
    }

    public void execJSLater(String code) {
        brCurrentPage.execJSLater(code);
    }

    public  String execJS(String code) {
        return brCurrentPage.execJS(code);
    }

    public void back() {
        brCurrentPage.back();
    }

    public void print() {
        brCurrentPage.print();
    }

    public  String getXHTML(boolean bWithUniqueID) {
        return brCurrentPage.getXHTML(bWithUniqueID);
    }

    public  String getXHTML() {
        return brCurrentPage.getXHTML();
    }

    public String getWindowTitle() {
        return brCurrentPage.getWindowTitle();
    }

    public  String getURL() {
        return brCurrentPage.getURL();
    }

    public String getStatusText() {
        return brCurrentPage.getStatusText();
    }

    public List getSprites() {
        return brCurrentPage.getSprites();
    }

    public String getSecurityIcon() {
        return brCurrentPage.getSecurityIcon();
    }

    public String getProgressBar() {
        return brCurrentPage.getProgressBar();
    }

    public String getNavigatedURL() {
        return brCurrentPage.getNavigatedURL();
    }
    //}Delagates to current page object...  
}
