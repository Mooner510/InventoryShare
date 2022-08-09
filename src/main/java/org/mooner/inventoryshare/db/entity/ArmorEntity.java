package org.mooner.inventoryshare.db.entity;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ArmorEntity {
    private final ItemStack[] items;

    public enum ArmorSlot {
        HEAD, CHEST, LEG, FEET, OFFHAND
    }

    public ArmorEntity() {
        items = null;
    }

    public ArmorEntity(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack shield) {
        items = new ItemStack[]{helmet, chestplate, leggings, boots, shield};
    }

    @Nullable
    public ItemStack getItem(ArmorSlot slot) {
        return items == null ? null : items[slot.ordinal()];
    }
}
