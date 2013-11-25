package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class ArcherySkill extends AbstractSkill {
        private final EnumIntMap<EntityType> spMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        private int minKillDistance;
        private int normDistance;

        public ArcherySkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        /**
         * Make sure that a launched arrow remembers its
         * source location.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onProjectileLaunch(ProjectileLaunchEvent event) {
                final Entity entity = event.getEntity();
                if (entity.getType() != EntityType.ARROW) return;

                Util.storeSourceLocation(plugin, entity, entity.getLocation());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEntityDeath(EntityDeathEvent event) {
                final LivingEntity entity = event.getEntity();
                if (entity.getHealth() > 0.0) return;

                // Figure out damager.
                final EntityDamageEvent lastDamage = entity.getLastDamageCause();
                if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;
                if (lastDamage.getDamage() <= 0) return;
                final EntityDamageByEntityEvent lastEntityDamage = (EntityDamageByEntityEvent)lastDamage;
                final Entity damager = lastEntityDamage.getDamager();
                if (!(damager instanceof Arrow)) return;
                final Arrow arrow = (Arrow)damager;
                final LivingEntity shooter = arrow.getShooter();

                // Figure out player.
                if (!(shooter instanceof Player)) return;
                Player player = (Player)shooter;

                // Give SP.
                int skillPoints = spMap.get(entity.getType());
                if (skillPoints == 0) return;
                if (ExploitsPlugin.getKillDistance(player) < minKillDistance) return;
                final int maxHealth = (int)entity.getMaxHealth();
                final int playerDamage = Math.min(maxHealth, ExploitsPlugin.getPlayerDamage(entity));
                // Get SP from player damage to max health ratio.
                skillPoints = Util.rollFraction(skillPoints, playerDamage, maxHealth);
                // Multiply with source distance to norm distance ratio.
                final int distance = Math.min(128, Util.sourceDistance(plugin, arrow, entity.getLocation()));
                skillPoints = Util.rollFraction(skillPoints, distance, normDistance);
                // Apply SP.
                addSkillPoints(player, skillPoints);

                if (plugin.perksEnabled) {
                        // Give bonus XP.
                        final int xp = event.getDroppedExp();
                        event.setDroppedExp(multiplyXp(player, xp));

                        // Drop the head.
                        if (Util.random.nextInt(1000) < getSkullDropPermil(player)) {
                                ItemStack skull = Util.getMobHead(entity);
                                if (skull != null) {
                                        event.getDrops().add(skull);
                                }
                        }
                }
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);

                // Skull Drop
                final int skullPermil = getSkullDropPermil(player);
                if (skullPermil > 0) {
                        result.add("Sniped mobs drop their head " + Util.printPermilAsPercent(skullPermil) + "% of the time.");
                }

                // XP Bonus
                result.add("Shot mobs drop +" + (getXpMultiplier(player) - 100) + "% XP");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                minKillDistance = getConfig().getInt("MinKillDistance");
                normDistance = getConfig().getInt("NormDistance");
                spMap.load(getConfig().getConfigurationSection("sp"));
        }
}
