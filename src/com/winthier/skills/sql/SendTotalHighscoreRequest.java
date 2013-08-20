package com.winthier.skills.sql;

import com.winthier.skills.SkillsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SendTotalHighscoreRequest extends TotalHighscoreRequest {
        private final CommandSender sender;

        public SendTotalHighscoreRequest(SkillsPlugin plugin, CommandSender sender, int page) {
                super(plugin, page, 10);
                this.sender = sender;
        }

        @Override
        public void run() {
                plugin.sendHighscore(sender, "Total", ChatColor.AQUA, count, names, levels, page);
        }
}
