package fedorabots.client;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import boofcv.alg.misc.GImageStatistics;
import boofcv.gui.image.VisualizeImageData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageGray;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * A utitlity class for displaying images. It uses JavaFX while BoofCV's image
 * display methods use Swing, and aims to more closely mirror OpenCV's
 * displaying functions.
 */
public abstract class ImageDisplay {

    private static class MapInfo {
        public Stage stage;
        public ImageView view;
        public WritableImage image;
    }

    private static Map<String, MapInfo> windows = new HashMap<String, MapInfo>();

    public static void showImage(String name, BufferedImage image) {
        DisplayManager.getInstance(); // Make sure JavaFX is initialized
        Platform.runLater(() -> {
            MapInfo info = windows.get(name);
            if (info == null) {
                info = new MapInfo();
                info.image = SwingFXUtils.toFXImage(image, null);
                info.stage = new Stage();
                info.stage.setX(info.stage.getX() + 50);
                info.stage.setY(info.stage.getY() + 50);
                info.stage.setTitle(name);
                info.view = new ImageView();
                info.view.fitWidthProperty().bind(info.stage.widthProperty());
                info.view.fitHeightProperty().bind(info.stage.heightProperty());
                info.view.setPreserveRatio(true);
                StackPane p = new StackPane();
                p.getChildren().add(info.view);
                StackPane.setAlignment(info.view, Pos.CENTER);
                info.stage.setScene(new Scene(p, image.getWidth(), image.getHeight(), Color.BLACK));
                windows.put(name, info);
            } else {
                SwingFXUtils.toFXImage(image, info.image);
            }
            if (!info.stage.isShowing()) {
                info.stage.show();
            }
            info.view.setImage(info.image);
        });
    }

    public static void showImage(String name, ImageBase<?> image) {
		BufferedImage buff = ConvertBufferedImage.convertTo(image, null, true);
		showImage(name, buff);
	}

    public static void showImage(String name, ImageGray<?> image, boolean showMagnitude) {
        double max = GImageStatistics.maxAbs(image);
		BufferedImage buff;
		if(showMagnitude && max > 0)
			buff = VisualizeImageData.grayMagnitude(image, null, max);
		else if (!showMagnitude)
            buff = VisualizeImageData.colorizeSign(image, null, max);
        else
            buff = VisualizeImageData.grayMagnitude(image, null, 1); // Should be a black image

		showImage(name, buff);
    }

    public static void closeAllWindows() {
        for (MapInfo info : windows.values()) {
            info.stage.close();
        }
    }
}
