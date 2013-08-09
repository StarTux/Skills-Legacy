package com.winthier.skills.sql;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import org.bukkit.OfflinePlayer;

public class HighscoreScoreboardRequest extends HighscoreRequest {
        public HighscoreScoreboardRequest(SkillsPlugin plugin, SkillType skill) {
                super(plugin, skill, 0, 10);
        }

        @Override
        public void run() {
                for (int i = 0; i < count; ++i) {
                        plugin.scoreboardManager.setSkillLevel(skill, names[i], levels[i]);
                }
        }
}
