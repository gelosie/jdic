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

package org.jdic.arc;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.CRC32;
import java.util.zip.ZipException;
import org.jdic.NativeLoadMgr;

/**
 * The class is responsible for CAB unpack operatrions. 
 * @author uta
 */

public class NativePackedInputStream extends ZipInputStream {
        static {
            NativeLoadMgr.loadLibrary("jdicArc");
            initIDs();
        }
        /**
         * Initialize methods and fields IDs for JNI 
         */
        private static native void initIDs();

        /*
         * fields
         */
        protected InputStream is; //source stream
        protected long        isNative; //native handler for unpacker
        protected ZipEntry    entry; //current ZipEntry
        protected CRC32 crc = new CRC32(); //CRC calculator/verifier

        /**
        * Format constant for constructor.
        * Primitive stream copier. Non-functionable.
        * Test purpose only
        */
        public static int FORMAT_COPY = 0;

        /**
        * Format constant for constructor.
        * CAB stream unpacker. Applies for CAB stream unpacking.
        */
        public static int FORMAT_CAB  = 1;

        /**
         * Hint constant for constructor.
         * Hold all temporary buffers in temp folder as files.
         */
        public static final long HINT_ON_DISK = 1;

        /**
         * Hint constant for constructor.
         * Hold all temporary buffers in memory.
         */
        public static final long HINT_IN_MEMORY = 0;


        /**
        * consts for getProperty call.
        */
        public static int arc_time = 0;
        public static int arc_attr = arc_time + 1;
        public static int arc_original_size = arc_attr + 1;

        public static int cab_next_file = arc_original_size + 1;
        public static int cab_next_disk = cab_next_file + 1;
        public static int cab_path = cab_next_disk + 1;
        public static int cab_set_ID = cab_path + 1;
        public static int cab_number = cab_set_ID + 1;
        public static int cab_torn_file = cab_number  + 1;
        public static int cab_prev_file = cab_torn_file + 1;
        public static int cab_prev_disk = cab_prev_file + 1;


        /**
         * Internal state validator
         */
        void checkValid() throws IOException {
            if(null == is || 0==isNative) {
                throw new IOException("Empty or corrupted input stream");
            }
            //checkNativeValid(isNative);
        }
        //private static native void checkNativeValid(long isNative) throws IOException;

        /**
         * Constructs a wrapper for native decompressor
         * @param _is the source stream
         * @param iFormat the FORMAT_XXXX constant for stream format selection
         * @param hint the HINT_XXXX constant
         * @throws java.io.IOException
         */
        public NativePackedInputStream(
                InputStream _is, 
                int iFormat, 
                long hint) throws IOException 
        {
            super(_is);
            is = _is;
            if(null == is) {
                throw new IOException("No base input stream");
            }
            isNative = createNativeStream(is, iFormat, hint);
        }
        
        /**
         * Constructs a wrapper for native decompressor with staff-buffers on the disk.
         * @param _is the source stream
         * @param iFormat the FORMAT_XXXX constant for stream format selection
         * @throws java.io.IOException
         */
        public NativePackedInputStream(
                InputStream _is, 
                int iFormat) throws IOException 
        {
            this(_is, iFormat, HINT_ON_DISK);
        }
        
        /** 
         * Creates native packer instance.
         * @param is the source stream
         * @param iFormat the FORMAT_XXXX constant for stream format selection
         * @param hint the HINT_XXXX constant
         * @return
         * @throws java.io.IOException
         */
        private static native long createNativeStream(
                InputStream is, 
                int iFormat, 
                long hint) throws IOException;

        /**
         * See java.util.zip.ZipInputStream.available
         * @return bytes available
         */
        @Override
        public int available() throws IOException 
        {
            checkValid();
            return null == entry ? 0 : 1;
        }

