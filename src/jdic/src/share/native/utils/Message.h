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
#define JEVENT_GOBACK            8
#define JEVENT_GOFORWARD         9
#define JEVENT_REFRESH           10
#define JEVENT_STOP              11
#define JEVENT_GETURL            12
#define JEVENT_FOCUSGAINED       13
#define JEVENT_FOCUSLOST         14
#define JEVENT_GETCONTENT        15
#define JEVENT_SETCONTENT        16
#define JEVENT_EXECUTESCRIPT     17

// C++ -> Java, must keep same with WebBrowserEvent.java
#define CEVENT_BEFORE_NAVIGATE	    3001
#define CEVENT_BEFORE_NEWWINDOW	    3002
#define CEVENT_DOWNLOAD_STARTED	    3003
#define CEVENT_DOWNLOAD_COMPLETED   3004
#define CEVENT_DOWNLOAD_PROGRESS    3005
#define CEVENT_DOWNLOAD_ERROR       3006
#define CEVENT_DOCUMENT_COMPLETED   3007

#define CEVENT_RETURN_URL           3021
#define CEVENT_COMMAND_STATE_CHANGE 3022
#define CEVENT_TITLE_CHANGE         3023
#define CEVENT_STATUSTEXT_CHANGE    3024

#define CEVENT_INIT_FAILED          3041
#define CEVENT_INIT_WINDOW_SUCC     3042

#define CEVENT_FOCUS_REQUEST        3043
#define CEVENT_DISTORYWINDOW_SUCC   3044

#define CEVENT_GETCONTENT           3061
#define CEVENT_SETCONTENT           3062
#define CEVENT_EXECUTESCRIPT        3063

// Socket message delimiters, must keep same with MsgClient.java
#define MSG_DELIMITER         "</html><body></html>"
// a long message may be devided into several pieces: 
// a head piece, multiple middle pieces and an end piece.
#define MSG_DELIMITER_        "</html><body></html>_"
#define MSG_DELIMITER_HEAD    "</html><body></html>_head"
#define MSG_DELIMITER_MIDDLE  "</html><body></html>_middle"
#define MSG_DELIMITER_END     "</html><body></html>_end"

#endif
