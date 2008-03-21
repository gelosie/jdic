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
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.CRC32;
import java.util.HashMap;
import java.util.Formatter;
import org.jdic.NativeLoadMgr;

/**
 * The class is responsible for CAB pack operatrions. 
 * @author uta
 */

public class NativePackedOutputStream extends ZipOutputStream {
        static {
            NativeLoadMgr.loadLibrary("jdicArc");
            initIDs();
        }
        private static native void initIDs();

        /*
         * fields
         */
        protected OutputStream os; //destination stream
        protected long osNative; //native handler for packer
        protected ZipEntry entry;
        protected HashMap<String, ZipEntry> names = new HashMap<String, ZipEntry>();
        protected int method = METHOD_LZX; //default compression method
        protected int level = 9; //default compression strength [0..9]
        protected CRC32 crc = new CRC32(); //CRC calculator
        protected long actualUncomressedSize = 0;


        /**
        * Format constant for constructor
        * Primitive stream copier. Non-functionable.
        * Test purpose only
        */
        public static int FORMAT_COPY = 0;

        /**
        * Format constant for constructor
        * CAB stream unpacker. Applies for CAB stream unpacking
        */
        public static int FORMAT_CAB  = 1;

        /**
         * Hint constant for constructor
         * Hold all temporary buffers in memory
         */
        public static final long HINT_IN_MEMORY = 0;

        /**
         * Hint constant for constructor
         * Hold all temporary buffers in temp folder as files
         */
        public static final long HINT_ON_DISK = 1;


        /**
         * Hint constant for constructor
         * Hold CRC32 and Compressed Size in entry name like
         * name{FAFAFAFA:FAFAFAFA}.ext  
         */
        public static final long HINT_SORE_EINFO = 2;

        /**
         * Mask for compression type: No compression. The same as STORED
         */
        public static final int METHOD_NONE    = 0x0000;//STORED

        /**
         * Mask for compression type: MSZIP.
         */
        public static final int METHOD_MSZIP   = 0x0001;

        /**
         * Mask for compression type: Quantum (not yet supported by MS)
         */
        public static final int METHOD_QUANTUM = 0x0002;

        /**
         * Mask for compression type: LZX
         */
        public static final int METHOD_LZX     = 0x0003;

        /**
         * Set <code>method</code> field of <code>ze</code> to value METHOD_MSZIP, 
         * METHOD_QUANTUM, METHOD_LZX.
         * @param ze the entry for modify
         * @param method the desired compression method
         */
         public void setMethod(ZipEntry ze, int method) {
            switch(method){
            case METHOD_MSZIP:
            case METHOD_QUANTUM:
            case METHOD_LZX:
                ZipEntryAccessor.setMethod(ze, method);
                return;
            }
            ze.setMethod(method);
        }


        /*
        * Internal state validator.
        */
        void checkValid() throws IOException {
            if(null == os || 0 == osNative) {
                throw new IOException("Empty or corrupted output stream");
            }
            //checkNativeValid(osNative);
        }
        //private static native void checkNativeValid(long osNative) throws IOException;
        
        /**
         * Constructs a wrapper for native compressor.
         * @param _os the destination stream
         * @param iFormat a FORMAT_XXXX constant 
         * @param hint a combination of HINT_XXXX constants
         * @throws java.io.IOException
         */
        public NativePackedOutputStream(
                OutputStream _os, 
                int iFormat, 
                long hint) throws IOException 
        {
            super(_os);
            os = _os;
            if(null == os) {
                throw new IOException("No base output stream");
            }
            osNative = createNativeStream(os, iFormat, hint);
        }
        
        /**
         * Constructs a wrapper for native compressor. All buffers will be opened 
         * on a disk, CRC will be calculated. 
         * @param _os the destination stream
         * @param iFormat a FORMAT_XXXX constant 
         * @throws java.io.IOException
         */
        public NativePackedOutputStream(
                OutputStream _os, 
                int iFormat) throws IOException 
        {
            this(_os, iFormat, HINT_ON_DISK | HINT_SORE_EINFO);
        }
        
