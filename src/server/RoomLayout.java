package server;

import java.util.HashMap;
import java.util.Map;

import common.LowDiscrepancySeq;
import javafx.geometry.Point2D;

public class RoomLayout {

    private static Map<Room, Integer> indexes = new HashMap<Room, Integer>();

    public static Point2D getLocation(Room room) {
        Integer index = indexes.get(room);
        if (index == null) {
            index = 1;
            indexes.put(room, index);
        }

        Point2D p = LowDiscrepancySeq.haltonPoint(index);
        indexes.put(room, index + 1);
        return p;
    }

}
