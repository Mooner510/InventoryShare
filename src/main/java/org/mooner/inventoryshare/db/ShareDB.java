package org.mooner.inventoryshare.db;

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
import java.util.Map;
import java.util.UUID;

public class ShareDB {
    public static ShareDB init;
    private final HashMap<UUID, Long> key;
    private static final String dbPath = "../db/";
    private static final String CONNECTION = "jdbc:sqlite:" + dbPath + "player.db";

    public ShareDB() {
        key = new HashMap<>();

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                            ItemParser.itemFromSerial(r.getString("helmet")),
                            ItemParser.itemFromSerial(r.getString("chestplate")),
                            ItemParser.itemFromSerial(r.getString("leggings")),
                            ItemParser.itemFromSerial(r.getString("boots")),
                            ItemParser.itemFromSerial(r.getString("shield"))
                    );
                return null;
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveArmor(Player p) {
        final long id = getKey(p.getUniqueId());
        final Map<String, Object> h = ItemParser.itemToSerial(p.getInventory().getHelmet());
        final Map<String, Object> cp = ItemParser.itemToSerial(p.getInventory().getChestplate());
        final Map<String, Object> leg = ItemParser.itemToSerial(p.getInventory().getLeggings());
        final Map<String, Object> b = ItemParser.itemToSerial(p.getInventory().getBoots());
        final Map<String, Object> off = ItemParser.itemToSerial(p.getInventory().getItemInOffHand());
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Armor SET helmet=?, chestplate=?, leggings=?, boots=?, shield=? WHERE id=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO Armor VALUES(?, ?, ?, ?, ?, ?)")
        ) {
            s2.setObject(1, h);
            s2.setObject(2, cp);
            s2.setObject(3, leg);
            s2.setObject(4, b);
            s2.setObject(5, off);
            s2.setLong(6, id);
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setObject(2, h);
                s.setObject(3, cp);
                s.setObject(4, leg);
                s.setObject(5, b);
                s.setObject(6, off);
                s.executeUpdate();
            }
            addAccess(id);
        } catch (SQLException e) {
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
        final long id = getKey(p.getUniqueId());
        final int level = p.getLevel();
        final float exp = p.getExp();
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
        final long id = getKey(p.getUniqueId());
        final double hp = p.getHealth();
        final double aHp = p.getExp();
        final int food = p.getFoodLevel();
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
                while(r.next()) map.put(r.getInt("slot"), ItemParser.itemFromSerial(r.getString("data")));
                return map.isEmpty() ? null : new InventoryEntity(map);
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void saveInventory(Player p) {
        final long id = getKey(p.getUniqueId());
        for (int i = 0; i < 36; i++) saveSlot(id, i, p.getInventory().getItem(i));
        addAccess(id);
    }

    private void saveSlot(long id, int slot, ItemStack i) {
        Map<String, Object> map = ItemParser.itemToSerial(i);
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE Inventory SET data=? WHERE id=? and slot=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO Inventory VALUES(?, ?, ?)")
        ) {
            s2.setObject(1, map);
            s2.setLong(2, id);
            s2.setInt(3, slot);
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setInt(2, slot);
                s.setObject(3, map);
                s.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public PotionEffectEntity getPotion(UUID uuid) throws RefreshError {
        return getPotion(getKey(uuid));
    }

    @Nullable
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
                return set.isEmpty() ? null : new PotionEffectEntity(set);
            }
        } catch (SQLException e) {
            throw new RefreshError(e);
        }
    }

    public void savePotion(Player p) {
        final long id = getKey(p.getUniqueId());
        final long time = System.currentTimeMillis();
        for (PotionEffect effect : p.getActivePotionEffects()) saveEffect(id, effect, time);
        addAccess(id);
    }

    private void saveEffect(long id, PotionEffect effect, long time) {
        try (
                Connection c = DriverManager.getConnection(CONNECTION);
                PreparedStatement s2 = c.prepareStatement("UPDATE PotionEffect SET level=?, time=?, particle=? WHERE id=? and type=?");
                PreparedStatement s = c.prepareStatement("INSERT INTO PotionEffect VALUES(?, ?, ?, ?, ?)")
        ) {
            s2.setInt(1, effect.getAmplifier());
            s2.setLong(2, time + effect.getDuration() * 50L);
            s2.setBoolean(3, effect.hasParticles());
            s2.setLong(4, id);
            s2.setString(5, effect.getType().getName());
            if(s2.executeUpdate() == 0) {
                s.setLong(1, id);
                s.setString(2, effect.getType().getName());
                s.setInt(3, effect.getAmplifier());
                s.setLong(4, time + effect.getDuration() * 50L);
                s.setBoolean(5, effect.hasParticles());
                s.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
