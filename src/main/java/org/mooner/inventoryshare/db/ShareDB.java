package org.mooner.inventoryshare.db;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mooner.inventoryshare.InventoryShare;
import org.mooner.inventoryshare.db.entity.*;
import org.mooner.inventoryshare.exception.RefreshError;
import org.mooner.inventoryshare.parser.ItemParser;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ShareDB {
    public static ShareDB init;
    private final HashMap<UUID, Long> key;
    private static final String dbPath = "../db/";
    private static final String CONNECTION = "jdbc:sqlite:" + dbPath + "player.db";
    private final int maxConnection;

    public ShareDB() {
        key = new HashMap<>();

        int connections = 0;
        new File(dbPath).mkdirs();
        File db = new File(dbPath, "player.db");
        if(!db.exists()) {
            try {
                db.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Armor (" +
                                "id INTEGER NOT NULL UNIQUE," +
                                "helmet BLOB," +
                                "chestplate BLOB," +
                                "leggings BLOB," +
                                "boots BLOB," +
                                "shield BLOB," +
                                "PRIMARY KEY(id))")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - ArmorDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Experience (" +
                                "id INTEGER NOT NULL UNIQUE," +
                                "level INTEGER NOT NULL," +
                                "exp REAL NOT NULL," +
                                "PRIMARY KEY(id))")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - ExperienceDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS Health (" +
                                "id INTEGER NOT NULL UNIQUE," +
                                "health REAL NOT NULL," +
                                "aHealth REAL NOT NULL," +
                                "hunger INTEGER NOT NULL," +
                                "PRIMARY KEY(id))")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - HealthDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS Inventory (" +
                                "id INTEGER NOT NULL," +
                                "slot INTEGER NOT NULL," +
                                "data BLOB)")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - InventoryDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("CREATE TABLE IF NOT EXISTS EnderChest (" +
                        "id INTEGER NOT NULL," +
                        "slot INTEGER NOT NULL," +
                        "data BLOB)")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - EnderChestDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS PotionEffect (" +
                                "id INTEGER NOT NULL," +
                                "type TEXT NOT NULL," +
                                "level INTEGER NOT NULL," +
                                "time INTEGER NOT NULL," +
                                "particle INTEGER NOT NULL)")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - PotionEffectDB 를 생성했습니다.");
            connections++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        maxConnection = connections;
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS Player (" +
                                "id INTEGER NOT NULL UNIQUE," +
                                "uuid TEXT NOT NULL UNIQUE," +
                                "loaded INTEGER NOT NULL," +
                                "PRIMARY KEY(id AUTOINCREMENT))")
        ) {
            s.execute();
            InventoryShare.plugin.getLogger().info("성공적으로 Player - PlayerDB 를 생성했습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxConnection() {
        return maxConnection;
    }

    public long getKey(UUID uuid) {
        Long i = key.get(uuid);
        if(i != null) return i;
        return getKeyFromDB(uuid);
    }

    private long getKeyFromDB(UUID uuid) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT id FROM Player WHERE uuid=?")
        ) {
            s.setString(1, uuid.toString());
            try (
                    ResultSet r = s.executeQuery()
            ) {
                if(r.next()) {
                    long i = r.getLong(1);
                    key.put(uuid, i);
                    return i;
                } else {
                    try (
                            Connection c2 = DriverManager.getConnection(CONNECTION);
                            PreparedStatement s2 = c2.prepareStatement("INSERT INTO Player (uuid, loaded) VALUES(?, 0)")
                    ) {
                        s2.setString(1, uuid.toString());
                        s2.executeUpdate();
                        return getKeyFromDB(uuid);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(new NullPointerException("Can't create key from DB."));
    }

    public int getAccess(long id) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("SELECT loaded FROM Player WHERE id=?");
        ) {
            s2.setLong(1, id);
            try (
                    final ResultSet r = s2.executeQuery()
            ) {
                if(r.next()) {
                    return r.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void addAccess(long id) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Player SET loaded=(loaded+1) WHERE id=?");
        ) {
            s2.setLong(1, id);
            s2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAccess(long id, int v) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Player SET loaded=? WHERE id=?");
        ) {
            s2.setInt(1, v);
            s2.setLong(2, id);
            s2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void keepBlock(long id, UUID uuid, Runnable runnable) {
        new KeepBlocking(id, uuid, runnable);
    }

    @Nullable
    public ArmorEntity getArmor(UUID uuid) throws RefreshError {
        return getArmor(getKey(uuid));
    }

    @Nullable
    public ArmorEntity getArmor(long id) throws RefreshError {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT * FROM Armor WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                if(r.next())
                    return new ArmorEntity(
                            ItemParser.itemFromSerial(r.getBytes("helmet")),
                            ItemParser.itemFromSerial(r.getBytes("chestplate")),
                            ItemParser.itemFromSerial(r.getBytes("leggings")),
                            ItemParser.itemFromSerial(r.getBytes("boots")),
                            ItemParser.itemFromSerial(r.getBytes("shield"))
                    );
                return null;
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveArmor(Player p) {
        saveArmor(getKey(p.getUniqueId()),
                ItemParser.itemToSerial(p.getInventory().getHelmet()),
                ItemParser.itemToSerial(p.getInventory().getChestplate()),
                ItemParser.itemToSerial(p.getInventory().getLeggings()),
                ItemParser.itemToSerial(p.getInventory().getBoots()),
                ItemParser.itemToSerial(p.getInventory().getItemInOffHand())
        );
    }

    private void saveArmor(long id, byte[] h, byte[] cp, byte[] leg, byte[] b, byte[] off) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Armor SET helmet=?, chestplate=?, leggings=?, boots=?, shield=? WHERE id=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO Armor VALUES(?, ?, ?, ?, ?, ?)")
        ) {
            s2.setBytes(1, h);
            s2.setBytes(2, cp);
            s2.setBytes(3, leg);
            s2.setBytes(4, b);
            s2.setBytes(5, off);
            s2.setLong(6, id);
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setBytes(2, h);
                s.setBytes(3, cp);
                s.setBytes(4, leg);
                s.setBytes(5, b);
                s.setBytes(6, off);
                s.executeUpdate();
            }
            addAccess(id);
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveArmor(id, h, cp, leg, b, off), 5);
            e.printStackTrace();
        }
    }

    @Nullable
    public ExperienceEntity getExperience(UUID uuid) throws RefreshError {
        return getExperience(getKey(uuid));
    }

    @Nullable
    public ExperienceEntity getExperience(long id) throws RefreshError {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("SELECT * FROM Experience WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                if(r.next())
                    return new ExperienceEntity(r.getInt("level"), r.getFloat("exp"));
                return null;
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveExperience(Player p) {
        saveExperience(getKey(p.getUniqueId()), p.getLevel(), p.getExp());
    }

    private void saveExperience(long id, int level, float exp) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("INSERT INTO Experience VALUES(?, ?, ?) ON CONFLICT(id) DO UPDATE SET level=?, exp=?")
        ) {
            s.setLong(1, id);
            s.setInt(2, level);
            s.setFloat(3, exp);
            s.setInt(4, level);
            s.setFloat(5, exp);
            s.executeUpdate();
            addAccess(id);
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveExperience(id, level, exp), 5);
            e.printStackTrace();
        }
    }

    @Nullable
    public HealthEntity getHealth(UUID uuid) throws RefreshError {
        return getHealth(getKey(uuid));
    }

    @Nullable
    public HealthEntity getHealth(long id) throws RefreshError {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT * FROM Health WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                if(r.next())
                    return new HealthEntity(r.getDouble("health"), r.getDouble("aHealth"), r.getInt("hunger"));
                return null;
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveHealth(Player p) {
        saveHealth(getKey(p.getUniqueId()), p.getHealth(), p.getAbsorptionAmount(), p.getFoodLevel());
    }

    private void saveHealth(long id, double hp, double aHp, int food) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("INSERT INTO Health VALUES(?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET health=?, aHealth=?, hunger=?")
        ) {
            s.setLong(1, id);
            s.setDouble(2, hp);
            s.setDouble(3, aHp);
            s.setDouble(4, food);
            s.setDouble(5, hp);
            s.setDouble(6, aHp);
            s.setDouble(7, food);
            s.executeUpdate();
            addAccess(id);
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveHealth(id, hp, aHp, food), 5);
            e.printStackTrace();
        }
    }

    @Nullable
    public InventoryEntity getInventory(UUID uuid) throws RefreshError {
        return getInventory(getKey(uuid));
    }

    @Nullable
    public InventoryEntity getInventory(long id) throws RefreshError {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT * FROM Inventory WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                HashMap<Integer, ItemStack> map = new HashMap<>();
                while(r.next()) map.put(r.getInt("slot"), ItemParser.itemFromSerial(r.getBytes("data")));
                return map.isEmpty() ? null : new InventoryEntity(map);
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveInventory(Player p) {
        final long id = getKey(p.getUniqueId());
        HashMap<Integer, ItemStack> stacks = new HashMap<>(36);
        for (int i = 0; i < 36; i++) {
            final ItemStack item = p.getInventory().getItem(i);
            stacks.put(i, item == null ? null : item.clone());
        }
        for (int i = 0; i < 36; i++) saveSlot(id, i, stacks.get(i));
        addAccess(id);
    }

    private void saveSlot(long id, int slot, ItemStack i) {
        byte[] map = ItemParser.itemToSerial(i);
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Inventory SET data=? WHERE id=? and slot=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO Inventory VALUES(?, ?, ?)")
        ) {
            s2.setBytes(1, map);
            s2.setLong(2, id);
            s2.setInt(3, slot);
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setInt(2, slot);
                s.setBytes(3, map);
                s.executeUpdate();
            }
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveSlot(id, slot, i), 5);
            e.printStackTrace();
        }
    }

    @Nullable
    public EnderChestEntity getEnderChest(UUID uuid) throws RefreshError {
        return getEnderChest(getKey(uuid));
    }

    @Nullable
    public EnderChestEntity getEnderChest(long id) throws RefreshError {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT * FROM EnderChest WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                HashMap<Integer, ItemStack> map = new HashMap<>();
                while(r.next()) map.put(r.getInt("slot"), ItemParser.itemFromSerial(r.getBytes("data")));
                return map.isEmpty() ? null : new EnderChestEntity(map);
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveEnderChest(Player p) {
        final long id = getKey(p.getUniqueId());
        HashMap<Integer, ItemStack> stacks = new HashMap<>(27);
        for (int i = 0; i < 27; i++) {
            final ItemStack item = p.getEnderChest().getItem(i);
            stacks.put(i, item == null ? null : item.clone());
        }
        for (int i = 0; i < 27; i++) saveEndSlot(id, i, stacks.get(i));
        addAccess(id);
    }

    private void saveEndSlot(long id, int slot, ItemStack i) {
        byte[] map = ItemParser.itemToSerial(i);
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE EnderChest SET data=? WHERE id=? and slot=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO EnderChest VALUES(?, ?, ?)")
        ) {
            s2.setBytes(1, map);
            s2.setLong(2, id);
            s2.setInt(3, slot);
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setInt(2, slot);
                s.setBytes(3, map);
                s.executeUpdate();
            }
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveEndSlot(id, slot, i), 5);
            e.printStackTrace();
        }
    }

    public PotionEffectEntity getPotion(UUID uuid) throws RefreshError {
        return getPotion(getKey(uuid));
    }

    public PotionEffectEntity getPotion(long id) throws RefreshError {
        long time = System.currentTimeMillis();
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement(
                        "SELECT * FROM PotionEffect WHERE id=" + id)
        ) {
            try (
                    ResultSet r = s.executeQuery()
            ) {
                HashSet<PotionEffect> set = new HashSet<>();
                while(r.next()) {
                    final PotionEffectType type;
                    if((type = PotionEffectType.getByName(r.getString("type"))) != null) {
                        final long l = r.getLong("time") - time;
                        if(l > 0)
                            set.add(new PotionEffect(type, Math.toIntExact(l / 50L), r.getInt("level"), true, r.getBoolean("particle")));
                    }
                }
                return set.isEmpty() ? new PotionEffectEntity() : new PotionEffectEntity(set);
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void savePotion(Player p) {
        final long id = getKey(p.getUniqueId());
        final long time = System.currentTimeMillis();
        clearEffect(id);
        for (PotionEffect effect : p.getActivePotionEffects()) saveEffect(id, effect, time);
        addAccess(id);
    }

    private void clearEffect(long id) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("DELETE FROM PotionEffect WHERE id=?");
        ) {
            s2.setLong(1, id);
            s2.executeUpdate();
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> clearEffect(id), 5);
            e.printStackTrace();
        }
    }

    private void saveEffect(long id, PotionEffect effect, long time) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s = c.prepareStatement("INSERT INTO PotionEffect VALUES(?, ?, ?, ?, ?)")
        ) {
            s.setLong(1, id);
            s.setString(2, effect.getType().getName());
            s.setInt(3, effect.getAmplifier());
            s.setLong(4, time + effect.getDuration() * 50L);
            s.setBoolean(5, effect.hasParticles());
            s.executeUpdate();
        } catch (SQLException e) {
            if(e.getErrorCode() == 5 || e.getErrorCode() == 6)
                Bukkit.getScheduler().runTaskLater(InventoryShare.plugin, () -> saveEffect(id, effect, time), 5);
            e.printStackTrace();
        }
    }
}
