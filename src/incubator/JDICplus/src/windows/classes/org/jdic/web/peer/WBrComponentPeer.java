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

package org.jdic.web.peer;

import sun.awt.windows.WComponentPeer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.peer.ComponentPeer;
import java.awt.image.*;
import java.io.InputStream;

import org.jdic.web.event.BrComponentEvent;
import org.jdic.web.BrComponent;

import javax.swing.*;
import org.jdic.NativeLoadMgr;

/**
 * Implementation of BrComponentPeer interface for Internet Explorer.
 * @author uta
 */
public class WBrComponentPeer implements BrComponentPeer {
    
    //Initialize JNI field and method IDs
    private static native void initIDs();
    
    private static final DirectColorModel directColorModel =
        new DirectColorModel(
            24,
            0x00FF0000,  /* red mask   */
            0x0000FF00,  /* green mask */
            0x000000FF   /* blue mask  */
        );


    private static final int[] bandmasks = new int[] {
        directColorModel.getRedMask(),
        directColorModel.getGreenMask(),
        directColorModel.getBlueMask()};


    private Component parentHW = null;
    // Toolkit & peer internals
    public WBrComponentPeer(BrComponent target) {
        this.target = target;
    }

    boolean editable = false;
    void enableEditing(boolean e) {
        execJS(":document.body.contentEditable=" + e + ";");
    }
    boolean isEditable() {
        //System.out.println(execJS("document.body.contentEditable=true"));
        return Boolean.parseBoolean(execJS("document.body.contentEditable"));
    }

    // BrComponentPeer implementation
    boolean documentReady = false;
    public void setEditable(boolean _editable) {
        if(_editable != editable){
            editable = _editable;
            enableEditing(editable);
        }
    }
    public boolean getEditable() {
        return editable;
    }

    public String getURL() {
        return execJS("document.URL"); 
    }

    public String getNativeHTML() {
        //FF has jast an innerHTML :(
        //return execJS("document.documentElement.outerHTML");
        return "<HTML>" + execJS("document.documentElement.innerHTML") + "</HTML>";
    }

