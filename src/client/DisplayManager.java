package client;

import java.net.ConnectException;
import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class DisplayManager extends Application {

    private static DisplayManager instance;
    private static CountDownLatch latch = new CountDownLatch(1);

    public DisplayManager() {
        instance = this;
    }

    public synchronized static DisplayManager getInstance() {
        if (instance == null) {
            Thread displayLauncher = new Thread(() -> {
                try {
                    launch();
                    System.exit(0);
                } catch (Exception e) {
                    Throwable cause = e.getCause().getCause();
                    if (cause instanceof ConnectException) {
                        new RuntimeException("Could not connect to server", cause).printStackTrace();
                    } else {
                        cause.printStackTrace();
                    }
                    System.exit(1);
                }
            });
            displayLauncher.setDaemon(false);
            displayLauncher.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public void start(Stage primaryStage) {
        latch.countDown();
    }

    public void run(Runnable runnable) {
        CountDownLatch runLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            runnable.run();
            runLatch.countDown();
        });
        try {
            runLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
