package client.event;

import java.util.EventListener;

@FunctionalInterface
public interface EventHandler<T extends Event> extends EventListener {

    public void handle(T Event);

}
