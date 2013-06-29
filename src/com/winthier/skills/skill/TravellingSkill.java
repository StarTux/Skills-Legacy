package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.TravellingPlayerInfo;
import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TravellingSkill extends AbstractSkill {
        private int normDistance, normDistanceSquared, minPearlingDistance, minPearlingDistanceSquared;
        private int walkingSkillPoints, sprintingSkillPoints, pearlingSkillPoints;

        public TravellingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerMove(PlayerMoveEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                final Location to = event.getTo();
                final TravellingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travellingPlayerInfo;
                int dist = info.distanceSquared(to);
                if (dist == -1) {
                        info.setLocation(event.getFrom());
                        return;
                }
                if (dist > normDistanceSquared) {
                        info.setLocation(to);
                        if (player.getVehicle() != null) return;
                        final int skillPoints = player.isSprinting() ? sprintingSkillPoints : walkingSkillPoints;
                        addSkillPoints(player, skillPoints);
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerTeleport(PlayerTeleportEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                final TravellingPlayerInfo info = plugin.playerManager.getPlayerInfo(player).travellingPlayerInfo;
                final Location to = event.getTo();
                final Location from = event.getFrom();
                info.setLocation(to);
                if (event.getCause() == TeleportCause.ENDER_PEARL) {
                        int dist = Util.horizontalDistanceSquared(from, to);
                        if (dist > minPearlingDistanceSquared) {
                                final int skillPoints = Util.rollFraction(pearlingSkillPoints, Util.sqrt(dist), normDistance);
                                addSkillPoints(player, skillPoints);
                        }
                }
        }

        @Override
        public void loadConfiguration() {
                normDistance = getConfig().getInt("NormDistance");
                normDistanceSquared = normDistance * normDistance;
                minPearlingDistance = getConfig().getInt("MinPearlingDistance");
                minPearlingDistanceSquared = minPearlingDistance * minPearlingDistance;
                walkingSkillPoints = getConfig().getInt("WalkingSkillPoints");
                sprintingSkillPoints = getConfig().getInt("SprintingSkillPoints");
                pearlingSkillPoints = getConfig().getInt("PearlingSkillPoints");
        }
}
