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

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.jar.JarInputStream;

import org.jdic.arc.NativePackedInputStream;
import org.jdic.arc.NativePackedOutputStream;

public class ArcTest {
        String stBase = ".\\";

        static void PrintHelp()
        {
            System.out.println(
                "\nUsage:  cab <a|x> [<switches>...] <CabFile> <FilesParams>\n" +
                "  a: encode file\n" +
                "  x: decode file\n" +
                "<Switches>\n" +
                "  -r:\t pack recursively\n" +
                "  -a<MSZIP|LHZA>:\t pack by MSZIP or LZX\n" +
                "  -l<0-9>:\t 0-fastest, 9-strongest\n" +
                "<CabFile>\n" +
                "   archive file name for coding or encoding" +
                "<FilesParams>\n" +
                "   files/folders for compression or folder for decompession"
            );

        }
        public static boolean pack(String[] args) throws Exception
        {
            int iHowToPack =  NativePackedOutputStream.METHOD_LZX;
            int iLevel =  9;
            boolean bRecursive = false;

            for(int i = 1; i < args.length; i++){
                String s = args[i];
                if(s.length() == 0)
                    return false;
                s.toLowerCase();

                if( s.startsWith("-r") ){
                    bRecursive = true;
                } else if( s.startsWith("-amszip") ){
                    iHowToPack =  NativePackedOutputStream.METHOD_MSZIP;    
                } else if( s.startsWith("-alzh") ){
                    iHowToPack =  NativePackedOutputStream.METHOD_LZX;
                } else if( s.startsWith("-l") && 3==s.length()){
                    iLevel = s.charAt(2) - '0';
                } else {
                    return false;
                }
            }
            return true;
        }
        public static boolean unpack(String[] args) throws Exception
        {
              return false;
        }

        public static void main(String[] args) throws Exception
        {
            /*
            if(args.length < 1 ){
                PrintHelp();
            }
            if(args[0].equalsIgnoreCase("a")){
                pack(args);
            } if(args[0].equalsIgnoreCase("x")){
                unpack(args);
            } else {
                PrintHelp();
            }
            */
            ArcTest pt = new ArcTest();
            pt.test0();
            pt.test1();
            pt.test2();
        }

        public void test0() throws Exception
        {
            
        }

        public void test1() throws Exception
        {
            File out = new File(stBase + "unpack");
            out.mkdir();

            for(int z=0; z<1; ++z){
                NativePackedInputStream nis = new NativePackedInputStream(
                   new FileInputStream(
                          new File(stBase + "80.cab")
                          //new File("C:\\cab\\SAMPLES\\TESTFDI\\data1.cab")
                   ),
                   NativePackedInputStream.FORMAT_CAB,
                   NativePackedInputStream.HINT_ON_DISK                         
                );
                int iCount = 0;
                for(
                    ZipEntry ze = nis.getNextEntry();
                    null != ze;
                    ze = nis.getNextEntry()
                ){
                    System.out.println(
                        "name: " + ze +
                        " original size: " + ze.getSize() +
                        " time: " + new java.util.Date(ze.getTime())
                    );
                    if(3<iCount) {
                        //close-any-time test
                        //nis.close();
                        //return;
                    }
                    File outf = new File(stBase + "unpack\\" + ze);
                    outf.delete();
                    OutputStream fo = new BufferedOutputStream(
                            new FileOutputStream(outf)
                    );
                    // test for one-byte-read
                    //for( int data = nis.read(); -1!=data; data = nis.read() ){
                    //    fo.write(data);
                    //}
                    byte[] buf = new byte[1024];
                    while(true){
                        int read = nis.read(buf);
                        if(-1==read){
                             break;
                        }
                        fo.write(buf, 0, read);
                        ++iCount;
                        if(48==iCount){
                             //if(68==iCount){
                            //close-any-time test
                            //nis.close();
                            //return;
                        }
                    }
                    fo.close();
                }
                nis.close();
            }
        }

        public void test2() throws Exception
        {
            String stJavaHome = System.getProperty("java.home");
            if(stJavaHome.isEmpty())
                throw new IOException("Java Home was not found!");
            for(int k=0; k<1; ++k){
                 for(int i=0; i<10; ++i){
                     /*
                     NativePackedInputStream in = new NativePackedInputStream(
                         new FileInputStream(
                               new File(stBase + "80.CAB")
                               //new File(stBase + "ASMS01.CAB")
                               //new File("C:\\cab\\SAMPLES\\TESTFDI\\data1.cab")
                         ),
                         NativePackedInputStream.FORMAT_CAB,
                         NativePackedInputStream.HINT_ON_DISK);
                     */
                     JarInputStream in = new JarInputStream(
                         new FileInputStream(
                             new File(stJavaHome 
                                + File.separator + "lib" 
                                + File.separator + "rt.jar"
                             )
                         )
                     );

                     File outF = new File(stBase + "out" + i + ".cab");
                     outF.delete();

                     NativePackedOutputStream out = new NativePackedOutputStream(
                        new FileOutputStream( outF ),
                        NativePackedOutputStream.FORMAT_CAB,
                        NativePackedOutputStream.HINT_ON_DISK | NativePackedOutputStream.HINT_SORE_EINFO);

                     out.setLevel(i);
                     try{
                         byte[] buffer = new byte[1024];
                         ZipEntry entry = in.getNextEntry();
                         int totalRead = 0;
                         while (entry != null) {
                             System.out.print(
                                 "name: " + entry +
                                 " original size: " + entry.getSize() +
                                 " time: " + new java.util.Date(entry.getTime())
                             );

                             // It is expensive to create new ZipEntry objects
                             // when compared to cloning the existing entry.
                             // We need to reset the compression size, since we
                             // are changing the compression ratio of the entry.

                             //ZipEntry outEntry = new ZipEntry(entry.toString() + i );
                             ZipEntry outEntry = (ZipEntry)entry.clone();

                             //out.setMethod(outEntry, QUANTUM);
                             out.setMethod(outEntry, NativePackedOutputStream.METHOD_LZX);
                             //out.setMethod(outEntry, MSZIP);

                             outEntry.setCompressedSize(-1);
                             out.putNextEntry(outEntry);

                             int read = 0;

                             while((read = in.read(buffer, 0, buffer.length)) != -1) {
                                 out.write(buffer, 0, read);
                                 totalRead += read;
                             }
                             out.closeEntry();
                             System.out.println(
                                 " size: " + outEntry.getSize() +
                                 " csize: " + outEntry.getCompressedSize() +
                                 " crc: " + Long.toHexString( outEntry.getCrc() )
                             );

                             entry = in.getNextEntry();
                         }
                         out.finish();
                     } finally {
                         out.close();
                         in.close();
                     }
                 }
            }
        }

}
