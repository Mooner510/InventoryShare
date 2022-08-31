package org.mooner.inventoryshare.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.mooner.inventoryshare.InventoryShare;
import org.mooner.inventoryshare.db.ShareDB;
import org.mooner.inventoryshare.db.entity.*;
import org.mooner.inventoryshare.exception.RefreshError;

import java.util.ArrayList;

public class InvShareListener implements Listener {
    private static String chat(String msg){
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        p.sendMessage(chat("&a[ &fDATA &a] &e데이터 불러오는중..."));
        p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        final long id = ShareDB.init.getKey(p.getUniqueId());
        ShareDB.init.keepBlock(id, p.getUniqueId(), () -> {
            ArrayList<String> list = new ArrayList<>(5);
            long start;
            start = System.currentTimeMillis();
            try {
                final ArmorEntity entity = ShareDB.init.getArmor(id);
                InventoryShare.plugin.getLogger().info("ArmorEntity");
                if(entity != null) {
                    InventoryShare.plugin.getLogger().info("Loaded");
                    p.getInventory().setHelmet(entity.getItem(ArmorEntity.ArmorSlot.HEAD));
                    p.getInventory().setChestplate(entity.getItem(ArmorEntity.ArmorSlot.CHEST));
                    p.getInventory().setLeggings(entity.getItem(ArmorEntity.ArmorSlot.LEG));
                    p.getInventory().setBoots(entity.getItem(ArmorEntity.ArmorSlot.FEET));
                    p.getInventory().setItemInOffHand(entity.getItem(ArmorEntity.ArmorSlot.OFFHAND));
                }
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x19");
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            try {
                final ExperienceEntity entity = ShareDB.init.getExperience(id);
                InventoryShare.plugin.getLogger().info("ExperienceEntity");
                if(entity != null) {
                    InventoryShare.plugin.getLogger().info("Loaded");
                    p.setLevel(entity.getLevel());
                    p.setExp(entity.getExp());
                }
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x28");
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            PotionEffectEntity potion;
            try {
                PotionEffectEntity entity = ShareDB.init.getPotion(id);
                InventoryShare.plugin.getLogger().info("PotionEffectEntity");
                InventoryShare.plugin.getLogger().info("Loaded");
                Bukkit.getScheduler().runTask(InventoryShare.plugin, () -> {
                    for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
                    entity.forEach(p::addPotionEffect);
                });
                potion = entity;
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x55");
                potion = null;
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            try {
                final HealthEntity entity = ShareDB.init.getHealth(id);
                InventoryShare.plugin.getLogger().info("HealthEntity");
                if(entity != null) {
                    InventoryShare.plugin.getLogger().info("Loaded");
                    p.setHealth(Math.min(entity.getHealth(), p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                    if(potion != null && potion.hasAbsorption()) p.setAbsorptionAmount(entity.getAbsorptionHealth());
                    else p.setAbsorptionAmount(0);
                    p.setFoodLevel(entity.getHunger());
                }
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x37");
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            try {
                final InventoryEntity entity = ShareDB.init.getInventory(id);
                InventoryShare.plugin.getLogger().info("InventoryEntity");
                if(entity != null) {
                    InventoryShare.plugin.getLogger().info("Loaded");
                    entity.forEach((slot, i) -> p.getInventory().setItem(slot, i));
                }
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x46");
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();

            try {
                final EnderChestEntity entity = ShareDB.init.getEnderChest(id);
                InventoryShare.plugin.getLogger().info("EnderChestEntity");
                if(entity != null) {
                    InventoryShare.plugin.getLogger().info("Loaded");
                    entity.forEach((slot, i) -> p.getEnderChest().setItem(slot, i));
                }
            } catch (RefreshError err) {
                err.printStackTrace();
                list.add("0x64");
            }
            InventoryShare.plugin.getLogger().info("Took: " + (System.currentTimeMillis() - start) + "ms");

            if(list.isEmpty()) {
                p.sendMessage(chat("&a[ &fDATA &a] &e서버 동기화 성공!"));
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else {
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1);
                p.sendMessage(chat("&a[ &fDATA &a] &c데이터를 불러오는 중 오류가 발생했습니다. 관리자에게 문의하세요."));
                p.sendMessage(chat("&a[ &fDATA &a] &c오류 코드: " + String.join(", ", list)));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
//        ShareDB.init.setAccess(ShareDB.init.getKey(p.getUniqueId()), 0);
        Bukkit.getScheduler().runTaskAsynchronously(InventoryShare.plugin, () -> {
            ShareDB.init.saveArmor(p);
            ShareDB.init.saveExperience(p);
            ShareDB.init.saveHealth(p);
            ShareDB.init.saveInventory(p);
            ShareDB.init.saveEnderChest(p);
            ShareDB.init.savePotion(p);
        });
    }
}
