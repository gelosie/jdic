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

#ifndef _Message_H_
#define _Message_H_

// define the constants which are used for communication between Java & C++

// Java -> C++, must keep same with NativeEventData.java
#define JEVENT_INIT              0
#define JEVENT_CREATEWINDOW      1
#define JEVENT_DESTROYWINDOW     2
#define JEVENT_SHUTDOWN          3
#define JEVENT_SET_BOUNDS        4
#define JEVENT_NAVIGATE          5
#define JEVENT_NAVIGATE_POST     6
#define JEVENT_NAVIGATE_POSTDATA 7
#define JEVENT_GOBACK            8
#define JEVENT_GOFORWARD         9
#define JEVENT_REFRESH           10
#define JEVENT_STOP              11
#define JEVENT_GETURL            12
#define JEVENT_FOCUSGAINED       13
#define JEVENT_FOCUSLOST         14

// C++ -> Java, must keep same with WebBrowserEvent.java
#define CEVENT_BEFORE_NAVIGATE	    3001
#define CEVENT_BEFORE_NEWWINDOW	    3002
#define CEVENT_DOWNLOAD_STARTED	    3003
#define CEVENT_DOWNLOAD_COMPLETED   3004
#define CEVENT_DOWNLOAD_PROGRESS    3005
#define CEVENT_DOWNLOAD_ERROR       3006
#define CEVENT_RETURN_URL           3007
#define CEVENT_COMMAND_STATE_CHANGE 3008
#define CEVENT_FOCUSIN              3009

#define CEVENT_INIT_FAILED          3020
#define CEVENT_INIT_WINDOW_SUCC     3021

#define CEVENT_FOCUS_REQUEST        3022

#endif
