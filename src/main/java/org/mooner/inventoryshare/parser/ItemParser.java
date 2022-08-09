package org.mooner.inventoryshare.parser;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.mooner.inventoryshare.InventoryShare;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ItemParser {
    public static Map<String, Object> itemToSerial(ItemStack item) {
        if(item == null || item.getType() == Material.AIR) return null;
        return item.serialize();
    }

    private static Object transfer(String s) {
        Object o;
        try { o = Integer.parseInt(s); return o; } catch (Exception ignore) {}
        try { o = Double.parseDouble(s); return o; } catch (Exception ignore) {}
        return s;
    }

    public static ItemStack itemFromSerial(String data) {
        if(data == null) return null;
        Map<String, Object> map = new HashMap<>();
        final String[] a = data.substring(1, data.length() - 1).split(", ");
        for (String s : a) {
            final String[] b = s.split("=");
            map.put(b[0], transfer(b[1]));
        }
        return ItemStack.deserialize(map);
    }

    public static ItemStack itemFromSerial(Map<String, Object> data) {
        if(data == null) return null;
        return ItemStack.deserialize(data);
    }

    public static String itemToBase64(ItemStack item) {
        if(item == null || item.getType() == Material.AIR) return null;
        try(
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)
        ) {
            dataOutput.writeObject(item);
            // Serialize that array
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            InventoryShare.plugin.getLogger().warning("Unable to save item stacks. "+ e);
        }
        return null;
    }

    public static ItemStack itemFromBase64(String data) {
        if(data == null) return null;
        try(
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                ) {
            // Read the serialized inventory
            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            InventoryShare.plugin.getLogger().warning("Unable to decode class type. "+ e);
        }
        return null;
    }
}
