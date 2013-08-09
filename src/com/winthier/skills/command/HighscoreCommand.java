package com.winthier.skills.command;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HighscoreCommand implements CommandExecutor {
        private final SkillsPlugin plugin;

        public HighscoreCommand(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getCommand("Highscore").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                // Figure out command is used by a player.
                Player player = null;
                if (sender instanceof Player) player = (Player)sender;

                // More than 2 arguments are illegal
                if (args.length > 2) return false;

                // No arguments means we display the overall
                // highscore and toggle the scoreboard.
                if (args.length == 0) {
                        if (player != null) {
                                plugin.scoreboardManager.toggleTotal(player);
                        }
                        plugin.sqlManager.sendTotalHighscore(sender, 0);
                        return true;
                }

                int page = 0;
                boolean usePageNumber = false;

                // One argument could by just a page number, so check for that.
                if (args.length == 1) {
                        boolean pageOnly = true;
                        try {
                                page = Integer.parseInt(args[0]) - 1;
                                usePageNumber = true;
                        } catch (NumberFormatException nfe) {
                        }
                        if (usePageNumber) {
                                if (page < 0) {
                                        Util.sendMessage(sender, "&cPage must be within 1 and 100, got %d.", page + 1);
                                        return true;
                                }
                                plugin.sqlManager.sendTotalHighscore(sender, page);
                                return true;
                        }
                }

                // Two arguments means skill name, then page number.
                if (args.length >= 2) {
                        try {
                                page = Integer.parseInt(args[1]) - 1;
                                usePageNumber = true;
                        } catch (NumberFormatException nfe) {
                                Util.sendMessage(sender, "&cNumber expected: %s", args[1]);
                                return true;
                        }
                        if (page < 0) {
                                Util.sendMessage(sender, "&cPage must be within 1 and 100, got %d.", page + 1);
                                return true;
                        }
                }

                // Figure out skill name
                SkillType skill = SkillType.fromUserString(args[0]);
                if (skill == null) {
                        Util.sendMessage(sender, "&cSkill name expected: %s", args[0]);
                        return true;
                }

                // Toggle the player's scoreboard if they didn't use a page number argument.
                if (!usePageNumber && player != null) {
                        plugin.scoreboardManager.toggleSkill(skill, player);
                }

                plugin.sqlManager.sendHighscore(sender, skill, page);
                return true;
        }
}