    public String getNativeXHTML() {
        return getNativeXHTML(false);
    }
    public String getNativeXHTML(boolean bWithUniqueID) {
        String stXHTML = execJS(
                "#function copyNode(xmlDoc, xmlParent, htmlNode)\n" +
                "{\n" +
                "  var htmlNode = htmlNode.firstChild;\n" +
                "  if(null==htmlNode){\n" +
                "      return false;\n" +
                "  }\n" +
                "  for(; null!=htmlNode; htmlNode = htmlNode.nextSibling){\n" +
                "        var stTagLow = htmlNode.nodeName.toLowerCase();\n" +
                //ditry HTML
                "        if( -1!=stTagLow.indexOf(\'/\') ||\n" +
                "            -1!=stTagLow.indexOf('<') ||\n" +
                "            -1!=stTagLow.indexOf('!') ||\n" +
                "            -1!=stTagLow.indexOf('?') ||\n" +
                "            -1!=stTagLow.indexOf('>')) {\n" +
                "            continue;\n" +
                "        }\n" +
                "        var xmlNode = null;\n" +
                "        if( \"#comment\"==stTagLow ){\n" +
                "            xmlNode = xmlDoc.createComment(htmlNode.data" + 
                //<, >, -- are forbidden in XML, but available in HTML
                //".replace(/<+/g, \"&lt;\")" +
                //".replace(/>+/g, \"&gt;\")" +
                ".replace(/(--)+/g, \"&#45;&#45;\"));\n" + //works
                "        } else if( \"#text\"==stTagLow ){\n" +
                "            xmlNode = xmlDoc.createTextNode(htmlNode.nodeValue);\n" +
                "        } else {\n" +
                "            xmlNode = xmlDoc.createElement(stTagLow);\n" +
                "            var htmlAttrs = htmlNode.attributes;\n" +
                "            var xmlAttrs = xmlNode.attributes;\n" +
                "            for(var i=0; i < htmlAttrs.length; ++i){\n" +
                "                var htmlAttr = htmlAttrs.item(i);\n" +
                "                if( !htmlAttr.specified || " +
                "                    \"style\"==htmlAttr.nodeName || " +
                "                    \"javaEval\"==htmlAttr.nodeName || " +
                "                    null==htmlAttr.nodeValue || " +                
                "                    \"inherit\"==htmlAttr.nodeValue)\n" +
                "                    continue;\n" +
                "                var xmlAttr = xmlDoc.createAttribute(htmlAttr.nodeName);\n" +
                "                xmlAttr.nodeValue = htmlAttr.nodeValue.toString();\n" +
                "                xmlAttrs.setNamedItem(xmlAttr);\n" +
                "            }\n" +
                "            var stStyle = htmlNode.style.cssText;\n" +
                "            if(0!=stStyle.length){\n" +
                "                xmlNode.setAttribute(\"style\", stStyle);\n" +
                "            }\n" +
                //{uniquie ID in IE for all container nodes. Could be used as index in document.all collection.                
               (bWithUniqueID 
               ?"            stStyle = htmlNode.uniqueID;\n" +                
                "            if(null!=stStyle && 0!=stStyle.length){\n" +
                "                xmlNode.setAttribute(\"_uniqueID\", stStyle);\n" +
                "            }\n" 
               :"") +    
                //}
                "            if(\"script\"==stTagLow || \"style\"==stTagLow){\n" +
                "                xmlNode.appendChild(xmlDoc.createTextNode(htmlNode.innerHTML));\n" +
                "            } else if( !copyNode(xmlDoc, xmlNode, htmlNode) && \n" +
                "                ( stTagLow==\"div\" || stTagLow==\"p\" || stTagLow==\"td\" || stTagLow==\"th\" )\n" +
                "            ){\n" +
                "                xmlNode.appendChild(xmlDoc.createTextNode(\"\\xa0\"));\n" +
                "            }\n" +
                "      }\n" +
                "      xmlParent.appendChild( xmlNode );\n" +
                "  }\n" +
                "  return true;\n" +
                "};\n" +
                "\n" +
                "function getXHTML()\n" +
                "{\n" +
                "    var xmlDoc = new ActiveXObject(\"Msxml2.FreeThreadedDOMDocument.3.0\");\n" +
                "    xmlDoc.async = false;\n" +
                "    copyNode(xmlDoc, xmlDoc, document);\n" +
                "    return xmlDoc.xml;\n" +
                "}\n" +
                "\n" +
                "document.documentElement.setAttribute(\"javaEval\", getXHTML());"
        );
        //IE mandatory compartibility
        //System.out.print(stXHTML);
        stXHTML = stXHTML.replace("></br>", "/>");//<br></br> -> <br/> 
        stXHTML = stXHTML.replace("></img>", "/>");//<img ...></img> -> <img/>
        stXHTML = stXHTML.replace("<title/>", "<title></title>");
        stXHTML = stXHTML.replace("<body/>", "<body></body>");
        return stXHTML;
    }

    public void acceptTargetURL() {
        execJS("##setNativeDraw(" + (BrComponent.DRAW_PRIVATE==target.paintAlgorithm) + ")");
        documentReady = false;
        setURL(target.stURL, target.isHTMLSrc);
    }

    public void validate() {
        if(target.isVisible()) {
            clearRgn();
            Rectangle cr;
            Component[] cms = target.getComponents();
            for (Component cm : cms) {
                if (cm.isLightweight() && cm.isVisible()) {
                    cr = cm.getBounds();
                    clipChild(cr.x, cr.y, cr.width, cr.height);
                    //System.out.println("child" + cm);
                }
            }
            cr = target.getBounds();
            updateTransparentMask(cr.x, cr.y, cr.width, cr.height);
        }
    }

