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
        public static final double RADIUS = 4;
        public static final double MASS = 2;
        public static final double DAMAGE = 0.25;
    }

    public class Obstacle {
        public static final double RADIUS = 10;
        /**
         * Milliseconds that the turret's turret must take to cooldown between each shot
         */
        public static final double TURRET_SHOOT_FREQUENCY = 5000;
        /**
         * The distance measured from the center of the turret to the edge of
         * the robot that must be between the two for the turret to shoot the
         * robot.
         */
        public static final double TURRET_RANGE = 100;
        /**
         * Milliseconds that the vaporizer must take to cooldown between each pulse
         */
        public static final double VAPORIZER_PULSE_FREQUENCY = 5000;
        /**
         * Milliseconds that the vaporizer's pulse takes to reach it's hightest point
         */
        public static final double VAPORIZER_PULSE_LENGTH = 1000;
        /**
         * The distance measured from the center of the vaporizer to the edge of
         * the robot that must be between the two for the vaporizer to pulse the
         * robot.
         */
        public static final double VAPORIZER_RANGE = 50;
        /**
         * The distance measured from the center of the jammer to the edge of
         * the robot that must be between the two for the jammer to disrupt the
         * robot's sensors.
         */
        public static final double JAMMER_RANGE = 200;
    }
}
