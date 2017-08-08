package fedorabots.client.event;

import fedorabots.client.Robot;

public class HealthDamageEvent extends Event {
    private static final long serialVersionUID = -6389304280358774957L;

    private double healthChange;

    public HealthDamageEvent(Object source, Robot target, double healthChange) {
        super(source, target);
        this.healthChange = healthChange;
    }

    public double getHealthChange() {
        return healthChange;
    }

}
