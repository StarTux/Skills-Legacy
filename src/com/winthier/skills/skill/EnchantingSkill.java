package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class EnchantingSkill extends AbstractSkill {
        private MaterialFractionMap itemSpMap = new MaterialFractionMap(0);

        public EnchantingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        /**
         * Find out what xp level a player can enchant on an
         * enchanting table.
         */
        public int getMaxEnchantingLevel(Player player) {
                int level = getSkillLevel(player);
                return Math.min(50, 30 + level / 20);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEnchantItem(EnchantItemEvent event) {
                final Player player = event.getEnchanter();
                final int xpLevels = event.getExpLevelCost();
                final ItemStack item = event.getItem();
                onEnchant(player, item.getType(), xpLevels);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onInventoryClick(InventoryClickEvent event) {
                if (event.getInventory().getType() != InventoryType.ANVIL) return;
                if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
                switch (event.getAction()) {
                case DROP_ALL_SLOT:
                case DROP_ONE_SLOT:
                case HOTBAR_MOVE_AND_READD:
                case HOTBAR_SWAP:
                case MOVE_TO_OTHER_INVENTORY:
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                        break;
                default:
                        return;
                }
                final Inventory inv = event.getInventory();
                if (inv.getItem(0) == null || inv.getItem(0).getType() == Material.AIR ||
                    inv.getItem(1) == null || inv.getItem(1).getType() == Material.AIR ||
                    inv.getItem(2) == null || inv.getItem(2).getType() == Material.AIR) {
                        return;
                }
                final Material mat = inv.getItem(2).getType();
                final Player player = (Player)event.getWhoClicked();
                final int oldLevel = player.getLevel();

                new BukkitRunnable() {
                        public void run() {
                                final int xpLevel = Math.min(39, oldLevel - player.getLevel());
                                if (xpLevel > 0) onEnchant(player, mat, xpLevel);
                        }
                }.runTask(plugin);
        }

        public void onEnchant(Player player, Material mat, int xpLevels) {
                // Give SP.
                final int sp = itemSpMap.roll(mat, xpLevels);
                addSkillPoints(player, sp);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
        public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
                if (!plugin.perksEnabled) return;

                // Perk: Give higher XP levels on the enchanting table.
                final Player player = event.getEnchanter();
                final int books = event.getEnchantmentBonus();
                final int[] levels = event.getExpLevelCostsOffered();
                if (levels.length < 3 || levels[2] < 30) return;
                int maxLevel = books * 2;
                final int level = Math.min(maxLevel, getMaxEnchantingLevel(player));
                levels[2] = level;
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("You can reach level " + getMaxEnchantingLevel(player) + " on the enchanting table.");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                itemSpMap.load(getConfig().getConfigurationSection("sp"));
        }
}
