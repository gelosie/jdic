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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.JOptionPane;

/**
 * Encapsulates a screensaver in an applet.  This is a convenient way to
 * display a 'screenshot' of a screensaver in a web-page.
 *
 * Usage is fairly straightforward.  Example
 * <pre>
 * &lt;applet
 *     archive='saverbeans-api.jar,saver/bouncingline.jar'
 *     class='org.jdesktop.jdic.screensaver.ScreensaverApplet'
 *     alt='Java must be enabled to see this screensaver!'
 *     codebase='.'
 *     width=400
 *     height=300&gt;
 * &lt;param name='saverClass' value='org.jdesktop.jdic.screensaver.bouncingline.BouncingLine'&gt;
 * &lt;param name='saverParameters' value=''&gt;
 * &lt;param name='development' value='true'&gt;
 * &lt;img src='starzoom-thumb.gif' width=160 height=120&gt;
 * &lt;p&gt;This screensaver requires the Java Plug-In!
 * &lt;/applet&gt;
 * </pre>
 *
 * Note that you need to list the Screensaver jar in the 'archives'
 * attribute, the 'class' is the ScreensaverApplet itself, which uses
 * the 'saverClass' parameter as the name of the SimpleSaver
 * derivative you wish to display.
 *
 * Saver parameters can be passed to the screensaver using the
 * same format as the command line in the 'saverParameters' attribute.
 * set 'development' to true for extra information.
 * Both these parameters are optional.
 *
 * @author Andrew Thompson
 */
public class ScreensaverApplet
    extends javax.swing.JApplet
{

	/** The applet info. */
	final String APPLET_INFO =
		"ScreensaverApplet by Andrew Thomspon, " +
		"part of the SaverBeans SDK";

	/** The applet's parameter info. */
	final String[][] PARAM_INFO = {
		{ "saverClass", "string", "fully qualified classname of screensaver" },
		{ "saverParameters", "string", "saver parameter names/values separated by '+'" },
		{ "development", "string", "supply 'true' for detailed development messages" }
	};

	/** Gives more detailed testing messages in a pop-up. */
	boolean development = true;

    /** The screensaver being run in this frame */
    private ScreensaverBase screensaver;

    /** The screensaver context object */
    private ScreensaverContext context;

    /** The width of the screensaver last time we checked */
    private int prevWidth;
    /** The height of the screensaver last time we checked */
    private int prevHeight;

	/** The number of 'frames per second', at which
	to render this animation. */
    public static final int FPS = 60;

    /**
     * Creates the applet to show the given screensaver.
     * Starts the screensaver right away.
     */
    public void init() {

		String developmentParam = getParameter(PARAM_INFO[2][0]);
		// initialise the development switch
		development = (developmentParam!=null &&
			developmentParam.trim().equalsIgnoreCase("true"));
        try {
            String screensaverClassName = getParameter( PARAM_INFO[0][0] );
            if (screensaverClassName==null) {
				usage("You must supply the <param name='" +
				PARAM_INFO[0][0] + "' value='" + PARAM_INFO[0][2] + "'>");
				throw new RuntimeException(PARAM_INFO[0][0] + " not supplied");
			}

			String paramsRaw = getParameter(PARAM_INFO[1][0]);
			String params = (paramsRaw==null ? "" : paramsRaw);

            // Create screensaver instance
            Class screensaverClass = Class.forName(screensaverClassName);
            if(!ScreensaverBase.class.isAssignableFrom(screensaverClass)) {
                System.err.println("Error: Class " + screensaverClassName +
                    " is not a subclass of ScreensaverBase");
				throw new RuntimeException("No Class");
            }
            ScreensaverBase screensaver =
                (ScreensaverBase)screensaverClass.newInstance();

			initComponents();

			// Initialize and set context
			this.screensaver = screensaver;
			this.context = createContext(params);
			screensaver.baseInit(context);

			String names[] = screensaverClassName.split("[.]"),
				name = "";
			System.out.println( "screensaverClassName: " +
				screensaverClassName );
			if ( names.length>1 ) {
				if ( names[names.length-1].equals("class") ) {
					name = names[names.length-2] + " started ";
				} else {
					name = names[names.length-1];
				}
			} else name = names[0];

			// Create timer to call render loop
			// XXX - Delay hard-coded to 60 FPS for now
			Timer t = new Timer(1000/FPS,
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ScreensaverApplet.this.screensaver.renderFrame();
					}
				});
			t.start();

			//new SettingsDialog();
			SettingsDialog d = null;
			try {
	            d = new SettingsDialog( "<?xml version='1.0' encoding='UTF-8'?><screensaver name='bouncingline' _label='Bouncing Line (Java)'><command arg='-root'/><command arg='-jar bouncingline.jar'/><command arg='-class org.jdesktop.jdic.screensaver.bouncingline.BouncingLine'/><file id='jdkhome' _label='Java Home (blank=auto)' arg='-jdkhome %' /><select id='color'><option id='blue' _label='Blue Line' /> <!-- default --><option id='green' _label='Green Line' arg-set='-color #00ff00' /><option id='red' _label='Red Line' arg-set='-color #ff0000' /></select><_description>Very simple line bouncing screensaver written in Java.This is meant as a base example for developers to write their own screensavers in Java but it can be oddly relaxing to watch anyway!</_description></screensaver>" );
			} catch (Exception e) {
			}
            d.setSize(640,480);
            d.setLocationRelativeTo(null);
            d.setVisible(true);

			showStatus( name + " started - " + getAppletInfo() );
        }
        catch(ClassNotFoundException e) {
			showError("Can't find screensaver ", e);
        }
        catch(InstantiationException e) {
			showError("Can't instantiate screensaver ", e);
        }
        catch(IllegalAccessException e) {
			showError("Can't instantiate screensaver ", e);
        }
        catch (RuntimeException e) {
			showError("No screensaver named ", e);
		}
    }

	/** Dumps the string and error message to system error stream,
	shows it in a pop-up message if development swith enabled. */
    private void showError(String s, Exception e) {
		System.err.println("Can't instantiate screensaver: " +
                e.getMessage());
        if ( development ) {
			JOptionPane.showMessageDialog(this, s + ": " + e.getMessage(),
				"ScreensaverApplet Error", JOptionPane.WARNING_MESSAGE );
	        e.printStackTrace();
		}
	}

	/** Returns information about this applet. */
    public String getAppletInfo() {
		return APPLET_INFO;
	}

	/** Returns information about the parameters
	that are understood by this applet. */
    public String[][] getParameterInfo() {
		return PARAM_INFO;
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        showStatus("SaverBeans Screensaver Applet");

        validate();
    }//GEN-END:initComponents

	/**
	* Display the error message and dump it to System.err
	*/
    private void usage(String s) {
        System.err.println("Error: " + ScreensaverApplet.class.getName() +
            " see applet parameters for usage.");
        showStatus( s );
    }

    /**
     * Creates the screensaver context object
     */
    private ScreensaverContext createContext(String params) {
        ScreensaverContext result = new ScreensaverContext();
        result.setComponent(getContentPane());
        result.getSettings().loadFromCommandline(params);
        return result;
    }
}

