package org.mooner.inventoryshare;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.mooner.inventoryshare.db.ShareDB;
import org.mooner.inventoryshare.listener.InvShareListener;

public final class InventoryShare extends JavaPlugin {
    public static InventoryShare plugin;

    @Override
    public void onEnable() {
        plugin = this;
        this.getLogger().info("Plugin Enabled!");

        ShareDB.init = new ShareDB();

        Bukkit.getPluginManager().registerEvents(new InvShareListener(), this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info("Plugin Disabled!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            ShareDB.init.saveArmor(p);
            ShareDB.init.saveEnderChest(p);
            ShareDB.init.saveExperience(p);
            ShareDB.init.saveHealth(p);
            ShareDB.init.saveInventory(p);
            ShareDB.init.savePotion(p);
        }
    }
}
