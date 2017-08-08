package fedorabots.lab;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * Class for activities relating the the lab "Find Randy the Red Square"
 */
public class Randy {

    /**
     * Returns the nth image of randy, where n is in the range 1 to 12 inclusive.
     */
    public static BufferedImage getImage(int n) {
        if (n < 1 || n > 12) {
            throw new IllegalArgumentException("Parameter n must be in the range 1 to 12 inclusive.");
        }
        InputStream stream = Randy.class.getResourceAsStream(String.format("/randy/randy-%02d.jpg", n));
        try {
            return ImageIO.read(stream);
        } catch (IOException|IllegalArgumentException e) {
            throw new RuntimeException("Could not load the image of Randy.");
        }
    }

}