    //native
    public native void clearRgn();
    public native void clipChild(int top, int left, int width, int height);
    native long create(long hwnd);
    public native void destroy();
    public native void setURL(String stURL, InputStream is);
    public native String execJS(String code);
    public native void updateTransparentMask(int top, int left, int width, int height);
    public native void blockNativeInputHandler(boolean dropNativeAction);
    public native void nativeDraw(int x, int y, int width, int height);
    public native void nativePosOnScreen(int x, int y, int width, int height);
    private native int[] ImageData(int x, int y, int width, int height);
    public native void laizyUpdate(int msDelay);
    public native void nativeSetTransparent(boolean bTransparent);    
    public native long nativeSendMouseEvent(int wnd, int wm, int wParam, int lParam);
    public native void nativeReleaseMouseCapture();
    public native int  setActionFiler(int flag, boolean busyState);

    public void reshape(int x, int y, int width, int height)
    {
        //System.out.println("{reshape x:" + x + " y:" + y + " w:" + width + " h:" + height);
        Point pt = new Point(x, y);
        SwingUtilities.convertPointToScreen(pt, target.getParent());
        nativePosOnScreen(
                pt.x,
                pt.y,
                width,
                height);
        //System.out.println("}reshape x:" + x + " y:" + y);
    }
    private void handlePaint(int x, int y, int w, int h) {
        //if ( !sun.awt.ComponentAccessor.getIgnoreRepaint(tg) ) //exists only under JDK 1.7
        if( target.isVisible() ){
            final Rectangle rc = new Rectangle(x, y, w, h);
            javax.swing.SwingUtilities.invokeLater ( new Runnable() {
                public void run() {
                    //System.out.printf("{p x:%d, y:%d, w:%d, h:%d", rc.x, rc.y, rc.width, rc.height);
                    target.repaint(rc.x, rc.y, rc.width, rc.height);
                    //System.out.println("}pppppppppp");
                }
            });
        }
    }

    private void refreshHard()
    {
        javax.swing.SwingUtilities.invokeLater ( new Runnable() {
            public void run() {
                //System.out.printf("{refreshHard");                
                target.reshape(target.getX(), target.getY(), target.getWidth(), target.getHeight() );
                //System.out.println("}refreshHard");
            }
        });
    }

    private void focusMove(final boolean bNext)
    {
        javax.swing.SwingUtilities.invokeLater ( new Runnable() {
            public void run() {
                KeyboardFocusManager fm = FocusManager.getCurrentKeyboardFocusManager();
                if(bNext) {
                   fm.focusNextComponent(target);
                } else {
                   fm.focusPreviousComponent(target);
                }    
            }
        });
    }


    boolean isFocusOwner = false;
    public boolean hasFocus() {
        return isFocusOwner;
    }

    public void focusGain(boolean  bKeyboardFocus) {
        if(!hasFocus()){
            //System.out.println("##setFocus");
            execJS("##setFocus(" + bKeyboardFocus + ")" );
        }
    }

    public final static int WND_TOP = 0;
    public final static int WND_PARENT = 1;
    public final static int WND_IE = 2;

