package example;

import client.Robot;

class CircleBot extends Robot {

    public static void main(String[] args) throws InterruptedException {
        CircleBot b = new CircleBot();
        b.joinGame();
        b.setAcceleration(20, 0);
        Thread.sleep(6000);
        while (true) {
            double velocityAngle = Math.atan2(b.getVy(), b.getVx());
            double accelerationAngle = velocityAngle + Math.PI / 2;
            // System.out.println(b.getVx() + " " + b.getVy());
            // System.out.println((90 * Math.cos(accelerationAngle)) + " " + (90 * Math.sin(accelerationAngle)));
            // System.out.println();
            b.setAcceleration(20 * Math.cos(accelerationAngle), 20 * Math.sin(accelerationAngle));
            System.out.println(Math.sqrt(b.getVx() * b.getVx() + b.getVy() * b.getVy()));
            Thread.sleep(40);
        }
    }

}
