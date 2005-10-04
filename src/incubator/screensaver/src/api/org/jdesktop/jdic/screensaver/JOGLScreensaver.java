// Copyright (c) 2004-2005 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

package org.jdesktop.jdic.screensaver;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import net.java.games.jogl.Animator;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLCapabilities;
import net.java.games.jogl.GLDrawableFactory;
import net.java.games.jogl.GLEventListener;

/**
 * Base class for OpenGL (JOGL) screensavers.
 * These screensavers use the JOGL API to render screensavers.  Subclasses
 * must provide an implementation of GLEventListener methods and be able
 * to return a GLCapabilities object from which the GLCanvas is created.
 *
 * @author Mark Roth
 */
public abstract class JOGLScreensaver 
    extends ScreensaverBase
    implements GLEventListener
{
    /** Logging support */
    private static Logger logger = 
        Logger.getLogger("org.jdesktop.jdic.screensaver");
    
    /** True if the GLCanvas has been added to the window */
    private boolean canvasAdded = false;
    
    /** The JOGL Animator driving the animation for the screensaver */
    private Animator animator = null;
    
    /** The JOGL Canvas the screensaver is drawing to */
    private GLCanvas canvas = null;
    
    /**
     * Called by the native layer to render the next frame.
     * This method is largely ignored for JOGL screensavers.
     * All painting happens in display() and is controlled by an Animator.
     */
    public final void renderFrame() {
        try {
            super.renderFrame();
        }
        catch(Throwable t) {
            logger.log(Level.WARNING, 
                "Screensaver threw exception while rendering frame: ", t);
        }
    }
    
    /**
     * Initializes the GLCanvas and attaches it to the screensaver.
     * First the subclass is queried for a GLCapabilities object by calling
     * getGLCapabilities().  Next, the GLCanvas is created and attached
     * to the screensaver preview window or fullscreen window.  Finally,
     * an Animator() is created to drive the animation and the screensaver
     * is attached as a GLEventListener to the screensaver.  Subclasses
     * can perform any initialization in the init(GLDrawable) method.
     */
    public final void init() {
        if(!canvasAdded) {
            canvasAdded = true;
            GLCapabilities capabilities = getGLCapabilities();
            this.canvas = 
                GLDrawableFactory.getFactory().createGLCanvas(capabilities);
            canvas.addGLEventListener(this);

            final Component component = getContext().getComponent();
            if(!(component instanceof Container)) {
                logger.log(Level.WARNING,
                    "Screensaver component was not a container.");
            }
            else {
                // This needs to be done on the Swing thread or the
                // application will crash.
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            Container container = (Container)component;
                            container.add(canvas);
                            // Since the container may already be shown,
                            // we need to validate to get the GL canvas 
                            // to show.
                            container.validate();

                            // Create the JOGL Animator which will drive 
                            // the animation of this screensaver (smooth!)
                            animator = new Animator(canvas);
                            animator.start();
                            
                            // Add mouse motion event to exit when mouse moves.
                            // The JOGL canvas is hidden which causes the next
                            // move of the mouse to be passed to Windows, 
                            // which causes a clean exit of the screensaver.
                            canvas.addMouseMotionListener(
                                new MouseMotionListener() {
                                    // For some reason, we get a spurious
                                    // mouse movement right at the beginning,
                                    // so ignore that one.
                                    int numMoves = 0;
                                    public void mouseDragged(MouseEvent e) {
                                    }
                                    public void mouseMoved(MouseEvent e) {
                                        SwingUtilities.invokeLater(
                                            new Runnable() {
                                                public void run() {
                                                    if(context.isFullScreen()) {
                                                        if(numMoves > 0) {
                                                            canvas.setVisible(
                                                                false);
                                                        }
                                                        numMoves++;
                                                    }
                                                }
                                            }
                                        ); // End SwingUtilities.invokeLater()
                                    }
                                }
                            ); // end canvas.addMouseMotionListener()
                        }
                    }
                ); // end SwingUtilities.invokeLater()
            }
        }
    }
    
    /**
     * Cleanly shuts down the OpenGL subsystem.
     */
    public final void destroy() {
        if(animator != null) {
            animator.stop();
        }
        joglScreensaverDestroy();
        System.exit(0);
    }
    
    /**
     * Subclasses can optionally override this method to perform any
     * orderly shutdown procedures before the screensaver exits.
     * The animator will have been fully stopped at this point.
     *
     * @since 0.3
     */
    protected void joglScreensaverDestroy() {
    }
    
    /**
     * The GLCapabilities object from which the GLCanvas is constructed.
     * Subclasses can override this method to specifiy their own
     * customized GLCapabilities object.  The default implementation 
     * returns a default GLCapabilities instance.
     *
     * @return The GLCapabilities object from which the GLCanvas is
     *     constructed.
     */
    protected GLCapabilities getGLCapabilities() {
        return new GLCapabilities();
    }
    
}
