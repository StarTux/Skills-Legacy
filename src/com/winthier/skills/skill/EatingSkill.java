package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
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
                        // give sp
                        addSkillPoints(player, Util.rollFraction(skillPoints, food, foodPoints));
                        // give health bonus
                        final int health = Math.min(getSkillLevel(player) / levelsPerHealthPoint, food);
                        if (health > 0) {
                                player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + health));
                        }
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                final Material mat = event.getItem().getType();
                onPlayerEat(player, mat);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                if (event.getClickedBlock().getType() != Material.CAKE_BLOCK) return;
                onPlayerEat(player, Material.CAKE);
        }

        @Override
        public void loadConfiguration() {
                foodMap.load(getConfig().getConfigurationSection("food"));
                spMap.load(getConfig().getConfigurationSection("sp"));
                levelsPerHealthPoint = getConfig().getInt("LevelsPerHealthPoint");
        }
}