        /**
         * Creates the native encoder.
         * @param os the destination stream
         * @param iFormat a FORMAT_XXXX constant 
         * @param hint a combination of HINT_XXXX constants
         * @return the native encoder handle
         * @throws java.io.IOException
         */
        private native long createNativeStream(
                OutputStream os, 
                int iFormat, 
                long hint) throws IOException;


        /**
         * Sets the CAB file comment. Not supported.
         * @param comment the comment string
         * @exception IllegalArgumentException if the length of the specified
         *		  ZIP file comment os greater than 0xFFFF bytes
         */
        @Override
        public void setComment(String comment) {
            if ( null != comment) {
                throw new IllegalArgumentException("CAB does not support.");
            }
        }

        /**
         * Sets the default compression method for subsequent entries. This
         * default will be used whenever the compression method is not specified
         * for an individual CAB file entry, and is initially set to DEFLATED.
         * @param method the default compression method
         * @exception IllegalArgumentException if the specified compression method
         *		  is invalid
         */
        @Override
        public void setMethod(int method) {
            this.method = method;
        }

        /**
         * Sets the compression level for subsequent entries which are DEFLATED.
         * The default setting is DEFAULT_COMPRESSION.
         * @param level the compression level (0-9)
         * @exception IllegalArgumentException if the compression level is invalid
         */        
        @Override
        public void setLevel(int level) {
            this.level = level;
        }

        /**
         * Begins writing a new CAB file entry and positions the stream to the
         * start of the entry data. Closes the current entry if still active.
         * The default compression method will be used if no compression method
         * was specified for the entry, and the current time will be used if
         * the entry has no set modification time.
         * @param e the ZIP entry to be written
         * @exception java.util.zip.ZipException if a ZIP format error has occurred
         * @exception IOException if an I/O error has occurred
         */        
        @Override
        public void putNextEntry(ZipEntry e) throws IOException {
            checkValid();
            if( null!= entry ){
                closeEntry();	// close previous entry
            }
            
            if( null==e || e.toString().isEmpty() ){
                return;
            }

            String en = e.getName();
            if(File.separatorChar!='/'){
                en.replace(File.separatorChar, '/');
            }
            if( names.containsKey(en) ) {
                throw new ZipException("duplicate entry: " + en);
            }

            entry = e;
            ZipEntryAccessor.setName(entry, en);
            if( -1 == entry.getTime()  ){
                entry.setTime(System.currentTimeMillis());
            }

            //re-code DEFLATED const for compatibility reason
            int emethod = entry.getMethod();
            if( -1==emethod || DEFLATED==emethod ){
                setMethod(entry, method); // use default method
            }
            switch( entry.getMethod() ) {
            case METHOD_NONE:
            case METHOD_MSZIP:
            case METHOD_QUANTUM:
            case METHOD_LZX:
            case DEFLATED:
                break;
            default:
                throw new ZipException("unsupported compression method");
            }
            names.put(en, entry);
            putNextEntryNative(osNative, entry, level);
        }
        private static native void putNextEntryNative(long osNative, ZipEntry e, int level);

