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
        public final SkillsPlugin plugin;
        private Player player;

        // Skill information
        private final Map<SkillType, PlayerSkillInfo> skillInfo = new EnumMap<SkillType, PlayerSkillInfo>(SkillType.class);
        private int totalLevel = 0;
        // A players may choose one primary element.
        private ElementType primaryElement = null;

        // Additional specific information.
        public final TravelingPlayerInfo travelingInfo = new TravelingPlayerInfo();
        public final SpellsPlayerInfo spellsInfo = new SpellsPlayerInfo(this);

        private BukkitRunnable removalTask = null;

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

        // Skill points and levels, getters and setters.

        public int getSkillPoints(SkillType skillType) {
                return skillInfo.get(skillType).skillPoints;
        }

        public int getSkillLevel(SkillType skillType) {
                return skillInfo.get(skillType).skillLevel;
        }

        public int getTotalSkillLevel() {
                return totalLevel;
        }

        public int getElementalLevel(ElementType elem) {
                int sum = 0;
                SkillType skillTypes[] = elem.getSkills();
                sum += getSkillPoints(skillTypes[0]) * 2;
                sum += getSkillPoints(skillTypes[1]);
                sum += getSkillPoints(skillTypes[2]);
                sum /= 4;
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
                if (info.skillPoints >= info.requiredSkillPoints && info.skillLevel < AbstractSkill.MAX_LEVEL) {
                        int oldLevel = info.skillLevel;
                        flushCache(skillType);
                        int newLevel = info.skillLevel;
                        if (newLevel > oldLevel) skillType.getSkill().onLevelUp(player, oldLevel, newLevel);
                }
        }

        public ElementType getPrimaryElement() {
                return primaryElement;
        }

        /**
         * Set the primary skill level of this player.
         *
         * Other than some of the other setters, this does not
         * imply any checks or sql writes.  Instead,
         * SwitchElementRequest is used to do all these things,
         * since the cooldown is checked with the database to
         * avoid multi-server exploits.  If the above request is
         * successful, it calls this.
         */
        public void setPrimaryElement(ElementType element) {
                this.primaryElement = element;
        }

        // Economy methods.

        public boolean hasMoney(double amount) {
                return plugin.economyManager.has(player, amount);
        }

        public boolean takeMoney(double amount) {
                return plugin.economyManager.take(player, amount);
        }

        public boolean giveMoney(double amount) {
                return plugin.economyManager.give(player, amount);
        }

        // Event handlers. We listen to some events that are
        // general enough so the particular skills shouldn't
        // bother.

        public void onJoin(Player player) {
                // Load player statistics.
                load();

                // Update Player reference.
                this.player = player;

                // The player joined. If we were waiting to remove
                // his data, cancel that.
                cancelRemoval();

                // Update locations for the traveling skill.
                travelingInfo.setLocation(player.getLocation());
                travelingInfo.setFarTravelLocation(player.getLocation());
                
        }

        public void onQuit(Player player) {
                // Update Player reference.
                this.player = player;

                removeSoon();
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
                removalTask.runTaskLater(plugin, 100L);
        }

        private void cancelRemoval() {
                if (removalTask == null) return;
                try { removalTask.cancel(); } catch (IllegalStateException e) {}
                removalTask = null;
        }

        public void onRemoval() {
                // Make sure the task is cancelled, in case this
                // is not called by the task.
                cancelRemoval();
        }

        /**
         * This function returns the remaining skill points
         * required to level up, not the total points needed,
         * which is what is cached in the info.
         */
        public int getRequiredSkillPoints(SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerSkillInfo info = skillInfo.get(skillType);
                if (info.skillLevel >= AbstractSkill.MAX_LEVEL) return 0;
                return info.requiredSkillPoints - info.skillPoints;
        }

        public void flushCache(SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerSkillInfo info = skillInfo.get(skillType);

                // Cache data.
                final int oldSkillLevel = info.skillLevel;
                final int newSkillLevel = skill.getLevelForSkillPoints(info.skillPoints);

                // Update level and required skill points.
                info.skillLevel = newSkillLevel;
                info.requiredSkillPoints = skill.getSkillPointsForLevel(newSkillLevel + 1);

                // Update total skill level.
                totalLevel += newSkillLevel - oldSkillLevel;

                // Update Scoreboards
                plugin.scoreboardManager.setSkillLevel(skillType, player, newSkillLevel);
                plugin.scoreboardManager.setTotalLevel(player, totalLevel);
        }
}
