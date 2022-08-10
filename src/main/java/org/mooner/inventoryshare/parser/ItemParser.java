package org.mooner.inventoryshare.parser;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.mooner.inventoryshare.InventoryShare;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class ItemParser {
    public static byte[] itemToSerial(ItemStack item) {
        if(item == null || item.getType() == Material.AIR) return null;
        return serialToString(item.serialize());
    }

    public static ItemStack itemFromSerial(byte[] data) {
        if(data == null) return null;
        return ItemStack.deserialize(stringToSerial(data));
    }

    public static byte[] serialToString(Map<String, Object> data) {
        if(data == null) return null;
        try (
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BukkitObjectOutputStream os = new BukkitObjectOutputStream(stream);
                ) {
            os.writeObject(data);
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> stringToSerial(byte[] data) {
        if(data == null) return null;
        try (
                ByteArrayInputStream stream = new ByteArrayInputStream(data);
                BukkitObjectInputStream os = new BukkitObjectInputStream(stream);
        ) {
            return (Map<String, Object>) os.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object transfer(String s) {
        Object o;
        try { o = Integer.parseInt(s); return o; } catch (Exception ignore) {}
        try { o = Double.parseDouble(s); return o; } catch (Exception ignore) {}
        return s;
    }

//    public static ItemStack itemFromSerial(String data) {
//        if(data == null) return null;
//        Map<String, Object> map = new HashMap<>();
//        final String[] a = data.substring(1, data.length() - 1).split(", ");
//        for (String s : a) {
//            final String[] b = s.split("=");
//            map.put(b[0], transfer(b[1]));
//        }
//        return ItemStack.deserialize(map);
//    }

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
