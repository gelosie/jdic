package org.jdesktop.jdic.misc.test;

import org.jdesktop.jdic.misc.Volume;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

public class VolumeTest {

	public static void main(String[] args) {
		final Volume vol = Volume.newInstance();
		
		JButton down = new JButton("<");
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(vol.getVolume() > 0f) {
					vol.setVolume(vol.getVolume()-0.1f);
				}
			}
		});
		
		JButton up = new JButton(">");
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if(vol.getVolume() < 1f) {
					vol.setVolume(vol.getVolume()+0.1f);
				}
			}
		});
		
		final JLabel cur_vol = new JLabel("?");
		
		
		PropertyChangeListener pcl = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("evt = " + evt);
				final Float vol = (Float)evt.getNewValue();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						cur_vol.setText(""+vol);
					}
				});
			}
		};
		
		vol.addPropertyListener(pcl);
	
		
		JFrame frame = new JFrame("Volume Test");
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(down);
		frame.getContentPane().add(cur_vol);
		frame.getContentPane().add(up);
		frame.pack();
		frame.setSize(400,200);
		frame.show();
	}
}
