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
import java.util.zip.ZipOutputStream;

/**
 * The class is responsible for archive operatrions from commantd line.
 * @author uta
 */

public class Executor {
    
    /**
     * Class can not be instantiated.
     */
    private Executor() {}
    
    /**
     * Prints help information to console.
     */
    static void PrintHelp()
    {
        System.out.println(
            "\nUsage:  cab <a|x> [<switches>...] <CabFile> <FilesParams>\n" +
            "  a: encode file\n" +
            "  x: decode file\n" +
            "<Switches>\n" +
            " pack:\n" +
            "  -r: pack recursively\n" +
            "  -a<NONE|MSZIP|LHZA>:  just store or pack by MSZIP or LZX\n" +
            "  -l<0-9>: 0-fastest, 9-strongest\n" +
            "  -Ext: store CRC and Compressed Size in file name\n" +                   
            " pack & unpack:\n" +                    
            "  -h<M|D>: M - use memory buffers, D - use temp files\n" +
            "<CabFile>\n" +
            "   archive file name for coding or encoding\n" +
            "<FilesParams>\n" +
            "   files/folders for compression or folder for decompession\n"
        );
    }
    
    /**
     * Standard streams copy routine.
     * @param in  the source stream
     * @param out the destination stream
     * @throws java.io.IOException
     */
    public static void copyStream(
        InputStream in,
        OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
    }
    
    /**
     * Reports staff information about <code>ZipEntry</code> to console.
     * @param ze the report subject
     */
    public static void dumpEntry(ZipEntry ze)
    {
        String resume = null;
        long size = ze.getSize();
        long csize = ze.getCompressedSize();
        if(0==size){
            resume = " (stored 0%)";
        } else {
            resume = " (deflated " + ( (size - csize)*100/size ) + "%";
            if(size < csize) {
                resume += " - ok for stream compression";                 
            }
            resume += ")";
        }
        System.out.println(
            "added: " + ze +
            " original: " + ze.getSize() +
            " compressed: " + ze.getCompressedSize() +                    
            //" time: " + new java.util.Date(ze.getTime() +
            resume );
    }
    
    /**
     * Copies archived content from source stream 
     * to destination. 
     * 
     * @param prefix contains the parent folder name
     * @param in     the archived source stream
     * @param out    the archived destination stream 
     * @throws java.io.IOException
     */
    public static void repackStream(
        String prefix,
        ZipInputStream in,
        ZipOutputStream out) throws IOException
    {
            ZipEntry entry = in.getNextEntry();
            while(entry != null) {
                // It is expensive to create new ZipEntry objects
                // when compared to cloning the existing entry.
                // We need to reset the compression size, since we
                // are changing the compression ratio of the entry.

                ZipEntry outEntry = new ZipEntry(entry.toString());
                outEntry.setTime(entry.getTime());
                ZipEntryAccessor.setMethod(outEntry, -1);
                ZipEntryAccessor.setName(outEntry, prefix + outEntry.toString());
                out.putNextEntry(outEntry);
                copyStream(in, out);
                //needs to update outEntry fields
                out.closeEntry();
                dumpEntry(outEntry);
                entry = in.getNextEntry();
            }
    }

    /**
     * Copies a content from source file(s) to archived destination stream. 
     * @param inf the source file to be compressed
     * @param out the archived destination stream 
     * @param bRecursive the recursive execution property for folder operation
     * @throws java.io.IOException
     */
    public static void repackFile(
        File inf,
        ZipOutputStream out,
        boolean bRecursive) throws IOException
    {
        String stEN = null;
        if( inf.isAbsolute() ){
           stEN = inf.getName();
        } else {
           stEN = inf.getPath();
        }
        if(inf.isDirectory() && !stEN.endsWith( File.separator )){
            stEN += File.separator;                         
        }
        ZipEntry outEntry = new ZipEntry(stEN);
        outEntry.setTime(inf.lastModified());
        ZipEntryAccessor.setMethod(outEntry, -1);
        out.putNextEntry(outEntry);
        if( inf.isFile() ){
            FileInputStream in = null;
            try{
                in = new FileInputStream(inf); 
                copyStream(in, out);
                //needs to update outEntry fields
                out.closeEntry();
                dumpEntry(outEntry);
            } finally {
                if(null!=in){
                    in.close();
                }
            }
        } else {
            //needs to update outEntry fields
            out.closeEntry();
            dumpEntry(outEntry);
            if(bRecursive){
                File cnt[] = inf.listFiles();
                for( File i: cnt ){
                    repackFile(
                        i,
                        out,
                        bRecursive);
                }
            }
        }
    }

