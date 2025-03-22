package org.craftarix.monitoring.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class ItemUtil {
    public ItemBuilder newBuilder(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public ItemBuilder newBuilder(Material material) {
        return new ItemBuilder(material);
    }

    public ItemStack replace(ItemStack itemStack, String oldString, String newString) {
        var builder = newBuilder(itemStack);
        builder.replace(oldString, newString);
        return builder.build();
    }

    public ItemStack loadItemFromConfig(ConfigurationSection section) {
        ItemBuilder itemBuilder;
        if (section == null) {
            return new ItemStack(Material.BARRIER);
        }
        var material = getMaterialByString(section.getString("material"));
        if (material == null) {
            return new ItemStack(Material.BARRIER);
        }

        itemBuilder = newBuilder(material);
        itemBuilder.setAmount(section.getInt("amount", 1));
        itemBuilder.setGlowing(section.getBoolean("glowing", false));

        var name = section.getString("name");
        if (name != null) {
            itemBuilder.setName(name);
        }

        var lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            itemBuilder.setLore(lore);
        }

        var texture = section.getString("texture");
        if (texture != null) {
            itemBuilder.setTextureValue(texture);
            itemBuilder.addItemFlag(ItemFlag.HIDE_PLACED_ON);
        }

        var enchants = section.getStringList("enchants");
        for (var enchant : enchants) {
            try {
                var enchantment = Enchantment.getByName(enchant.split(":")[0]);
                var level = Integer.parseInt(enchant.split(":")[1]);
                itemBuilder.addEnchantment(enchantment, level);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        for (String minecraftEffect : section.getStringList("effects")) {
            Bukkit.getLogger().info(minecraftEffect);
            PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(minecraftEffect.split(":")[0]),
                    Integer.parseInt(minecraftEffect.split(":")[1]),
                    Integer.parseInt(minecraftEffect.split(":")[2]) - 1);
            Bukkit.getLogger().info(material.name() + ": " + potionEffect.getType().toString());
            if (material.name().contains("POTION") || material.name().contains("ARROW")) {
                itemBuilder.addCustomPotionEffect(potionEffect, true);
            }
        }

        var flags = section.getStringList("flags");
        for (String flagName : flags) {
            try {
                val itemFlag = ItemFlag.valueOf(flagName);
                itemBuilder.addItemFlag(itemFlag);
            } catch (IllegalArgumentException e) {
                continue;
            }
        }


        if (material.name().contains("POTION") || material.name().contains("ARROW")
                || material.name().contains("LEATHER_") && material != Material.LEATHER_HORSE_ARMOR) {

            if (section.getInt("color.r", -1) >= 0) {
                Color color = Color.fromRGB(section.getInt("color.r"), section.getInt("color.g"), section.getInt("color.b"));
                if (material.name().contains("LEATHER_")) {
                    itemBuilder.setLeatherColor(color);
                } else {
                    itemBuilder.setPotionColor(color);
                }
            }

        }

        return itemBuilder.build();
    }

    public Material getMaterialByString(String materialName) {
        if (materialName == null) {
            return null;
        }
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException exception) {
            try {
                return Material.valueOf("LEGACY_" + materialName.toUpperCase());
            } catch (IllegalArgumentException exception2) {
                return null;
            }
        }
    }

    public class ItemBuilder {
        private final ItemStack itemStack;

        public ItemBuilder(Material material) {
            itemStack = new ItemStack(material);
        }

        public ItemBuilder(ItemStack itemStack) {
            this.itemStack = itemStack.clone();
        }

        public ItemBuilder setName(String name) {
            var itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name.replace("&", "§"));
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder replace(String oldString, String newString) {
            var itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace(oldString, newString));
            var oldLore = itemMeta.getLore();
            if (oldLore != null) {
                var newLore = oldLore.stream()
                        .map(line -> line.replace(oldString, newString))
                        .collect(Collectors.toList());
                itemMeta.setLore(newLore);
            }

            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder addLore(String line) {
            var itemMeta = itemStack.getItemMeta();
            var lore = itemMeta.getLore() == null ? new ArrayList<String>() : itemMeta.getLore();
            lore.add(line);
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder setLore(List<String> lore) {
            var itemMeta = itemStack.getItemMeta();
            lore = lore.stream().map(line -> line.replace("&", "§")).collect(Collectors.toList());
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder setAmount(int amount) {
            itemStack.setAmount(amount);
            return this;
        }

        public ItemBuilder setGlowing(boolean glowing) {
            if (!glowing) return this;
            var itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 0, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder addItemFlag(ItemFlag itemFlag) {
            var itemMeta = itemStack.getItemMeta();
            itemMeta.addItemFlags(itemFlag);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
            var itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(enchantment, level, true);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public ItemBuilder setTextureValue(String texture) {
            if (texture == null) {
                return this;
            }

            if (!itemStack.getType().equals(Material.PLAYER_HEAD)) {
                return this;
            }

            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "ItzStonlex");
            gameProfile.getProperties().put("textures", new Property("textures", texture));
            var profile = ReflectionUtil.getResolvableProfile(gameProfile);

            ReflectionUtil.setField(skullMeta, "profile", profile);

            itemStack.setItemMeta(skullMeta);
            return this;
        }

        public ItemBuilder setLeatherColor(Color color) {
            if (color == null) {
                return this;
            }

            var leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
            leatherArmorMeta.setColor(color);

            itemStack.setItemMeta(leatherArmorMeta);
            return this;
        }

        public ItemBuilder setPotionColor(Color color) {
            if (color == null) {
                return this;
            }

            var potionMeta = (PotionMeta) itemStack.getItemMeta();
            potionMeta.setColor(color);

            itemStack.setItemMeta(potionMeta);
            return this;
        }

        public ItemBuilder addCustomPotionEffect(PotionEffect potionEffect, boolean isAdd) {
            if (potionEffect == null) {
                return this;
            }

            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            potionMeta.addCustomEffect(potionEffect, isAdd);

            itemStack.setItemMeta(potionMeta);
            return this;
        }

        public ItemStack build() {
            return itemStack;
        }
    }
}
