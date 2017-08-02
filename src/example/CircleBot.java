package example;

import boofcv.alg.color.ColorHsv;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import client.ImageDisplay;
import client.Robot;
import javafx.scene.paint.Color;

class CircleBot extends Robot {

    public static void main(String[] args) throws InterruptedException {
        CircleBot b = new CircleBot();
        b.setColor(Color.ORANGERED);
        // b.joinNetworkGame((short) 0);
        b.joinLocalGame(6);
        b.setAcceleration(20, 0);
        while (b.getVx() < 25) {
            Thread.sleep(10);
        }
        while (!b.isDead()) {
            double velocityAngle = Math.atan2(b.getVy(), b.getVx());
            double accelerationAngle = velocityAngle + Math.PI / 2;

            b.setAcceleration(20 * Math.cos(accelerationAngle), 20 * Math.sin(accelerationAngle));

            // For display angle = 0 is up, but for calculations angle = 0 is to the left.
            double displayAngle = accelerationAngle + Math.PI / 2;
            b.setRotation(displayAngle * 180 / Math.PI);

            // if (b.canShoot()) {
            //     b.shoot();
            // }

            // System.out.println(Math.sqrt(b.getVx() * b.getVx() + b.getVy() * b.getVy()));

            Planar<GrayF32> rgb = ConvertBufferedImage.convertFromMulti(b.getDisplayImage(),null,true,GrayF32.class);
            Planar<GrayF32> hsv = rgb.createSameShape();
            ColorHsv.rgbToHsv_F32(rgb, hsv);

            GrayF32 H = hsv.getBand(0);
            GrayF32 S = hsv.getBand(1);
            GrayF32 V = hsv.getBand(2);

            ImageDisplay.showImage("H", H, false);
            ImageDisplay.showImage("S", S, false);
            ImageDisplay.showImage("V", V, false);

            Thread.sleep(40);
        }
    }

}
