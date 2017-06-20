package example;

import client.Robot;

class CircleBot extends Robot {

    public static void main(String[] args) throws InterruptedException {
        CircleBot b = new CircleBot();
        b.joinGame();
        b.setAcceleration(0, 10);
        while (true) {
            Thread.sleep(100);
        }
    }

}
