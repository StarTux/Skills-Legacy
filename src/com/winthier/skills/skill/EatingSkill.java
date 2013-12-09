package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class EatingSkill extends AbstractSkill {
        private MaterialIntMap foodMap = new MaterialIntMap(0);
        private Map<Material, Float> saturationMap = new EnumMap<Material, Float>(Material.class);
        private MaterialIntMap spMap = new MaterialIntMap(0);
        private int levelsPerHealthPoint;

        public EatingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        public void onPlayerEat(final Player player, final Material mat) {
                final int skillPoints = spMap.get(mat);
                final int foodPoints = foodMap.get(mat);
                if (skillPoints > 0 && foodPoints > 0) {
                        final int foodLevel = player.getFoodLevel();
                        if (foodLevel >= 20) return;
                        final int hunger = 20 - foodLevel;
                        final int food = Math.min(hunger, foodPoints);
                        final int newFoodLevel = Math.min(20, foodLevel + foodPoints);

                        final float saturationPoints = saturationMap.get(mat);
                        final float saturationLevel = player.getSaturation();
                        final float saturationHunger = Math.max(0.0f, (float)newFoodLevel - saturationLevel);
                        final float saturationFood = Math.min(saturationHunger, saturationPoints);
                        final float saturationPct = saturationFood / saturationPoints;
                        final int satFood = (int)(saturationFood * 10.0);
                        final int satPct = (int)(saturationPct * 10.0);

                        // Give SP.
                        final int foodSP = Util.rollFraction(food, food, foodPoints);
                        final int satSP = Util.rollFraction(satFood, satPct, 200);
                        final int sp = foodSP + satSP;
                        addSkillPoints(player, sp);

                        // Give bonus health and XP.
                        if (plugin.perksEnabled) {
                                final int healthBoost = Util.rollFraction(food, getHealthBoost(player), 100);
                                if (healthBoost > 0) {
                                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthBoost));
                                }
                                // Bonus XP.
                                final int xp = multiplyXp(player, foodSP);
                                if (xp > 0) player.giveExp(xp);
                        }
                }
        }

        public int getHealthBoost(Player player) {
                return Math.min(100, getSkillLevel(player) / 9);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
                final Player player = event.getPlayer();
                final Material mat = event.getItem().getType();
                onPlayerEat(player, mat);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                final Player player = event.getPlayer();
                if (event.getClickedBlock().getType() != Material.CAKE_BLOCK) return;
                onPlayerEat(player, Material.CAKE);
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Healthy food grants you " + getHealthBoost(player) + "% health boost");
                result.add("Exquisite food gives +" + (getXpMultiplier(player) - 100) + "% XP");
                return result;
        }

        // Configuration

        @Override
        public void loadConfiguration() {
                foodMap.load(getConfig().getConfigurationSection("food"));
                spMap.load(getConfig().getConfigurationSection("sp"));
                levelsPerHealthPoint = getConfig().getInt("LevelsPerHealthPoint");

                for (Material mat : Material.values()) saturationMap.put(mat, 0.0f);
                ConfigurationSection saturation = getConfig().getConfigurationSection("saturation");
                for (String key : saturation.getKeys(false)) {
                        Material mat = Util.enumFromString(Material.class, key);
                        if (mat == null) {
                                plugin.getLogger().warning("[Eating] Invalid material: " + key);
                                continue;
                        }
                        float val = (float)saturation.getDouble(key);
                        saturationMap.put(mat, val);
                }
        }
}