    public long sendMouseEvent(MouseEvent e) {
        long ret = 0;
        int wm = 0;
        final int modifiers = e.getModifiers();        
        int wParam =
            (( 0 != (modifiers & MouseEvent.CTRL_MASK) ) ? 0x0008/*MK_CONTROL*/ : 0)
            | (( 0 != (modifiers & MouseEvent.SHIFT_MASK) ) ? 0x0004/*MK_SHIFT*/ : 0)
            | (( 0 != (modifiers & MouseEvent.BUTTON1_DOWN_MASK) ) ? 0x0001/*MK_LBUTTON*/ : 0)
            | (( 0 != (modifiers & MouseEvent.BUTTON3_DOWN_MASK) ) ? 0x0002/*MK_RBUTTON*/ : 0)
            | (( 0 != (modifiers & MouseEvent.BUTTON2_DOWN_MASK) ) ? 0x0010/*MK_MBUTTON*/ : 0);
        int lParam = e.getX() + (e.getY() << 16 );
        if( MouseEvent.MOUSE_PRESSED==e.getID() ){
            switch(e.getButton()){
            case MouseEvent.BUTTON1:
                wm = 0x0202; //WM_LBUTTONUP
                break;
            case MouseEvent.BUTTON3:
                wm = 0x0205; //WM_RBUTTONUP
                break;
            case MouseEvent.BUTTON2:
                wm = 0x0208; //WM_MBUTTONUP
                break;
            }
            nativeSendMouseEvent(WND_PARENT, wm, wParam, lParam);            
        }
        nativeSetTransparent(false);
        //System.out.println("MouseEvent  m:" + e.getModifiers() + " " + e);
        switch(e.getID()){
        case MouseEvent.MOUSE_PRESSED:
            switch(e.getButton()){
            case MouseEvent.BUTTON1:
                wm = 0x0201; //WM_LBUTTONDOWN             
                wParam |= 0x0001/*MK_LBUTTON*/; 
                break;
            case MouseEvent.BUTTON3:
                wm = 0x0204; //WM_RBUTTONDOWN
                wParam |= 0x0002/*MK_RBUTTON*/;
                break;
            case MouseEvent.BUTTON2:
                wm = 0x0207; //WM_MBUTTONDOWN
                wParam |= 0x0010/*MK_MBUTTON*/;
                break;
            }
            break;
        case MouseEvent.MOUSE_RELEASED:
            switch(e.getButton()){
            case MouseEvent.BUTTON1:
                wm = 0x0202; //WM_LBUTTONUP
                break;
            case MouseEvent.BUTTON3:
                wm = 0x0205; //WM_RBUTTONUP
                break;
            case MouseEvent.BUTTON2:
                wm = 0x0208; //WM_MBUTTONUP
                break;
            }
            break;
        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
            wm = 0x0200; //WM_MOUSEMOVE
            break;
        case MouseEvent.MOUSE_ENTERED:
            wm = 0x02A1; //WM_MOUSEHOVER
            break;            
        case MouseEvent.MOUSE_EXITED:
            if( hasFocus() ){
                //System.out.println("MouseEvent skipped");
            } else {
                wm = 0x02A3; //WM_MOUSELEAVE
            }    
            break;
        }
        if(0!=wm && !hasFocus()){
            int iHitTest = (int)nativeSendMouseEvent(
                WND_IE,
                0x0084,//WM_NCHITTEST
                0, e.getXOnScreen() + (e.getYOnScreen() << 16 ));
            if( MouseEvent.MOUSE_PRESSED==e.getID() )
                focusGain(false);
            /*
            if( MouseEvent.MOUSE_PRESSED==e.getID() ){
                //System.out.println("##focusGain!");
                long res = nativeSendMouseEvent(
                    WND_IE,
                    0x0021,//WM_MOUSEACTIVATE
                    (int)((WComponentPeer)target.getTopLevelAncestor().getPeer()).getHWnd(),
                    iHitTest //HTCLIENT
                    + (wm << 16 ));
                if(1==res || 2==res){
                    focusGain(false);                    
                }
                if(4==res || 2==res){
                    //discad message
                    return ret;
                }
            }*/
            nativeSendMouseEvent(
                WND_IE,
                0x0020,//WM_SETCURSOR
                0,
                iHitTest + (wm << 16 ));
            ret = nativeSendMouseEvent(
                 WND_IE,
                 wm,
                 wParam,
                 lParam);
        }
        if( !hasFocus() ){
            nativeSetTransparent(true);
        }            
        return ret;
    }

    native public void setEnabled(boolean enabled);
    native public void setVisible(boolean aFlag);

//    public boolean requestFocusInWindow() {
//        return true;  //To change body of implemented methods use File | Settings | File Templates.
//    }
    /**
     * Synchronous callback for notifications from native code.
     * @param iId the event identifier - <code>BrComponentEvent.DISPID_XXXX</code> const
     * @param stName the event name of (optional)
     * @param stValue the event paramenter(s) (optional)
     * @return the application respont
     */
    private String postEvent(
            int iId,
            String stName,
            String stValue)
    {
        switch(iId){
        case BrComponentEvent.DISPID_REFRESH:
            refreshHard();
            break;
        case BrComponentEvent.DISPID_DOCUMENTCOMPLETE:
            if(isEditable()!=editable){
                enableEditing(editable);
            } else if(editable) {
                refreshHard();
            }
            documentReady = true;
            break;
        case BrComponentEvent.DISPID_ONFOCUCHANGE:
            isFocusOwner = Boolean.parseBoolean(stValue);
            break;
        case BrComponentEvent.DISPID_ONFOCUSMOVE:
            focusMove(Boolean.parseBoolean(stValue));
            break;
        case BrComponentEvent.DISPID_PROGRESSCHANGE:
            break;
        }        
        return target.processBrComponentEvent(
                new BrComponentEvent(target, iId, stName, stValue));
    }

