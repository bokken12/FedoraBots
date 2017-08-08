package fedorabots.client.event;

import java.util.EventObject;

public class Event extends EventObject {
    private static final long serialVersionUID = -4802024259688831730L;

    private Object target;

    public Event(Object source, Object target) {
        super(source);
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }
}
