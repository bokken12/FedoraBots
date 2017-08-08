package fedorabots.client.event;

import fedorabots.client.Robot;

public class BulletDamageEvent extends HealthDamageEvent {
    private static final long serialVersionUID = -5387871389260455163L;

    public BulletDamageEvent(Bullet source, Robot target, double healthChange) {
        super(source, target, healthChange);
    }

    @Override
    public Bullet getSource() {
        return (Bullet) super.getSource();
    }

}
