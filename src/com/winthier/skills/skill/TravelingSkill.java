package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.TravelingPlayerInfo;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TravelingSkill extends AbstractSkill {
        private int normDistance, normDistanceSquared, minPearlingDistance, minPearlingDistanceSquared;
        private int farTravelDistance, farTravelDistanceSquared;
        private int walkingSkillPoints, sprintingSkillPoints, pearlingSkillPoints, ridingSkillPoints;

        public TravelingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerMove(PlayerMoveEvent event) {
                final Player player = event.getPlayer();
                final Location to = event.getTo();
                final TravelingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travelingPlayerInfo;
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
                        int skillPoints = 0;
                        if (riding) {
                                skillPoints = ridingSkillPoints;
                        } else if (player.isSprinting()) {
                                skillPoints = sprintingSkillPoints;
                        } else {
                                skillPoints = walkingSkillPoints;
                        }
                        addSkillPoints(player, skillPoints);
                        // give bonus xp
                        if (info.farTravelDistanceSquared(to) >= farTravelDistanceSquared) {
                                info.setFarTravelLocation(to);
                                player.giveExp(getBonusXp(player));
                        }
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
                final Player player = event.getPlayer();
                final TravelingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travelingPlayerInfo;
                final Location to = event.getTo();
                final Location from = event.getFrom();
                info.setLocation(to);
                info.setFarTravelLocation(to);
                if (event.getCause() == TeleportCause.ENDER_PEARL) {
                        int dist = Util.horizontalDistanceSquared(from, to);
                        if (dist > minPearlingDistanceSquared) {
                                final int skillPoints = Util.rollFraction(pearlingSkillPoints, Util.sqrt(dist), normDistance);
                                addSkillPoints(player, skillPoints);
                        }
                }
        }

        protected int getBonusXp(Player player) {
                return getSkillLevel(player) / 10;
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Traveling far distances earns you " + getBonusXp(player) + " xp");
                return result;
        }

        // Configuration

        @Override
        public void loadConfiguration() {
                normDistance = getConfig().getInt("NormDistance");
                normDistanceSquared = normDistance * normDistance;
                minPearlingDistance = getConfig().getInt("MinPearlingDistance");
                minPearlingDistanceSquared = minPearlingDistance * minPearlingDistance;
                farTravelDistance = getConfig().getInt("FarTravelDistance");
                farTravelDistanceSquared = farTravelDistance * farTravelDistance;
                walkingSkillPoints = getConfig().getInt("WalkingSkillPoints");
                sprintingSkillPoints = getConfig().getInt("SprintingSkillPoints");
                pearlingSkillPoints = getConfig().getInt("PearlingSkillPoints");
                ridingSkillPoints = getConfig().getInt("RidingSkillPoints");
        }
}
