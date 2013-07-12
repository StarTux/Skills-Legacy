package com.winthier.skills.command;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {
        public final SkillsPlugin plugin;

        public SkillCommand(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getCommand("skill").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                Player player = null;
                if (sender instanceof Player) {
                        player = (Player)sender;
                        PlayerInfo playerInfo = plugin.playerManager.getPlayerInfo(player);
                }
                if (args.length == 0) {
                        if (player == null) {
                                Util.sendMessage(sender, "&cPlayer expected");
                                return true;
                        }
                        plugin.sqlManager.sendPlayerStatistics(sender, player.getName());
                        return true;
                }
                if (args.length == 1) {
                        if (player == null) {
                                Util.sendMessage(sender, "&cPlayer expected");
                                return true;
                        }
                        String skillName = args[0];
                        SkillType skillType = SkillType.fromUserString(skillName);
                        if (skillType == null) {
                                Util.sendMessage(sender, "&cUnknown skill: %s", skillName);
                                return true;
                        }
                        plugin.sendSkillInfo(player, player, skillType);
                        return true;
                }
                return false;
        }
}
