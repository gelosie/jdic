package org.jdesktop.jdic.misc.test;

import org.jdesktop.jdic.misc.Alerter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlerterTest {
	
	public static void main(String[] args) {
		
		// set up the main menubar
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Test");
		JMenuItem item = new JMenuItem("Do Alert in 4 Seconds");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.currentThread().sleep(4000);
						} catch (Exception ex) {
						}
						
						Alerter alerter = Alerter.newInstance();
						alerter.alert();
					}
				}).start();
			}
		});
		
		menu.add(item);
		menubar.add(menu);
		
		
		// show the frame
		JFrame frame = new JFrame("asdf");
		frame.setJMenuBar(menubar);
		frame.pack();
		frame.setSize(100,100);
		frame.show();
	}
}

