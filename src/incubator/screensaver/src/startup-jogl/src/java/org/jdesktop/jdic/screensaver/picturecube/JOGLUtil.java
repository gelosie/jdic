package org.jdesktop.jdic.screensaver.picturecube;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLU;
import net.java.games.jogl.util.BufferUtils;

/**
 * Various utility functions for use with JOGL.  Most are borrowed from
 * the NeHe tutorial.
 *
 * @author Kevin Duling (jattier@hotmail.com)
 * @author Mark Roth
 */
public final class JOGLUtil {
    /** Utility class - private constructor */
    private JOGLUtil() {}
    
    /**
     * Read a PNG image from the classpath
     */
    public static BufferedImage readPNGImage(String resource) {
        BufferedImage img;
        try {
            URL url = JOGLUtil.class.getResource(resource);
            if(url == null) {
                throw new RuntimeException("Error reading resoruce " + 
                    resource);
            }
            img = ImageIO.read(url);
            AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
            tx.translate(0, -img.getHeight(null));
            AffineTransformOp op = new AffineTransformOp(tx,
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            img = op.filter(img, null);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        return img;
    }

    /**
     * Create an OpenGL compatible RGB texture from a BufferedImage
     */
    public static void makeRGBTexture(GL gl, GLU glu, BufferedImage img,
        int target, boolean mipMapped) 
    {
        ByteBuffer dest = null;
        switch(img.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_CUSTOM:
            {
                byte[] data = ((DataBufferByte)img.getRaster().
                    getDataBuffer()).getData();
                dest = ByteBuffer.allocateDirect(data.length);
                dest.order(ByteOrder.nativeOrder());
                dest.put(data, 0, data.length);
                break;
            }
            case BufferedImage.TYPE_INT_RGB:
            {
                int[] data = ((DataBufferInt)img.getRaster().
                    getDataBuffer()).getData();
                dest = ByteBuffer.allocateDirect(data.length * 
                    BufferUtils.SIZEOF_INT);
                dest.order(ByteOrder.nativeOrder());
                dest.asIntBuffer().put(data, 0, data.length);
                break;
            }
            default:
                throw new RuntimeException("Unsupported Image type " + 
                    img.getType());
        }
        
        if(mipMapped) {
            glu.gluBuild2DMipmaps(target, GL.GL_RGB8, img.getWidth(),
                img.getHeight(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dest);
        }
        else {
            gl.glTexImage2D(target, 0, GL.GL_RGB, img.getWidth(),
                img.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, dest);
        }
    }
    
    /**
     * Convenience method for generating a single texture
     */
    public static int genTexture(GL gl) {
        final int[] texture = new int[1];
        gl.glGenTextures(1, texture);
        return texture[0];
    }
    
}