        /**
        * Reads uncompressed data into an array of bytes. If <code>len</code> is not
        * zero, the method will block until some input can be decompressed; otherwise,
        * no bytes are read and <code>0</code> is returned.
        * @param b the buffer into which the data is read
        * @param off the start offset in the destination array <code>b</code>
        * @param len the maximum number of bytes read
        * @return the actual number of bytes read, or -1 if the end of the
        *         compressed input is reached or a preset dictionary is needed
        * @exception  NullPointerException If <code>b</code> is <code>null</code>.
        * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
        * <code>len</code> is negative, or <code>len</code> is greater than
        * <code>b.length - off</code>
        * @exception IOException if an I/O error has occurred
        */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            checkValid();
            if (off < 0 || len < 0 || off > b.length - len) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if(null == entry) {
                return -1;
            }
            int ret = readNativeBytes(isNative, b, off, len);
            if(-1 == ret){
                long newCrc = crc.getValue();
                long oldCrc = entry.getCrc();
                //suffux L in 0xffffffffL is VERY critical! 
                if(0xffffffffL==(oldCrc & 0xffffffffL)){
                    entry.setCrc(newCrc);
                    oldCrc = newCrc; 
                }
                entry = null;
                if( oldCrc != newCrc ) {
                    throw new ZipException(
                        "invalid entry CRC (expected 0x" + Long.toHexString(oldCrc) +
                        ", but got 0x" + Long.toHexString(newCrc) + ")");
                }
            } else {
                crc.update(b, off, len);
            }
            return ret;
        }
        private static native int readNativeBytes(
                long isNative, 
                byte[] b, 
                int off, 
                int len) throws IOException;

        /**
         * See java.util.zip.ZipInputStream.close.
         */
        @Override
        public void close() throws IOException {
            if(0 != isNative) {
                closeNative(isNative);
                isNative = 0;
            }
            //   this call are in super
            //if(null != is)
            //      is.close();
            super.close();
        }
        private static native void closeNative(long isNative);


        /**
         * Reads the next CAB file entry and positions the stream at the
         * beginning of the entry data.
         * @return the next CAB file entry, or null if there are no more entries
         * @exception IOException if an I/O error has occurred
         */
        @Override
        public ZipEntry getNextEntry() throws IOException {
            checkValid();
            if(null != entry) {
                closeEntry();
            }
            crc.reset();
            return preprocessNewEntry(isNative);
        }
        
        private static native ZipEntry readNextEntryNative(long isNative);
        
        /**
         * Converts string like "1234ABCD" to number 0x1234ABCD.
         * @param st the string for conversion 
         * @param iPos the offset for the first converted hex literal in <code>st</code>
         * @param len the number of chars from <code>st</code> for conversion
         * @return converted <code>long</code> value
         */
        public static long HexSubstr(String st, int iPos, int len)
        {
            final String szHex = "0123456789abcdefABCDEF";
            final byte[] Nibbles = new byte[]{
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
                0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
            };
            int iLastPos = iPos + len;
            if( iLastPos > st.length() ){
                return -1;
            }
            long ret = 0;
            for(int i = iPos; i < iLastPos; ++i){
                int k = szHex.indexOf(st.charAt(i));
                if( 0 > k  ){
                    //non-hex value
                    return -1;
                } else {
                    ret <<= 4;
                    ret |= Nibbles[k];
                }
            }
            return ret;
        }
        private ZipEntry preprocessNewEntry(long isNative)
        {
            entry =  readNextEntryNative(isNative);
            if(null!=entry && !entry.isDirectory()){
                String name = entry.toString();
                int iDotPos = name.lastIndexOf('.');
                if(-1==iDotPos){
                    iDotPos = name.length();
                }
                int iCloseInfoPos = name.lastIndexOf('}', iDotPos);
                if( 0<=iCloseInfoPos ){
                    int iOpenInfoPos = name.lastIndexOf('{', iCloseInfoPos);
                    if( 0<=iOpenInfoPos ){
                        long crc = HexSubstr(name, iOpenInfoPos+1, 8);
                        if(0<=crc){
                            long csize = HexSubstr(name, iOpenInfoPos+10, 8);
                            if(0<=csize){
                                //cut the extended attributes information
                                name = name.substring(0, iOpenInfoPos) + name.substring(iDotPos);
                                ZipEntryAccessor.setName(entry, name);
                            }
                        }
                    }
                }
            }
            return entry;
        }

        /**
         * Returns the value for the propery specified by arc_XXXX or cab_XXXX const.
         * @return  custom property for current state
         * consts arc_XXXX and cab_XXXX are applicable
         * @exception IOException if an I/O error has occurred
         */
        String getProperty(int iIndex) throws IOException {
            checkValid();
            return getPropertyNative(isNative, iIndex);
        }
         private static native String  getPropertyNative(long isNative, int iIndex) throws IOException;

        /**
         * Closes the current CAB entry and positions the stream for reading the
         * next entry.
         * @exception IOException if an I/O error has occurred
         */
        @Override
        public void closeEntry() throws IOException {
            checkValid();
            if(null != entry) {
                byte[] tmpbuf = new byte[1024];
                while( read(tmpbuf, 0, tmpbuf.length) != -1 ) ;
            }
        }
 }