    /**
     * Command line excecutor for packing operations.
     * @param args the command line arguments. 
     * @return <code>false</code> on error, <code>true</code> in other cases
     * @throws java.io.IOException
     */
    public static boolean pack(String[] args) throws IOException
    {
        int iMethod =  NativePackedOutputStream.METHOD_LZX;
        int iLevel =  9;
        int iHint  =  0;
        boolean bRecursive = false;

        int i;
        for(i = 1; i < args.length; ++i){
            String s = args[i];
            if(s.length() == 0)
                return false;
            if( !s.startsWith("-") ){
                //end of switch section
                break;
            }
            s.toLowerCase();

            if( s.equalsIgnoreCase("-r") ){
                bRecursive = true;
            } else if( s.equalsIgnoreCase("-anone") ){
                iMethod =  NativePackedOutputStream.METHOD_NONE;
            } else if( s.equalsIgnoreCase("-amszip") ){
                iMethod =  NativePackedOutputStream.METHOD_MSZIP;
            } else if( s.equalsIgnoreCase("-alzh") ){
                iMethod =  NativePackedOutputStream.METHOD_LZX;
            } else if( s.startsWith("-l") && 3==s.length()){
                iLevel = s.charAt(2) - '0';
            } else if( s.equalsIgnoreCase("-hM")){
                iHint |= NativePackedOutputStream.HINT_IN_MEMORY;
            } else if( s.equalsIgnoreCase("-hD")){
                iHint |= NativePackedOutputStream.HINT_ON_DISK;
            } else if( s.equalsIgnoreCase("-Ext")){
                iHint |= NativePackedOutputStream.HINT_SORE_EINFO;
            } else {
                System.err.println("Errror: Unknown switch " + s);
                return false;
            }
        }
        if(i==args.length){
            System.err.println("Errror: Unknown CAB name ");
            return false;
        }
        if( (i+1)==args.length ){
            System.err.println("Errror: Unknown compression list");
            return false;
        }

        File of = new File(args[i]);
        of.delete();
        NativePackedOutputStream out = new NativePackedOutputStream(
                new FileOutputStream(of), NativePackedOutputStream.FORMAT_CAB, iHint );
        out.setLevel(iLevel);
        out.setMethod(iMethod);
        InputStream in = null;
        boolean bDelOut = true;
        try {
            for(++i; i < args.length; ++i){
                File inf = new File(args[i]);
                if(
                        args[i].endsWith(".zip") ||
                        args[i].endsWith(".jar") ||
                        args[i].endsWith(".cab")
                ){
                    in = new FileInputStream(inf);
                    in = args[i].endsWith(".cab")
                        ? new NativePackedInputStream(
                            in,
                            NativePackedInputStream.FORMAT_CAB )
                        : new ZipInputStream( in );
                    System.out.println(args[i] + " archive repack");
                    File pf = inf.getParentFile();
                    repackStream(
                        (null==pf || inf.isAbsolute())
                                ? ""
                                : pf.getPath() + File.separatorChar,
                        (ZipInputStream)in,
                        out);
                    in.close();
                    in = null;
                } else {
                    repackFile(inf, out, bRecursive);
                }
            }
            bDelOut = false;
        } catch( Exception e ){
            e.printStackTrace();
        } finally {
            if(null!=in){
                in.close();
            }
            out.close();
            if(bDelOut){
                of.delete();
            }
        }
        return true;
    }

    /**
     * Command line excecutor for unpacking operations.
     * @param args the command line arguments. 
     * @return <code>false</code> on error, <code>true</code> in other cases
     * @throws java.io.IOException
     */
    public static boolean unpack(String[] args) throws Exception
    {
        int iHint  =  0;
        boolean bRecursive = false;

        int i;
        for(i = 1; i < args.length; ++i){
            String s = args[i];
            if(s.length() == 0)
                return false;
            if( !s.startsWith("-") ){
                //end of switch section
                break;
            }
            s.toLowerCase();

            if( s.equalsIgnoreCase("-hM")){
                iHint |= NativePackedOutputStream.HINT_IN_MEMORY;
            } else if( s.equalsIgnoreCase("-hD")){
                iHint |= NativePackedOutputStream.HINT_ON_DISK;
            } else {
                System.err.println("Errror: Unknown switch " + s);
                return false;
            }
        }
        if(i==args.length){
            System.err.println("Errror: Unknown CAB name ");
            return false;
        }
        File inf = new File(args[i]);        
        ++i;
        File outDir = new File(
            i==args.length
                ? "."
                : args[i]);
        outDir.mkdirs();

        NativePackedInputStream in = new NativePackedInputStream(
                new FileInputStream(inf), NativePackedOutputStream.FORMAT_CAB, iHint );
        OutputStream out = null;
        try{
            for(
                ZipEntry ze = in.getNextEntry();
                null != ze;
                ze = in.getNextEntry())
            {
                System.out.println(
                    "extracted: " + ze +
                    " original size: " + ze.getSize() +
                    " time: " + new java.util.Date(ze.getTime())
                );

                File outf =  new File(outDir.getPath() + File.separator + ze);
                if( !ze.isDirectory() ){
                    outf.delete();                    
                    outf.getParentFile().mkdirs();
                    out = new FileOutputStream(outf);
                    copyStream(in, out);
                    out.close();
                    out = null;
                } else {
                    outf.mkdirs();                    
                }
            }
        } catch( Exception e ){
            e.printStackTrace();
        } finally {
            in.close();
            if( null!=out ){
                out.close();
            }
        }
        return true;
    }

    /**
     * Module entry point. Command line excecutor for packing/unpacking operations.
     * @param args the command line arguments. In case of empty argument list 
     * the help is printing to console.
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception
    {
        do{
            if(args.length > 1 ){
                if(args[0].equalsIgnoreCase("a")){
                    if( pack(args) ){
                        break;
                    }
                } if(args[0].equalsIgnoreCase("x")){
                    if( unpack(args) ){
                        break;
                    }    
                }
            }
            PrintHelp();
        }while(false);
    }
}