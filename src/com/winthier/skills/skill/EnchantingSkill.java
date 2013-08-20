package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

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
                if (level < 100) return 30;
                return Math.min(50, 31 + (level - 100) / 20);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEnchantItem(EnchantItemEvent event) {
                final Player player = event.getEnchanter();
                final int xpLevels = event.getExpLevelCost();
                final ItemStack item = event.getItem();

                // Give SP.
                final int sp = itemSpMap.roll(item.getType(), xpLevels);
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
