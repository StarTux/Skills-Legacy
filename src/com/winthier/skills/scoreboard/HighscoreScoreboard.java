package com.winthier.skills.scoreboard;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * A scoreboard associated with a skill or the overall highscore.
 * This class is intended to remain ignorant of its purpose so it
 * can be used for skills and the total highscore alike.
 */
public class HighscoreScoreboard {
        private final Scoreboard scoreboard;
        private final Objective sidebarObjective;
        private final Objective playerListObjective;
        private final Objective belowNameObjective;
        private final int LIST_LEN = 10;
        private final OfflinePlayer topList[] = new OfflinePlayer[LIST_LEN];

        private HighscoreScoreboard() {
                scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

                // Create objectives
                sidebarObjective = scoreboard.registerNewObjective("sidebar", "dummy");
                playerListObjective = scoreboard.registerNewObjective("player_list", "dummy");
                belowNameObjective = scoreboard.registerNewObjective("below_name", "dummy");

                // Set display slots
                sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
                playerListObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                belowNameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

                for (int i = 0; i < topList.length; ++i) topList[i] = null;
        }

        public static HighscoreScoreboard forSkill(SkillType skill) {
                HighscoreScoreboard result = new HighscoreScoreboard();
                result.setSidebarTitle(Util.format("%s%s &b%s %s%s", skill.getColor(), Util.ICON, skill.getDisplayName(), skill.getColor(), Util.ICON));
                result.setBelowNameTitle(Util.format("%s%s &b%s", skill.getColor(), Util.ICON, skill.getDisplayName()));
                return result;
        }

        public static HighscoreScoreboard forTotals() {
                HighscoreScoreboard result = new HighscoreScoreboard();
                result.setSidebarTitle(Util.format("&b%s &bTotal Skill Level &b%s", Util.ICON, Util.ICON));
                result.setBelowNameTitle(Util.format("&b%s &bTotal", Util.ICON));
                return result;
        }

        public void setSidebarTitle(String title) {
                sidebarObjective.setDisplayName(title);
        }

        public void setBelowNameTitle(String title) {
                belowNameObjective.setDisplayName(title);
        }

        public void setTopList(OfflinePlayer player, int level) {
                int minIndex = -1;
                for (int i = 0; i < LIST_LEN; ++i) {
                        // If there is an empty slot, fill it.
                        if (topList[i] == null) {
                                topList[i] = player;
                                sidebarObjective.getScore(player).setScore(level);
                                return;
                        }
                        // If the player is already in the list, update it.
                        if (topList[i].equals(player)) {
                                sidebarObjective.getScore(player).setScore(level);
                                return;
                        }
                        // If tehre is another player with a lower score, remember. See below.
                        if (sidebarObjective.getScore(topList[i]).getScore() < level) {
                                minIndex = i;
                        }
                }

                // Another player in the list had a lower score. Replace it.
                if (minIndex >= 0) {
                        OfflinePlayer old = topList[minIndex];
                        if (old.isOnline()) {
                                int oldScore = belowNameObjective.getScore(old).getScore();
                                scoreboard.resetScores(old);
                                belowNameObjective.getScore(old).setScore(oldScore);
                                playerListObjective.getScore(old).setScore(oldScore);
                        } else {
                                scoreboard.resetScores(old);
                        }
                        topList[minIndex] = player;
                        sidebarObjective.getScore(player).setScore(level);
                }
        }

        public void setLevel(OfflinePlayer player, int level) {
                if (level > 0) setTopList(player, level);
                if (player.isOnline()) {
                        playerListObjective.getScore(player).setScore(level);
                        belowNameObjective.getScore(player).setScore(level);
                }
        }

        public void showTo(Player player) {
                player.setScoreboard(scoreboard);
        }

        public void toggle(Player player) {
                if (player.getScoreboard() == scoreboard) {
                        player.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
                } else {
                        player.setScoreboard(scoreboard);
                }
        }
}
