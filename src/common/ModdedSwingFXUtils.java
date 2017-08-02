/*
 * Original license:
 *
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

 package common;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import sun.awt.image.IntegerComponentRaster;

/**
 * This class provides utility methods for converting data types between
 * Swing/AWT and JavaFX formats. It has been modified from the class provided by
 * JavaFx, eliminating the check in fromFXImage to make sure the BufferedImage
 * has an alpha channel.
 * @since JavaFX 2.2
 */
public class ModdedSwingFXUtils {
    private ModdedSwingFXUtils() {} // no instances

    /**
     * Snapshots the specified {@link BufferedImage} and stores a copy of
     * its pixels into a JavaFX {@link Image} object, creating a new
     * object if needed.
     * The returned {@code Image} will be a static snapshot of the state
     * of the pixels in the {@code BufferedImage} at the time the method
     * completes.  Further changes to the {@code BufferedImage} will not
     * be reflected in the {@code Image}.
     * <p>
     * The optional JavaFX {@link WritableImage} parameter may be reused
     * to store the copy of the pixels.
     * A new {@code Image} will be created if the supplied object is null,
     * is too small or of a type which the image pixels cannot be easily
     * converted into.
     *
     * @param bimg the {@code BufferedImage} object to be converted
     * @param wimg an optional {@code WritableImage} object that can be
     *        used to store the returned pixel data
     * @return an {@code Image} object representing a snapshot of the
     *         current pixels in the {@code BufferedImage}.
     * @since JavaFX 2.2
     */
    public static WritableImage toFXImage(BufferedImage bimg, WritableImage wimg) {
        return SwingFXUtils.toFXImage(bimg, wimg);
    }

    /**
     * Determine the appropriate {@link WritablePixelFormat} type that can
     * be used to transfer data into the indicated BufferedImage.
     *
     * @param bimg the BufferedImage that will be used as a destination for
     *             a {@code PixelReader<IntBuffer>#getPixels()} operation.
     * @return
     */
    private static WritablePixelFormat<IntBuffer>
        getAssociatedPixelFormat(BufferedImage bimg)
    {
        switch (bimg.getType()) {
            // We lie here for xRGB, but we vetted that the src data was opaque
            // so we can ignore the alpha.  We use ArgbPre instead of Argb
            // just to get a loop that does not have divides in it if the
            // PixelReader happens to not know the data is opaque.
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                return PixelFormat.getIntArgbPreInstance();
            case BufferedImage.TYPE_INT_ARGB:
                return PixelFormat.getIntArgbInstance();
            default:
                // Should not happen...
                throw new InternalError("Failed to validate BufferedImage type");
        }
    }

    /**
     * Snapshots the specified JavaFX {@link Image} object and stores a
     * copy of its pixels into a {@link BufferedImage} object, creating
     * a new object if needed.
     * The method will only convert a JavaFX {@code Image} that is readable
     * as per the conditions on the
     * {@link Image#getPixelReader() Image.getPixelReader()}
     * method.
     * If the {@code Image} is not readable, as determined by its
     * {@code getPixelReader()} method, then this method will return null.
     * If the {@code Image} is a writable, or other dynamic image, then
     * the {@code BufferedImage} will only be set to the current state of
     * the pixels in the image as determined by its {@link PixelReader}.
     * Further changes to the pixels of the {@code Image} will not be
     * reflected in the returned {@code BufferedImage}.
     * <p>
     * The optional {@code BufferedImage} parameter may be reused to store
     * the copy of the pixels.
     * A new {@code BufferedImage} will be created if the supplied object
     * is null, is too small or of a type which the image pixels cannot
     * be easily converted into.
     *
     * @param img the JavaFX {@code Image} to be converted
     * @param bimg an optional {@code BufferedImage} object that may be
     *        used to store the returned pixel data
     * @return a {@code BufferedImage} containing a snapshot of the JavaFX
     *         {@code Image}, or null if the {@code Image} is not readable.
     * @since JavaFX 2.2
     */
    public static BufferedImage fromFXImage(Image img, BufferedImage bimg) {
        PixelReader pr = img.getPixelReader();
        if (pr == null) {
            return null;
        }
        int iw = (int) img.getWidth();
        int ih = (int) img.getHeight();
        int prefBimgType = BufferedImage.TYPE_INT_RGB;
        if (bimg != null) {
            int bw = bimg.getWidth();
            int bh = bimg.getHeight();
            if (bw < iw || bh < ih || bimg.getType() != prefBimgType) {
                bimg = null;
            } else if (iw < bw || ih < bh) {
                Graphics2D g2d = bimg.createGraphics();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, bw, bh);
                g2d.dispose();
            }
        }
        if (bimg == null) {
            bimg = new BufferedImage(iw, ih, prefBimgType);
        }
        IntegerComponentRaster icr = (IntegerComponentRaster) bimg.getRaster();
        int offset = icr.getDataOffset(0);
        int scan = icr.getScanlineStride();
        int data[] = icr.getDataStorage();
        WritablePixelFormat<IntBuffer> pf = getAssociatedPixelFormat(bimg);
        pr.getPixels(0, 0, iw, ih, pf, data, offset, scan);
        return bimg;
    }
}
