package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class EatingSkill extends AbstractSkill {
        private MaterialIntMap foodMap = new MaterialIntMap(0);
        private MaterialIntMap spMap = new MaterialIntMap(0);
        private int levelsPerHealthPoint;

        public EatingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        public void onPlayerEat(final Player player, final Material mat) {
                final int foodPoints = foodMap.get(mat);
                final int skillPoints = spMap.get(mat);
                if (skillPoints > 0 && foodPoints > 0) {
                        final int hunger = 20 - player.getFoodLevel();
                        final int food = Math.min(hunger, foodPoints);

                        // Give SP.
                        final int sp = Util.rollFraction(skillPoints, food, foodPoints);
                        addSkillPoints(player, sp);

                        // Give bonus health and XP.
                        if (plugin.perksEnabled) {
                                final int healthBoost = Util.rollFraction(food, getHealthBoost(player), 100);
                                if (healthBoost > 0) {
                                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthBoost));
                                }
                                // Bonus XP.
                                final int xp = multiplyXp(player, Util.rollFraction(1, sp, skillPoints));
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
        }
}
