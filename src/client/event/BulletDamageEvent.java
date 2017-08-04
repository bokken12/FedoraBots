package client.event;

import client.Robot;

public class BulletDamageEvent extends HealthDamageEvent {
    private static final long serialVersionUID = -5387871389260455163L;

    public BulletDamageEvent(Bullet source, Robot target, double healthChange) {
        super(source, target, healthChange);
    }

}