    public Image getImage(int x, int y, int width, int height){
        BrComponent ie = (BrComponent)target;
        if(
            x < 0 ||
            y < 0 ||
            (x+width)>(ie.getX()+ie.getWidth()) ||
            (y+height)>(ie.getY()+ie.getHeight()) )
        {
            return null;
        }
        int[] imageData = ImageData(x, y, width, height);
        if(null==imageData || 0==imageData.length) {
            return null;
        }   

        int len = imageData.length - 2;
        int rwidth = imageData[len];
        int rheight = imageData[len + 1];

        DataBufferInt buffer = new DataBufferInt(imageData, len);
        WritableRaster raster = Raster.createPackedRaster(buffer, rwidth,
                                                          rheight, rwidth,
                                                          bandmasks, null);

        return new BufferedImage(directColorModel, raster, false, null);
    }
    
    public void paintClientArea(Graphics g, int iPaintHint)
    {
        if( target.isVisible()  ){
            Rectangle updateRect = g.getClipBounds();            
            if(null==updateRect){
                updateRect = target.getBounds();
            }
            if( null!=updateRect ){            
                if(updateRect.x < 0){
                    updateRect.width += updateRect.x;
                    updateRect.x = 0;
                }
                if(updateRect.y < 0){
                    updateRect.height += updateRect.y;
                    updateRect.y = 0;
                }
                if( 0<updateRect.width 
                    && 0<updateRect.height)
                {
                    if(BrComponent.DRAW_NATIVE_BEFORE_CONTENT==iPaintHint ){
                        nativeDraw(
                            updateRect.x,
                            updateRect.y,
                            updateRect.width,
                            updateRect.height);
                    } else if(BrComponent.DRAW_DOUBLE_BUFFERED==iPaintHint ){
                        Image updateImage = getImage(
                            updateRect.x,                                                 
                            updateRect.y,
                            updateRect.width,
                            updateRect.height);
                        if( null!=updateImage ){
                            g.drawImage(
                                updateImage,
                                updateRect.x,
                                updateRect.y,
                                null
                            );
                        }
                    }
                }    
            }    
        }    
    }

    public long getNativeHandle() {
        return data;
    }
    
    protected int notifyCounter = 0;    
    protected boolean showOnZero = true;
    
    public void onAddNotify() {
        if(0 == notifyCounter){
            if( !BrComponent.DESIGN_MODE ) {
                if( NativeLoadMgr.loadLibrary("jdicWeb") ) {
                    //init IDs for native callbacks on the first load
                    initIDs();
                }   
                for( Container c = target.getParent(); null != c; c = c.getParent() )
                {
                    ComponentPeer cp = c.getPeer();
                    if( (cp instanceof WComponentPeer) ){
                        parentHW = c;
                        data = create( ((WComponentPeer)cp).getHWnd() );
                        acceptTargetURL();
                        setEditable(target.isEditable());
                        break;
                    }
                }
            } else if( !target.isMandatoryDispose() ){
                setVisible(showOnZero);
            }
        }
        ++notifyCounter;
    }

    public void onRemoveNotify() {
        --notifyCounter;                
       if( 0==notifyCounter ){
            showOnZero = target.isVisible();
            if( !target.isMandatoryDispose() ) {
                if(showOnZero){
                   setVisible(false);
                }
            } else if( 0!=data ) {
               destroy();
               data = 0;
            }
        }
    }

    public boolean isNativeReady() {
        return 0!=data;
    }

    public boolean isSelfPaint()
    {
        return false;
    }
    
    public JComponent getCentralPanel() {
        return target;
    }

    BrComponent target;
    long        data = 0;

}    
