package com.winthier.skills.sql;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import org.bukkit.command.CommandSender;

public class SendHighscoreRequest extends HighscoreRequest {
        private final CommandSender sender;

        public SendHighscoreRequest(SkillsPlugin plugin, CommandSender sender, SkillType skill, int page) {
                super(plugin, skill, page, 10);
                this.sender = sender;
        }

        @Override
        public void run() {
                plugin.sendHighscore(sender, skill.getDisplayName(), skill.getColor(), count, names, levels, page);
        }
}
