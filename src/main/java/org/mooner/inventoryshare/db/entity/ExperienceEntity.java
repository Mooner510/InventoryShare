package org.mooner.inventoryshare.db.entity;

public class ExperienceEntity {
    private final int level;
    private final float exp;

    public ExperienceEntity() {
        level = 0;
        exp = 0;
    }

    public ExperienceEntity(int level, float exp) {
        this.level = level;
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public float getExp() {
        return exp;
    }
}
