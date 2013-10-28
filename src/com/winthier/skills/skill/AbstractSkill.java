package com.winthier.skills.skill;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.util.Util;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class AbstractSkill implements Listener {
        public final SkillsPlugin plugin;
        public final SkillType skillType;
        public final static int MAX_LEVEL = 999;
        private String description;
        private final static int[] gaussian = new int[MAX_LEVEL + 1];
        private ConfigurationSection config = null;

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
         * called.  To circumvent hooks, use setSkillPointsBare().
         */
        public void addSkillPoints(Player player, int skillPoints) {
                if (skillPoints <= 0) return;
                //player.sendMessage(skillType.getName() + " sp + " + skillPoints);
                PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                if (!info.canCollectSkillPoints()) return;
                boolean bonus = false;
                for (ElementType element : skillType.getElements()) {
                        if (element == info.getPrimaryElement()) {
                                bonus = true;
                                break;
                        }
                }
                if (bonus) {
                        skillPoints = plugin.proficiencySkillPointFactor.roll(skillPoints);
                }
                info.addSkillPoints(skillType, skillPoints);
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
                final PlayerInfo info = plugin.playerManager.getPlayerInfo(player);

                // Figure out if this is the primary element.
                final ElementType primaryElement = info.getPrimaryElement();
                boolean primary = false;
                if (primaryElement != null) {
                        for (ElementType element : skillType.getElements()) {
                                if (element == primaryElement) {
                                        primary = true;
                                        break;
                                }
                        }
                }

                // Give reward.
                double reward = 0.0;
                if (primary) {
                        int sum = 0;
                        for (int i = oldLevel + 1; i <= newLevel; ++i) sum += i;
                        reward = (float)sum * plugin.rewardFactor;
                }
                if (primary && reward > 0.0) {
                        info.giveMoney(reward);
                        Util.sendMessage(player, "&b%s %s%s &3Level&f %d&3, Reward &f%s", skillType.getDisplayName(), skillType.getColor(), Util.ICON, newLevel, plugin.economyManager.format(reward));
                } else {
                        Util.sendMessage(player, "&b%s %s%s &3Level&f %d", skillType.getDisplayName(), skillType.getColor(), Util.ICON, newLevel);
                }

                // Play sound.
                final int period = newLevel % 5;
                if (period == 0) {
                        player.playSound(player.getLocation(), "random.levelup", 1.0f, 1.0f);
                } else { // 1..4
                        player.playSound(player.getLocation(), "random.successful_hit", 1.0f, 0.4f + 0.1f * (float)period);
                }
        }

        // configuration

        protected int multiplyXp(Player player, int xp) {
                return Util.rollFraction(xp, getXpMultiplier(player), 100);
        }

        protected int getXpMultiplier(Player player) {
                return 100 + getSkillLevel(player) / 2;
        }

        protected int getSkullDropPermil(Player player) {
                return Math.min(50, getSkillLevel(player) / 50);
        }

        public ConfigurationSection getConfig() {
                if (config == null) {
                        config = plugin.getSkillsConfig().getConfigurationSection(skillType.getName());
                        if (config == null) {
                                plugin.getLogger().warning("Skill type has no configuration section: " + skillType.getName());
                                config = plugin.getSkillsConfig().createSection(skillType.getName());
                        }
                }
                return config;
        }

        public void loadConfig() {
                ConfigurationSection config = getConfig();
                description = config.getString("Description", "");
                loadConfiguration();
        }

        public abstract void loadConfiguration();
}
