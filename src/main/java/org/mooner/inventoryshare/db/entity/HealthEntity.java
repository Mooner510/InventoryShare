package org.mooner.inventoryshare.db.entity;

public class HealthEntity {
    private final double health;
    private final double absorptionHealth;
    private final int hunger;

    public HealthEntity() {
        this.health = 20;
        this.absorptionHealth = 0;
        this.hunger = 20;
    }

    public HealthEntity(double health, double absorptionHealth, int hunger) {
        this.health = health;
        this.absorptionHealth = absorptionHealth;
        this.hunger = hunger;
    }

    public double getHealth() {
        return health;
    }

    public double getAbsorptionHealth() {
        return absorptionHealth;
    }

    public int getHunger() {
        return hunger;
    }
}
