package fedorabots.example;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import fedorabots.client.ImageDisplay;
import fedorabots.client.Robot;
import javafx.scene.paint.Color;

class CircleBot {

    public static void main(String[] args) throws InterruptedException {
        Robot b = new Robot();
        b.setColor(Color.ORANGERED);
        // b.joinNetworkGame((short) 0);
        b.joinLocalGame(6);
        b.setAcceleration(20, 0);

        b.addBulletDamageListener(event -> {
            System.out.println(event.getSource());
        });
        b.addVaporizerDamageListener(event -> {
            System.out.println(event.getSource());
        });
        while (b.getVx() < 25) {
            Thread.sleep(10);
        }
        while (!b.isDead()) {
            double velocityAngle = Math.atan2(b.getVy(), b.getVx());
            double accelerationAngle = velocityAngle + Math.PI / 2;

            b.setAcceleration(20 * Math.cos(accelerationAngle), 20 * Math.sin(accelerationAngle));

            // For display angle = 0 is up, but for calculations angle = 0 is to the left.
            double displayAngle = accelerationAngle + Math.PI / 2;
            b.setBlasterRotation(displayAngle * 180 / Math.PI);

            if (b.canShoot()) {
                System.out.println(b.nearbyEntities());
                b.shoot();
            }

            // System.out.println(Math.sqrt(b.getVx() * b.getVx() + b.getVy() * b.getVy()));

            Planar<GrayF32> rgb = ConvertBufferedImage.convertFromMulti(b.getDisplayImage(),null,true,GrayF32.class);
            Planar<GrayF32> hsv = rgb.createSameShape();
            ColorHsv.rgbToHsv_F32(rgb, hsv);

            GrayF32 V = hsv.getBand(2);
            int brightness = (int) (Color.LIGHTGRAY.getBrightness() * 255) - 1;
            GrayU8 threshed = ThresholdImageOps.threshold(V, null, brightness, true);

            ImageDisplay.showImage("Image", threshed, true);

            Thread.sleep(40);
        }
    }

}
