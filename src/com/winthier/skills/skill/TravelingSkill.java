package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.TravelingPlayerInfo;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TravelingSkill extends AbstractSkill {
        private int normDistance, normDistanceSquared, pearlingNormDistance;
        private int farTravelDistance, farTravelDistanceSquared;
        private int walkingSkillPoints, pearlingSkillPoints, ridingSkillPoints;

        public TravelingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerMove(PlayerMoveEvent event) {
                final Player player = event.getPlayer();
                final Location to = event.getTo();
                final TravelingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travelingInfo;
                int dist = info.distanceSquared(to);
                if (dist == -1) {
                        info.setLocation(event.getFrom());
                        return;
                }
                if (dist >= normDistanceSquared) {
                        info.setLocation(to);
                        final Entity vehicle = player.getVehicle();
                        boolean riding = false;
                        if (vehicle != null) {
                                if (vehicle.getVehicle() != null) {
                                        info.setLocation(to);
                                        info.setFarTravelLocation(to);
                                        return;
                                }
                                switch (vehicle.getType()) {
                                case HORSE:
                                case PIG:
                                        riding = true;
                                        break;
                                default:
                                        info.setLocation(to);
                                        info.setFarTravelLocation(to);
                                        return;
                                }
                        }

                        // Give SP.
                        int skillPoints = 0;
                        if (riding) {
                                skillPoints = ridingSkillPoints;
                        } else {
                                skillPoints = walkingSkillPoints;
                        }
                        addSkillPoints(player, skillPoints);

                        // Give bonus XP.
                        if (plugin.perksEnabled && info.farTravelDistanceSquared(to) >= farTravelDistanceSquared) {
                                info.setFarTravelLocation(to);
                                player.giveExp(getBonusXp(player));
                        }
                }
        }

        /**
         * Teleportation should not count towards the travling
         * skill. Make sure that all the cached values are set
         * accordingly.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
                final Player player = event.getPlayer();
                final TravelingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travelingInfo;

                final Location to = event.getTo();
                info.setLocation(to);
                info.setFarTravelLocation(to);
        }

        // /**
        //  * Make sure that a launched ender pearl remembers its
        //  * source location.
        //  */
        // @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        // public void onProjectileLaunch(ProjectileLaunchEvent event) {
        //         final Entity entity = event.getEntity();
        //         if (entity.getType() != EntityType.ENDER_PEARL) return;

        //         Util.storeSourceLocation(plugin, entity, entity.getLocation());
        // }

        // @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        // public void onProjectileHit(ProjectileHitEvent event) {
        //         final Projectile projectile = event.getEntity();
        //         if (projectile.getType() != EntityType.ENDER_PEARL) return;

        //         final LivingEntity shooter = projectile.getShooter();
        //         if (!(shooter instanceof Player)) return;
        //         final Player player = (Player)shooter;

        //         if (!player.isValid()) return;
        //         if (player.isFlying()) return;
        //         if (player.isSleeping()) return;
        //         if (player.isDead()) return;
        //         if (player.isInsideVehicle()) return;

        //         final int distance = Math.min(128, Util.horizontalSourceDistance(plugin, projectile, player.getLocation()));
        //         if (distance <= 0) return;

        //         // Give SP.
        //         int skillPoints = Util.rollFraction(pearlingSkillPoints, distance, pearlingNormDistance);
        //         addSkillPoints(player, skillPoints);
        // }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerRespawn(PlayerRespawnEvent event) {
                final Player player = event.getPlayer();
                final TravelingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travelingInfo;
                final Location to = event.getRespawnLocation();
                info.setLocation(to);
                info.setFarTravelLocation(to);
        }

        protected int getBonusXp(Player player) {
                return getSkillLevel(player) / 16;
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Traveling far distances earns you " + getBonusXp(player) + " XP");
                return result;
        }

        // Configuration

        @Override
        public void loadConfiguration() {
                normDistance = getConfig().getInt("NormDistance");
                normDistanceSquared = normDistance * normDistance;

                walkingSkillPoints = getConfig().getInt("WalkingSkillPoints");
                ridingSkillPoints = getConfig().getInt("RidingSkillPoints");

                pearlingNormDistance = getConfig().getInt("PearlingNormDistance");
                pearlingSkillPoints = getConfig().getInt("PearlingSkillPoints");

                farTravelDistance = getConfig().getInt("FarTravelDistance");
                farTravelDistanceSquared = farTravelDistance * farTravelDistance;
        }
}
