package fedorabots.server;

import java.util.HashMap;
import java.util.Map;

import fedorabots.common.Constants;
import fedorabots.common.LowDiscrepancySeq;
import javafx.geometry.Point2D;

public class RoomLayout {

    private static Map<Room, Integer> indexes = new HashMap<Room, Integer>();

    public static Point2D getLocation(int index) {
        Point2D p = LowDiscrepancySeq.haltonPoint(index);
        return new Point2D(p.getX() * Constants.World.WIDTH,
                           p.getY() * Constants.World.HEIGHT);
    }

    public static Point2D getLocation(Room room) {
        Integer index = indexes.get(room);
        if (index == null) {
            index = 1;
            indexes.put(room, index);
        }

        indexes.put(room, index + 1);
        return getLocation(index);
    }

}
