package com.winthier.skills.command;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.spell.Totem;
import com.winthier.skills.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigCommand implements CommandExecutor {
        public final SkillsPlugin plugin;

        public ConfigCommand(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getCommand("skillconfig").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                Player player = null;
                if (sender instanceof Player) player = (Player)sender;
                if (args.length == 1 && args[0].equals("help")) {
                        sendHelp(sender);
                        return true;
                }
                if (args.length >= 1 && args[0].equals("info")) {
                        if (args.length != 3) {
                                Util.sendMessage(sender, "&eUsage: /%s <player> <skill>", label);
                                return true;
                        }
                        String playerName = args[1];
                        String skillName = args[2];
                        Player other = plugin.getServer().getPlayer(playerName);
                        if (other == null) {
                                Util.sendMessage(sender, "&cPlayer not online: %s", playerName);
                                return true;
                        }
                        SkillType skillType = SkillType.fromUserString(skillName);
                        if (skillType == null) {
                                Util.sendMessage(sender, "&cUnknown skill: %s", skillName);
                                return true;
                        }
                        plugin.sendSkillInfo(sender, other, skillType);
                        return true;
                }
                if (args.length >= 1 && args[0].equals("stats")) {
                        String playerName = null;
                        if (args.length == 1) {
                                if (player != null) {
                                        playerName = player.getName();
                                } else {
                                        Util.sendMessage(sender, "&cPlayer name required.");
                                        return true;
                                }
                        }
                        if (playerName == null) {
                                Player onl = plugin.getServer().getPlayer(args[1]);
                                if (onl != null) playerName = onl.getName();
                        }
                        if (playerName == null) {
                                OfflinePlayer off = plugin.getServer().getOfflinePlayer(args[1]);
                                if (off.hasPlayedBefore()) playerName = off.getName();
                        }
                        // give up; be naive
                        if (playerName == null) {
                                playerName = args[1];
                                Util.sendMessage(sender, "&cWarning: %s is unknown!", playerName);
                        }
                        plugin.sqlManager.sendPlayerStatistics(sender, playerName);
                        return true;
                }
                if (args.length >= 1 && args[0].equals("setlevel")) {
                        if (args.length < 3 || args.length > 4) {
                                Util.sendMessage(sender, "&cUsage: /%s setlevel <skill> [player] <level>", label);
                                return true;
                        }
                        String skillName = args[1];
                        Player affectedPlayer = null;
                        String playerName = null;
                        String levelName = null;
                        if (args.length == 3) {
                                if (player != null) {
                                        affectedPlayer = player;
                                        playerName = player.getName();
                                        levelName = args[2];
                                } else {
                                        Util.sendMessage(sender, "&cPlayer name required.");
                                        return true;
                                }
                        } else { // args.length == 4
                                playerName = args[2];
                                levelName = args[3];
                                affectedPlayer = plugin.getServer().getPlayer(args[2]);
                                if (affectedPlayer == null) {
                                        OfflinePlayer off = plugin.getServer().getOfflinePlayer(playerName);
                                        if (off.hasPlayedBefore()) {
                                                playerName = off.getName();
                                        } else {
                                                Util.sendMessage(sender, "&cPlayer not found: " + playerName);
                                        }
                                } else {
                                        playerName = affectedPlayer.getName();
                                }
                        }
                        final SkillType skillType = SkillType.fromUserString(skillName);
                        if (skillType == null) {
                                Util.sendMessage(sender, "Bad skill type: " + skillName);
                                return true;
                        }
                        int level;
                        try {
                                level = Integer.parseInt(levelName);
                        } catch (NumberFormatException e) {
                                sender.sendMessage("Bad skill level: " + levelName);
                                return true;
                        }
                        // If the player is online
                        if (affectedPlayer != null) {
                                skillType.getSkill().setSkillLevel(affectedPlayer, level);
                        } else {
                                plugin.sqlManager.setSkillPoints(playerName, skillType, skillType.getSkill().getSkillPointsForLevel(level));
                                // force reload in case player data are still cached
                                plugin.playerManager.remove(playerName);
                        }
                        Util.sendMessage(sender, "Set %s skill level of %s to %s.", skillType.getDisplayName(), playerName, level);
                        return true;
                }
                if (args.length == 1 && args[0].equals("reload")) {
                        plugin.reloadConfig();
                        SkillType.loadAll();
                        Util.sendMessage(sender, "&eConfiguration reloaded.");
                        return true;
                }
                if (args.length == 1 && args[0].equals("flush")) {
                        plugin.playerManager.onDisable();
                        plugin.playerManager.onEnable();
                        Util.sendMessage(sender, "&ePlayer data flushed.");
                        return true;
                }
                if (args.length == 1 && args[0].equals("totems")) {
                        if (player == null) {
                                Util.sendMessage(sender, "&cPlayer expected");
                                return true;
                        }
                        for (ElementType element : ElementType.values()) {
                                player.getInventory().addItem(Totem.createTotem(element));
                                player.sendMessage(element.getDisplayName() + " totem given.");
                        }
                        return true;
                }
                sendHelp(sender);
                return true;
        }

        private void sendHelp(CommandSender sender) {
                Util.sendMessage(sender, "&eSkills Configuration Interface");
                Util.sendMessage(sender, "&fUSAGE:&e /skc &6<subcommand> [args...]");
                Util.sendMessage(sender, "&fSUBCOMMANDS");
                Util.sendMessage(sender, "&e/skc &6reload&r - Reload the configuration file");
                Util.sendMessage(sender, "&e/skc &6flush&r - Reload all player data");
                Util.sendMessage(sender, "&e/skc &6stats <player>&r - Lookup a player's skill statistics.");
                Util.sendMessage(sender, "&e/skc &6setlevel <skill> [player] <level>&r - Modify a player's skill level.");
                Util.sendMessage(sender, "&e/skc &6totems&r - Get a full set of totems.");
        }
}
