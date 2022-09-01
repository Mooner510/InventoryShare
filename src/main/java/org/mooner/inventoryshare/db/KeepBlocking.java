package org.mooner.inventoryshare.db;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;
import org.mooner.inventoryshare.InventoryShare;

import java.util.UUID;

public class KeepBlocking {
    private BukkitTask task;
    private int delayed;
    private final UUID uuid;
    private final Stopper stopper;

    public KeepBlocking(long id, UUID uuid, Runnable runnable) {
        this.uuid = uuid;
        Bukkit.getPluginManager().registerEvents(stopper = new Stopper(), InventoryShare.plugin);
        delayed = 0;
        task = Bukkit.getScheduler().runTaskTimer(InventoryShare.plugin, () -> {
            if(delayed++ >= 100) {
                InventoryShare.plugin.getLogger().info("Delayed Skip");
                ShareDB.init.setAccess(id, 0);
                runnable.run();
                unregister();
                task.cancel();
            } else if (ShareDB.init.getAccess(id) >= ShareDB.init.getMaxConnection()) {
                InventoryShare.plugin.getLogger().info("Indexing Skip");
                ShareDB.init.setAccess(id, 0);
                runnable.run();
                unregister();
                task.cancel();
            }
        }, 0, 5);
    }

    public void unregister() {
        HandlerList.unregisterAll(stopper);
    }

    public static boolean loc(Location loc1, Location loc2) {
        return loc2 != null && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ();
    }

    private class Stopper implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onMove(PlayerMoveEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid) && !loc(e.getFrom(), e.getTo())) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlace(BlockPlaceEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onMultiPlace(BlockMultiPlaceEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBucketFill(PlayerBucketEmptyEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBreak(BlockBreakEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBucketFill(PlayerBucketFillEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInteract(PlayerInteractEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInteractEntity(PlayerInteractEntityEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onTeleport(PlayerTeleportEvent e) {
            if(e.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
                if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPickup(EntityPickupItemEvent e) {
            if (e.getEntity() instanceof Player p) {
                if (p.getUniqueId().equals(uuid)) e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onThrow(PlayerDropItemEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onDamage(EntityDamageByEntityEvent e) {
            if(e.getDamager() instanceof Player p) {
                if (p.getUniqueId().equals(uuid)) e.setCancelled(true);
            } else if(e.getEntity() instanceof Player p) {
                if (e.getDamager() instanceof Player) return;
                if (p.getUniqueId().equals(uuid)) e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onClick(InventoryClickEvent e) {
            if (e.getWhoClicked().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onClick(InventoryOpenEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onClick(InventoryDragEvent e) {
            if (e.getWhoClicked().getUniqueId().equals(uuid)) e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPickupEXP(PlayerExpChangeEvent e) {
            if (e.getPlayer().getUniqueId().equals(uuid)) e.setAmount(0);
        }
    }
}