        /**
         * Closes the current CAB entry and positions the stream for writing
         * the next entry.
         * @exception ZipException if a CAB format error has occurred
         * @exception IOException if an I/O error has occurred
         */        
        @Override
        public void closeEntry() throws IOException {
            if (null != entry && 0 != osNative) {
                long newCrc = crc.getValue();
                crc.reset();

                //Not cheking now due to different packing algorithms.
                //Due to "solid" packing algorithm the packed size can be equal to 0 or
                //greater than unpacked size.
                
                long newCSize = closeEntryNative( osNative, newCrc );
                entry.setCompressedSize(newCSize);

                long newSize = actualUncomressedSize;
                actualUncomressedSize = 0;
                
                long oldSize = entry.getSize();
                entry.setSize(newSize);

                long oldCrc = entry.getCrc();
                entry.setCrc(newCrc);

                entry = null;

                if( -1!=oldSize && newSize != oldSize ){
                    throw new ZipException(
                        "invalid entry size (expected " + oldSize +
                        ", but got " + newSize + " bytes)");
                }
                
                //in 0xffffffffL "L" suffix is VERY critical!
                if(  (0xffffffffL != (oldCrc & 0xffffffffL)) && (newCrc!=oldCrc) ){
                    throw new ZipException(
                         "invalid entry crc-32 (expected 0x" +
                         Long.toHexString(oldCrc) + ", but got 0x" +
                         Long.toHexString(newCrc) + ")");
                }

            }
        }
        
        /**
         * @param osNative native handler to stream processor
         * @param crc Java-calculated CRC
         * @return the count of bytes were written to base output stream (compressed size)
         */
        private static native long closeEntryNative(long osNative, long crc);

        /**
         * Callback from native code that reserves space for crc and compressed 
         * size that are not natively supported in OS DLL.
         * @return length-model for final {@link #getEntrySuffix} call 
         */
        protected String getEntrySuffixStub()
        {
            //call ones to reserve additional bytes in file name
            //for storing extended attribytes
            return "{01234567-01234567}";
        }
        
        /**
         * Callback from native code that stores real crc and compressed size 
         * values that are not natively supported in OS DLL.
         * @param entryName CAB entry name for modification
         * @return modified CAB entry name
         * @throws java.io.IOException
         */
        protected String getEntrySuffix(String entryName) throws IOException
        {
            ZipEntry ze = names.get(entryName);
            if( null == ze){
                return null;
            }

            Formatter f = new Formatter();
            f.format("{%08x-%08x}", ze.getCrc(), ze.getCompressedSize());
            String ret = f.toString();
            if( ret.length() > getEntrySuffixStub().length() ){
                throw new ZipException(
                    "no room for extended attributes (reserved:" +
                    getEntrySuffixStub() + ", but stored " +
                    ret + ")");
            }
            return f.toString();
        }

        /**
         * Writes an array of bytes to the current CAB entry data. This method
         * will block until all the bytes are written.
         * @param b the data to be written
         * @param off the start offset in the data
         * @param len the number of bytes that are written
         * @exception ZipException if a CAB file error has occurred
         * @exception IOException if an I/O error has occurred
         */        
        @Override
        public synchronized void write(byte[] b, int off, int len)
            throws IOException
        {
            checkValid();
            if (off < 0 || len < 0 || off > b.length - len) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            if (null == entry) {
                throw new ZipException("no current ZIP entry");
            }
            writeNativeBytes(osNative, b, off, len);
            actualUncomressedSize += len;
            //System.out.println(actualUncomressedSize);
            crc.update(b, off, len);
        }
        private static native void writeNativeBytes(long osNative, byte[] b, int off, int len) throws IOException;

        /**
         * Finishes writing the content of the CAB output stream without closing
         * the underlying stream. Use this method when applying multiple filters
         * in succession to the same output stream.
         * @exception ZipException if a CAB file error has occurred
         * @exception IOException if an I/O exception has occurred
         */        
        @Override
        public void finish() throws IOException {
            checkValid();
            if (null != entry) {
                closeEntry();
            }
            finishNative(osNative);
        }
        private static native void finishNative(long osNative) throws IOException;

        /**
         * Closes the CAB output stream as well as the stream being filtered.
         * @exception ZipException if a CAB file error has occurred
         * @exception IOException if an I/O error has occurred
         */        
        @Override
        public void close() throws IOException {
            //this call are in super:
            // finish();
            // if(null != os)
            //      os.close();
            try{
                super.close();
            } finally {
                if(0 != osNative) {
                    closeNative(osNative);
                    osNative = 0;
                }
            }                
        }
        private static native void closeNative(long osNative);
}