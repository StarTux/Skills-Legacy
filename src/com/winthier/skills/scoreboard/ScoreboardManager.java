package com.winthier.skills.scoreboard;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Keep track of all the skills' scoreboards as well as the total
 * skill level scoreboard. Maintain all player data.
 */
public class ScoreboardManager {
        private final SkillsPlugin plugin;
        private HighscoreScoreboard total = null;
        private final Map<SkillType, HighscoreScoreboard> skills = new EnumMap<SkillType, HighscoreScoreboard>(SkillType.class);

        public ScoreboardManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                // Create and load skill scoreboards.
                for (SkillType skill : SkillType.values()) {
                        skills.put(skill, HighscoreScoreboard.forSkill(skill));
                        plugin.sqlManager.loadHighscoreScoreboard(skill);
                }

                // Create and load total scoreboard.
                total = HighscoreScoreboard.forTotals();
                plugin.sqlManager.loadTotalHighscoreScoreboard();
        }

        public void onDisable() {
                total = null;
                for (SkillType skill : SkillType.values()) skills.remove(skill);
        }

        public void setSkillLevel(SkillType skill, OfflinePlayer player, int level) {
                skills.get(skill).setLevel(player, level);
        }

        public void setSkillLevel(SkillType skill, String name, int level) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
                setSkillLevel(skill, player, level);
        }

        public void setTotalLevel(OfflinePlayer player, int level) {
                total.setLevel(player, level);
        }

        public void setTotalLevel(String name, int level) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(name);
                setTotalLevel(player, level);
        }

        public void toggleSkill(SkillType skill, Player player) {
                skills.get(skill).toggle(player);
        }

        public void toggleTotal(Player player) {
                total.toggle(player);
        }
}
