/*
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

package org.jdesktop.jdic.icons.impl;

import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

public class WinIconWrapper {

    private static final boolean isLoaded;
    static {
        boolean success= false;
        try {
            System.loadLibrary("jdic_icon");
            success= true;
        }
        catch(Throwable ex) {
        }
        isLoaded= success;
    }

    /**
    * Was the native portion successfully loaded?
    */
    public static boolean isLoaded() {
        return isLoaded;
    }
 
    /**
     * Get the icon bytes from the specification
     *
     * @param iconSpecification The return value from getIconFileNameByFileExt or getIconFileNameByMimeType.
     * @return the win32 icon bytes
     */
    private static native byte[] GetIcon(char[] iconSpecification);

    /**
     * Returns this java string as a null-terminated byte array
     */
    static char[] stringToCharArray(String str) {
        if (str == null) {
            throw new NullPointerException();
        }
        char[] chars= new char[str.length()+1];
        str.getChars(0, str.length(), chars, 0);
        chars[str.length()]= 0;
        return chars;
    }

    /**
     * Returns the icons from the icon specification.  This specification is the result of
     * getIconFileNameByFileExt or getIconFileNameByMimeType.
     *
     * @param specification Given file extension.
     * @return An array of ImageProducer - may be null
     */
    public static ImageProducer[] getIconsFromSpecification(String specification) {
        final byte[] iconBytes= GetIcon(stringToCharArray(specification));
        if(iconBytes==null)
            return null;
        try {
            return getImages(iconBytes);
        }
        catch(Exception ignore) {
            return null;
        }
    }

    static final private int DWORD(byte[] d, int offset) {
        return (d[offset]&0xff)|((d[offset+1]&0xff)<<8)|((d[offset+2]&0xff)<<16)|((d[offset+3]&0xff)<<24);
    }

    static final private int TRIPLE(byte[] d, int offset) {
        return (d[offset+2]&0xff)|((d[offset+1]&0xff)<<8)|((d[offset]&0xff)<<16)|0xff000000;
    }

    static final private int WORD(byte[] d, int offset) {
        return (d[offset]&0xff)|((d[offset+1]&0xff)<<8);
    }

    static ImageProducer Bitmap(byte[] imageData, int offset, int type) {
        if(WORD(imageData, offset+12)!=1)   // planes==1
            throw new IllegalArgumentException("planes!=1");

        final int width= DWORD(imageData, offset+4);
        final int h= DWORD(imageData, offset+8);
        final int height= type!=0 ?h/2 :h;

        final int bitCount= WORD(imageData, offset+14);
        final int nColorsUsed= DWORD(imageData, offset+32);
        final int colorCount= nColorsUsed!=0 ?nColorsUsed :1<<bitCount;

        final int colorOffset= offset+DWORD(imageData, offset);
        final int compression= DWORD(imageData, offset+16);
        if(compression!=0 && compression!=3)
            throw new IllegalArgumentException("compression!=0 && compression!=3");

        // flip the RGB palette
        if(bitCount<16 || compression==3) {
            int idx= colorOffset;
            for(int i= 0; i<colorCount; ++i) {
                final byte b= imageData[idx];
                imageData[idx]= imageData[idx+2];
                imageData[idx+2]= b;
                imageData[idx+3]= (byte)255;
                idx+= 4;
            }
        }

        // use default color model 0xAARRGGBB
        final int[] intData= new int[height*width];
        int dst= 0;

        final int xorBytes= colorOffset + 4*(bitCount<16 ?colorCount :nColorsUsed);
        final int xorBytesPerRow= ((width * bitCount / 8) + 3)&~3;  // round up to next 4-byte boundary
        final int andBytes= xorBytes+xorBytesPerRow*height;

        int xorOffset= xorBytes;
        int andOffset= andBytes;
        final int xorMask= bitCount<32 ?(1<<bitCount)-1 :0xffffffff;
        final int pixelsPerByte= 8/bitCount;
        final int bytesPerPixel= bitCount/8;
        final int extraMaskBytesPerRow= (((width+31) & ~31) - width)/8;

        for(int y= 0; y<height; ++y) {
            int xPixel= 0, aPixel= 0;
            byte xData= 0, aData= 0;

            for(int x= 0; x<width; ++x) {
                int argb;
                if(bitCount<16) {
                    if(--xPixel<0) {
                        xData= imageData[xorOffset++];
                        xPixel= pixelsPerByte-1;
                    }
                    argb= TRIPLE(imageData, colorOffset+((xData>>(xPixel*bitCount))&xorMask)*4);
                }
                else {
                    argb= (imageData[xorOffset]&0xff)|((imageData[xorOffset+1]&0xff)<<8)|((imageData[xorOffset+2]&0xff)<<16);
                    if(compression==3)
                        argb= TRIPLE(imageData, colorOffset+argb*4);
                    else
                        argb|= 0xff000000;

                    xorOffset+= bytesPerPixel;
                }

                if(type!=0) {
                    if(--aPixel<0) {
                        aData= imageData[andOffset++];
                        aPixel= 7;
                    }
                    if((aData&(1<<aPixel))!=0) {
                        argb&= 0xffffff;    // transparent
                    }
                }

                intData[dst++]= argb;
            }
            andOffset+= extraMaskBytesPerRow;
        }

        return new MemoryImageSource(width, height, intData, dst-width, -width);
    }

    static private class Order implements Comparable {
        int bitCount;
        int width;
        ImageProducer producer;

        Order(int bitCount, int width, ImageProducer producer) {
            this.bitCount= bitCount;
            this.width= width;
            this.producer= producer;
        }

        // order by size, then colors
        public int compareTo(Object o) {
            final int diff= ((Order)o).width-width;
            if(diff!=0)
                return diff;

            return ((Order)o).bitCount-bitCount;
        }
    }

    static ImageProducer[] BitmapDir(byte[] imageData) {
        final int count= WORD(imageData, 4);
        final Order[] order= new Order[count];

        int offset= 6;
        for(int i= 0; i<count; ++i) {
            order[i]= new Order(
                WORD(imageData, offset+6),
                imageData[offset]&0xff,
                Bitmap(imageData, DWORD(imageData, offset+12), imageData[2]));
            offset+= 16;
        }

        java.util.Arrays.sort(order);

        final ImageProducer[] producers= new ImageProducer[count];
        for(int i= 0; i<count; ++i) {
            producers[i]= order[i].producer;
        }
        return producers;
    }

    static ImageProducer[] getImages(byte[] imageData) {
        if(imageData.length<6)
            throw new IllegalArgumentException("not enough imageData");

        // check for bmp/icon file signature
        if(imageData[0]==(byte)'B' && (imageData[1]==(byte)'A' || imageData[1]==(byte)'M')
            || imageData[0]==(byte)'C' && (imageData[1]==(byte)'I' || imageData[1]==(byte)'P')
            || imageData[0]==(byte)'I' && imageData[1]==(byte)'C'
            || imageData[0]==(byte)'P' && imageData[1]==(byte)'T') {

            int fileSize= DWORD(imageData, 2);
            if(fileSize!=imageData.length)
                throw new IllegalArgumentException("imageData length inconsistent");

            int type= 0;
            if( imageData[0]==(byte)'I' || imageData[1]==(byte)'I')
                type= 1;
            else if( imageData[0]==(byte)'P' || imageData[1]==(byte)'P')
                type= 2;

            return new ImageProducer[] {Bitmap(imageData, 14, type)};
        }

        // check for icon/pointer directory signature
        if(imageData[0]==(byte)0 && imageData[1]==(byte)0 && imageData[3]==(byte)0 
           && (imageData[2]==(byte)1 || imageData[2]==(byte)2)) {

            return BitmapDir(imageData);
        }

        // check for bmp signature
        if(imageData[0]==(byte)40 && imageData[1]==(byte)0 && imageData[2]==(byte)0 && imageData[3]==(byte)0) {
            return new ImageProducer[] {Bitmap(imageData, 0, 0)};
        }

        throw new IllegalArgumentException("invalid signature bytes");
    }
}  

