package de.ld338.mysqlsync.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Base64Util {
    public static String encodeInventoryToBase64(Player player) throws Exception {
        PlayerInventory inventory = player.getInventory();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(inventory.getSize());

            for (ItemStack item : inventory.getContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static void decodeInventoryFromBase64(Player player, String base64) throws Exception {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            PlayerInventory inventory = player.getInventory();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            inventory.setContents(items);
        }
    }

    public static String encodeAchievementsToBase64(Player player) {
        Map<String, Boolean> achievements = new HashMap<>();

        Iterator<Advancement> advancements = Bukkit.advancementIterator();
        while (advancements.hasNext()) {
            Advancement advancement = advancements.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);

            achievements.put(advancement.getKey().getKey(), progress.isDone());
        }

        String jsonAchievements = new Gson().toJson(achievements);
        return java.util.Base64.getEncoder().encodeToString(jsonAchievements.getBytes());
    }

    public static void decodeAchievementsFromBase64(Player player, String base64) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        String jsonAchievements = new String(decodedBytes);

        Map<String, Boolean> achievements = new Gson().fromJson(jsonAchievements, HashMap.class);

        for (Map.Entry<String, Boolean> entry : achievements.entrySet()) {
            if (entry.getValue()) {
                Advancement advancement = Bukkit.getAdvancement(new NamespacedKey("minecraft", entry.getKey()));
                if (advancement != null) {
                    AdvancementProgress progress = player.getAdvancementProgress(advancement);
                    for (String criteria : progress.getRemainingCriteria()) {
                        progress.awardCriteria(criteria);
                    }
                }
            }
        }
    }

    public static void decodeEnderChestFromBase64(Player player, String base64) throws Exception {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            Inventory enderChest = player.getEnderChest();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            enderChest.setContents(items);
        }
    }

    public static String encodeEnderChestToBase64(Player player) throws Exception {
        Inventory enderChest = player.getEnderChest();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(enderChest.getSize());

            for (ItemStack item : enderChest.getContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
    }

    public static void decodeArmorFromBase64(Player player, String inv) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(inv);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            PlayerInventory inventory = player.getInventory();
            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            inventory.setArmorContents(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encodeArmorToBase64(Player player) {
        PlayerInventory inventory = player.getInventory();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(inventory.getArmorContents().length);

            for (ItemStack item : inventory.getArmorContents()) {
                dataOutput.writeObject(item);
            }

            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encodeStatsToBase64(Player player) {
        Map<String, Object> stats = new HashMap<>();

        for (Statistic statistic : Statistic.values()) {
            if (statistic.isSubstatistic()) {
                Map<String, Integer> paramStats = new HashMap<>();
                if (statistic.isBlock()) {
                    for (Material material : Material.values()) {
                        if (material.isBlock()) {
                            int value = player.getStatistic(statistic, material);
                            paramStats.put(material.name(), value);
                        }
                    }
                } else if (statistic.getType() == Statistic.Type.ITEM) {
                    for (Material material : Material.values()) {
                        if (material.isItem()) {
                            int value = player.getStatistic(statistic, material);
                            paramStats.put(material.name(), value);
                        }
                    }
                } else if (statistic.getType() == Statistic.Type.ENTITY) {
                    for (EntityType entityType : EntityType.values()) {
                        if (entityType.isSpawnable()) {
                            int value = player.getStatistic(statistic, entityType);
                            paramStats.put(entityType.name(), value);
                        }
                    }
                }
                if (!paramStats.isEmpty()) {
                    stats.put(statistic.name(), paramStats);
                }
            } else {
                stats.put(statistic.name(), player.getStatistic(statistic));
            }
        }

        String jsonStats = new Gson().toJson(stats);
        return java.util.Base64.getEncoder().encodeToString(jsonStats.getBytes());
    }

    public static void decodeStatsFromBase64(Player player, String base64) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        String jsonStats = new String(decodedBytes);

        Map<String, Object> stats = new Gson().fromJson(jsonStats, HashMap.class);

        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            try {
                Statistic statistic = Statistic.valueOf(entry.getKey());

                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> paramStats = (Map<String, Object>) entry.getValue();

                    if (statistic.isBlock()) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            Material material = Material.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, material, value);
                        }
                    } else if (statistic.getType() == Statistic.Type.ITEM) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            Material material = Material.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, material, value);
                        }
                    } else if (statistic.getType() == Statistic.Type.ENTITY) {
                        for (Map.Entry<String, Object> paramEntry : paramStats.entrySet()) {
                            EntityType entityType = EntityType.valueOf(paramEntry.getKey());
                            int value = ((Double) paramEntry.getValue()).intValue();
                            player.setStatistic(statistic, entityType, value);
                        }
                    }
                } else if (entry.getValue() instanceof Double) {
                    player.setStatistic(statistic, ((Double) entry.getValue()).intValue());
                }
            } catch (IllegalArgumentException | ClassCastException e) {
                System.err.println("Error processing statistic: " + entry.getKey() + " - " + e.getMessage());
            }
        }
    }

    public static String encodeEffectsToBase64(Player player) {
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        Map<String, Integer[]> effectMap = new HashMap<>();

        for (PotionEffect effect : effects) {
            effectMap.put(effect.getType().getName(), new Integer[]{effect.getDuration(), effect.getAmplifier()});
        }

        String jsonEffects = new Gson().toJson(effectMap);
        return java.util.Base64.getEncoder().encodeToString(jsonEffects.getBytes());
    }

    public static void decodeEffectsFromBase64(Player player, String base64) {
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64);
        String jsonEffects = new String(decodedBytes);

        Type mapType = new TypeToken<Map<String, Integer[]>>() {}.getType();
        Map<String, Integer[]> effectMap = new Gson().fromJson(jsonEffects, mapType);

        for (Map.Entry<String, Integer[]> entry : effectMap.entrySet()) {
            PotionEffectType effectType = PotionEffectType.getByName(entry.getKey());
            if (effectType != null) {
                int duration = entry.getValue()[0];
                int amplifier = entry.getValue()[1];
                player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
            }
        }
    }
}
