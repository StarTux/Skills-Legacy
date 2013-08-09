package com.winthier.skills.sql;

import com.winthier.skills.SkillsPlugin;
import org.bukkit.OfflinePlayer;

public class TotalHighscoreScoreboardRequest extends TotalHighscoreRequest {
        public TotalHighscoreScoreboardRequest(SkillsPlugin plugin) {
                super(plugin, 0, 10);
        }

        @Override
        public void run() {
                for (int i = 0; i < count; ++i) {
                        plugin.scoreboardManager.setTotalLevel(names[i], levels[i]);
                }
        }
}
