package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.util.Util;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Miscellaneous totem related utility functions.
 */
public class Totem {
        public static final String TOTEM_LORE_MAGIC = "" + ChatColor.BLACK + ChatColor.MAGIC + "Totem";
        private final static ItemStack totems[] = new ItemStack[4];

        public static ElementType getTotemType(ItemStack item) {
                switch (item.getType()) {
                case DIAMOND: return ElementType.EARTH;
                case FIRE:    return ElementType.FIRE;
                case FEATHER:  return ElementType.AIR;
                case WATER:   return ElementType.WATER;
                default: return null;
                }
        }

        public static Material getTotemMaterial(ElementType element) {
                switch (element) {
                case EARTH: return Material.DIAMOND;
                case FIRE:  return Material.FIRE;
                case AIR:   return Material.FEATHER;
                case WATER: return Material.WATER;
                default: return null;
                }
        }

        public static boolean isTotem(ItemStack item) {
                if (getTotemType(item) == null) return false;
                if (!item.hasItemMeta()) return false;
                final ItemMeta meta = item.getItemMeta();
                if (!meta.hasLore()) return false;
                final List<String> lore = meta.getLore();
                if (lore.size() < 1) return false;
                return lore.get(0).equals(TOTEM_LORE_MAGIC);
        }

        public static ItemStack createTotem(ElementType element) {
                ItemStack item = new ItemStack(getTotemMaterial(element));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("" + element.getColor() + element.getDisplayName());
                meta.setLore(Arrays.asList(TOTEM_LORE_MAGIC));
                item.setItemMeta(meta);
                item = Util.addGlow(item);
                return item;
        }
}
