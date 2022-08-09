package org.mooner.inventoryshare.db.entity;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

public class InventoryEntity {
    private final Map<Integer, ItemStack> map;

    public InventoryEntity() {
        this.map = Collections.emptyMap();
    }

    public InventoryEntity(Map<Integer, ItemStack> map) {
        this.map = map;
    }

    public void forEach(BiConsumer<Integer, ItemStack> action) {
        for (int i = 0; i < 36; i++) action.accept(i, map.get(i));
    }
}
