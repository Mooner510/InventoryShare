package org.mooner.inventoryshare.db.entity;

import org.bukkit.potion.PotionEffect;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

public class PotionEffectEntity {
    private final Set<PotionEffect> set;

    public PotionEffectEntity() {
        this.set = Collections.emptySet();
    }

    public PotionEffectEntity(Set<PotionEffect> set) {
        this.set = set;
    }

    public void forEach(Consumer<PotionEffect> action) {
        set.forEach(action);
    }
}
