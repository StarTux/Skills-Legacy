package com.winthier.skills.player;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerInfo {
        private final SkillsPlugin plugin;
        private Player player;
        private final Map<SkillType, PlayerSkillInfo> skillInfo = new EnumMap<SkillType, PlayerSkillInfo>(SkillType.class);
        private BukkitRunnable removalTask = null;;
        public final TravelingPlayerInfo travelingPlayerInfo = new TravelingPlayerInfo(this);

        public PlayerInfo(SkillsPlugin plugin, Player player) {
                this.plugin = plugin;
                this.player = player;
                for (SkillType skillType : SkillType.values()) {
                        skillInfo.put(skillType, new PlayerSkillInfo());
                }
        }

        /**
         * Add skill points without any hooks or checks.  This is
         * intended to be called by the routine which loads data
         * from the database.
         */
        public void setSkillPointsBare(SkillType skillType, int skillPoints) {
                skillInfo.get(skillType).skillPoints = skillPoints;
                flushCache(skillType);
        }

        public String getName() {
                return player.getName();
        }

        public Player getPlayer() {
                return player;
        }

        public int getSkillPoints(SkillType skillType) {
                return skillInfo.get(skillType).skillPoints;
        }

        public int getElementalLevel(ElementType elem) {
                int sum = 0;
                SkillType skillTypes[] = elem.getSkills();
                sum += getSkillPoints(skillTypes[0]) * 2;
                sum += getSkillPoints(skillTypes[1]);
                sum += getSkillPoints(skillTypes[2]);
                sum /= 2;
                return AbstractSkill.getLevelForSkillPoints(sum);
        }

        public void setSkillPoints(SkillType skillType, int skillPoints) {
                skillInfo.get(skillType).skillPoints = skillPoints;
                flushCache(skillType);
                plugin.sqlManager.setSkillPoints(player.getName(), skillType, skillPoints);
        }

        public void addSkillPoints(SkillType skillType, int skillPoints) {
                final PlayerSkillInfo info = skillInfo.get(skillType);
                info.skillPoints += skillPoints;
                plugin.sqlManager.addSkillPoints(player.getName(), skillType, skillPoints);
                if (info.skillPoints >= info.requiredSkillPoints) {
                        int oldLevel = info.skillLevel;
                        flushCache(skillType);
                        int newLevel = info.skillLevel;
                        if (newLevel > oldLevel) skillType.getSkill().onLevelUp(player, oldLevel, newLevel);
                }
        }

        // Event handlers

        public void onJoin(Player player) {
                this.player = player;
                cancelRemoval();
                travelingPlayerInfo.setLocation(player.getLocation());
                travelingPlayerInfo.setFarTravelLocation(player.getLocation());
        }

        // Caching functions and procedures

        public void load() {
                plugin.sqlManager.loadPlayerInfo(this);
        }

        private void removeSoon() {
                cancelRemoval();
                removalTask = new BukkitRunnable() {
                        public void run() {
                                if (!player.isOnline()) {
                                        plugin.playerManager.remove(player.getName());
                                }
                        }
                };
                removalTask.runTaskLater(plugin, 200L);
        }

        private void cancelRemoval() {
                if (removalTask == null) return;
                try { removalTask.cancel(); } catch (IllegalStateException e) {}
                removalTask = null;
        }

        public void onRemoval() {
                cancelRemoval();
        }

        public int getSkillLevel(SkillType skillType) {
                return skillInfo.get(skillType).skillLevel;
        }

        /**
         * This function returns the remaining skill points
         * required to level up, not the total points needed,
         * which is what is cached in the info.
         */
        public int getRequiredSkillPoints(SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerSkillInfo info = skillInfo.get(skillType);
                return info.requiredSkillPoints - info.skillPoints;
        }

        public void flushCache(SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerSkillInfo info = skillInfo.get(skillType);
                info.skillLevel = skill.getLevelForSkillPoints(info.skillPoints);
                info.requiredSkillPoints = skill.getSkillPointsForLevel(info.skillLevel + 1);
        }
}
