package com.winthier.skills.player;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInfo {
        private final SkillsPlugin plugin;
        private Player player;
        private final Map<SkillType, PlayerSkillInfo> skillInfo = new EnumMap<SkillType, PlayerSkillInfo>(SkillType.class);
        public final TravellingPlayerInfo travellingPlayerInfo = new TravellingPlayerInfo(this);

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
        }

        // Caching functions and procedures

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
