package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class AbstractSkill implements Listener {
        public final SkillsPlugin plugin;
        public final SkillType skillType;
        public final static int MAX_LEVEL = 999;
        private String description;
        private final static int[] gaussian = new int[MAX_LEVEL + 1];

        static {
                for (int i = 0; i < gaussian.length; ++i) {
                        gaussian[i] = (i * i + i) / 2;
                }
        }

        public AbstractSkill(SkillsPlugin plugin, SkillType skillType) {
                this.plugin = plugin;
                this.skillType = skillType;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        // Getters

        public SkillType getSkillType() {
                return skillType;
        }

        public String getDescription() {
                return description;
        }

        public List<String> getPerkDescription(Player player) {
                return Collections.<String>emptyList();
        }

        // Utility functions for subclasses

        public static int gaussian(int i) {
                return gaussian[i];
        }

        public int getSkillPointsForLevel(int level) {
                if (level > MAX_LEVEL) return 0;
                return gaussian[level];
        }

        public static int getLevelForSkillPoints(int skillPoints) {
                int i = Arrays.binarySearch(gaussian, skillPoints);
                if (i < 0) return -i - 2;
                return i;
        }

        // Player related functions

        /**
         * Check if a given player is currently eligible to
         * collect experience points.
         */
        public static final boolean canCollectSkillPoints(Player player) {
                if (player.getGameMode() == GameMode.CREATIVE) return false;
                if (!player.hasPermission("skills.play")) return false;
                return true;
        }

        public int getSkillPoints(Player player) {
                return plugin.playerManager.getPlayerInfo(player).getSkillPoints(skillType);
        }

        public int getRequiredSkillPoints(Player player) {
                return plugin.playerManager.getPlayerInfo(player).getRequiredSkillPoints(skillType);
        }

        /**
         * Give skill points to this player. It is assumed that he
         * PlayerData.addSkillPoints().  got the points legitly,
         * so all hooks such as saving and leveling up will be
         * called.  To circumvent hooks, use
         */
        public void addSkillPoints(Player player, int skillPoints) {
                // TODO hooks
                //player.sendMessage(skillType.getName() + " sp + " + skillPoints);
                if (!canCollectSkillPoints(player)) return;
                plugin.playerManager.getPlayerInfo(player).addSkillPoints(skillType, skillPoints);
        }

        public int getSkillLevel(Player player) {
                // cached
                return plugin.playerManager.getPlayerInfo(player).getSkillLevel(skillType);
        }

        public void setSkillLevel(Player player, int level) {
                if (level > MAX_LEVEL) level = MAX_LEVEL;
                plugin.playerManager.getPlayerInfo(player).setSkillPoints(skillType, getSkillPointsForLevel(level));
        }

        // Handlers

        public void onLevelUp(Player player, int oldLevel, int newLevel) {
                Util.sendMessage(player, "&b%s %s\u25A3 &3Level&f %d", skillType.getDisplayName(), skillType.getColor(), newLevel);
        }

        // configuration

        protected int multiplyXp(Player player, int xp) {
                return Util.rollFraction(xp, getXpMultiplier(player), 100);
        }

        protected int getXpMultiplier(Player player) {
                return 100 + getSkillLevel(player) / 2;
        }

        protected ConfigurationSection getSkillsSection() {
                return plugin.getConfig().getConfigurationSection("skills");
        }

        public ConfigurationSection getConfig() {
                ConfigurationSection result = getSkillsSection().getConfigurationSection(skillType.getName());
                if (result == null) {
                        plugin.getLogger().warning("Skill type has no configuration section: " + skillType.getName());
                        result = getSkillsSection().createSection(skillType.getName());
                }
                return result;
        }

        public void loadConfig() {
                description = getConfig().getString("Description", "");
                loadConfiguration();
        }

        public abstract void loadConfiguration();
}
