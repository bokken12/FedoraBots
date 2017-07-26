package common;

/**
 * Describes the various constants in the game
 */
public class Constants {
    public static class World {
        public static final int WIDTH = 500;
        public static final int HEIGHT = 400;
    }

    public static class Robot {
        public static final double MAX_VELOCITY = 100;
        public static final double MAX_ACCELERATION = 100;
        public static final double RADIUS = 10;
        public static final double MASS = 10;
        /**
         * Milliseconds that the turret must take to cooldown between each shot
         */
        public static final long SHOOT_FREQUENCY = 1000;
    }

    public static class Bullet {
        public static final double VELOCITY = 200;
        public static final double RADIUS = 2;
        public static final double MASS = 2;
    }
}
