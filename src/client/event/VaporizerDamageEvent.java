package client.event;

import client.Robot;

public class VaporizerDamageEvent extends HealthDamageEvent {
    private static final long serialVersionUID = -1995667596115219589L;

    public VaporizerDamageEvent(Vaporizer source, Robot target, double healthChange) {
        super(source, target, healthChange);
    }

}
