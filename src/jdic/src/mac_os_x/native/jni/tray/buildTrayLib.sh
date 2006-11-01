#!/bin/sh

#thanks to Jerome Dochez for a blog entry about the universal flags for gcc. Sure beats having to have an Xcode project for this!

gcc -std=gnu99 -c -arch i386 -arch ppc -isysroot  /Developer/SDKs/MacOSX10.4u.sdk -I/System/Library/Frameworks/JavaVM.framework/Headers -I/System/Library/Frameworks/Cocoa.framework/Headers *.m
gcc -dynamiclib -o ../libtray.jnilib *.o  -framework JavaVM -framework Cocoa
