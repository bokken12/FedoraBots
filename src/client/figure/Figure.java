package client.figure;

import afester.javafx.svg.SvgLoader;
import javafx.scene.Group;
import javafx.scene.transform.Scale;

public interface Figure {

    public static final SvgLoader LOADER = new SvgLoader();
    public static final int SVG_SIZE = 64;

    public static Group loadSvg(String file) {
        return LOADER.loadSvg(Figure.class.getResourceAsStream(file));
    }

    public static void setGroupSize(Group group, double size) {
        // Scale down the robot to the correct size
        double scale = size / SVG_SIZE;
        group.getTransforms().add(new Scale(scale, scale, SVG_SIZE/2, SVG_SIZE/2));

        // The SVG has a center (0, 0) but the program needs a center of
        // (width/2, height/2)
        group.setTranslateX(-SVG_SIZE/2);
        group.setTranslateY(-SVG_SIZE/2);
    }

}
