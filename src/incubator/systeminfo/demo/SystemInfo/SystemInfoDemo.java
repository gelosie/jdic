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
 
import org.jdesktop.jdic.systeminfo.*;
import java.util.*;

public class SystemInfoDemo extends TimerTask {

  public void run() {
    long idleTime = SystemInfo.getSessionIdleTime();
    System.out.println("User has been idle for " + 
        (int)(idleTime/1000) + " seconds.");

    System.out.println("Is user's workstation locked? (" + SystemInfo.isSessionLocked() +")");
    
  }

  public static void main(String[] args) {
    SystemInfoDemo demo = new SystemInfoDemo();
    Timer timer = new java.util.Timer();
    
    timer.schedule(demo, 0, 1000);
  }
}