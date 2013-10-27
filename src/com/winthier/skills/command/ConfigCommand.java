package com.winthier.skills.command;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.spell.AbstractSpell;
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
                plugin.getCommand("SkillConfig").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                Player player = null;
                if (sender instanceof Player) player = (Player)sender;
                if (args.length == 1 && args[0].equalsIgnoreCase("Help")) {
                        sendHelp(sender);
                        return true;
                }
                if (args.length >= 1 && args[0].equalsIgnoreCase("Info")) {
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
                if (args.length >= 1 && args[0].equalsIgnoreCase("Stats")) {
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
                if (args.length >= 1 && args[0].equalsIgnoreCase("SkillLevel")) {
                        if (args.length < 3 || args.length > 4) {
                                Util.sendMessage(sender, "&cUsage: /%s SkillLevel <skill> [player] <level>", label);
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
                if (args.length >= 1 && args[0].equalsIgnoreCase("SpellLevel")) {
                        if (args.length != 4) {
                                Util.sendMessage(sender, "&cUsage: /%s SpellLevel <spell> <player> <level>", label);
                                return true;
                        }
                        AbstractSpell spell = plugin.spellManager.getSpell(args[1]);
                        if (spell == null) {
                                Util.sendMessage(sender, "&cSpell not found: %s.", args[1]);
                                return true;
                        }
                        String playerName = args[2];
                        Player other = plugin.getServer().getPlayer(playerName);
                        if (other != null) playerName = other.getName();
                        PlayerInfo info = plugin.playerManager.getPlayerInfo(playerName);
                        int level = 0;
                        try {
                                level = Integer.parseInt(args[3]);
                        } catch (NumberFormatException nfe) {
                                Util.sendMessage(sender, "&cNot a number: %s.", args[3]);
                                return true;
                        }
                        if (level < 0 || level > spell.getMaxLevel()) {
                                Util.sendMessage(sender, "&cInvalid level. Got %d.", level);
                                return true;
                        }
                        if (info == null) {
                                plugin.sqlManager.setSpellLevel(playerName, spell, level);
                        } else {
                                info.spellsInfo.setSpellLevel(spell, level);
                        }
                        Util.sendMessage(sender, "&aSet %s level for %s to %d.", spell.getName(), playerName, level);
                        return true;
                }
                if (args.length == 1 && args[0].equalsIgnoreCase("Reload")) {
                        plugin.reloadConfig();
                        SkillType.loadAll();
                        Util.sendMessage(sender, "&eConfiguration reloaded.");
                        return true;
                }
                if (args.length == 1 && args[0].equalsIgnoreCase("Flush")) {
                        plugin.playerManager.onDisable();
                        plugin.playerManager.onEnable();
                        Util.sendMessage(sender, "&ePlayer data flushed.");
                        return true;
                }
                if (args.length == 1 && args[0].equalsIgnoreCase("Totems")) {
                        if (player == null) {
                                Util.sendMessage(sender, "&cPlayer expected");
                                return true;
                        }
                        for (ElementType element : ElementType.values()) {
                                if (!element.hasTotem()) continue;
                                player.getInventory().addItem(Totem.createTotem(element));
                                player.sendMessage(element.getDisplayName() + " totem given.");
                        }
                        return true;
                }
                if (args.length == 3 && args[0].equalsIgnoreCase("Element")) {
                        Player other = plugin.getServer().getPlayerExact(args[1]);
                        if (other == null) {
                                Util.sendMessage(sender, "&cPlayer not found: %s.", args[1]);
                                return true;
                        }
                        ElementType element = Util.enumFromString(ElementType.class, args[2]);
                        if (element == null) {
                                Util.sendMessage(sender, "&cInvalid element: %s.", args[2]);
                                return true;
                        }
                        PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                        info.setPrimaryElement(element);
                        Util.sendMessage(sender, "&aSet primary element of %s to %s.", player.getName(), element.getDisplayName());
                        return true;
                }
                sendHelp(sender);
                return true;
        }

        private void sendHelp(CommandSender sender) {
                Util.sendMessage(sender, "&eSkills Configuration Interface");
                Util.sendMessage(sender, "&fUSAGE:&e /skc &6<subcommand> [args...]");
                Util.sendMessage(sender, "&fSUBCOMMANDS");
                Util.sendMessage(sender, "&e/skc &6Reload&r - Reload the configuration file");
                Util.sendMessage(sender, "&e/skc &6Flush&r - Reload all player data");
                Util.sendMessage(sender, "&e/skc &6Stats <player>&r - Lookup a player's skill statistics.");
                Util.sendMessage(sender, "&e/skc &6SkillLevel <skill> [player] <level>&r - Modify a player's skill level.");
                Util.sendMessage(sender, "&e/skc &6SpellLevel <spell> <player> <level>&r - Modify a player's spell level.");
                Util.sendMessage(sender, "&e/skc &6Totems&r - Get a full set of totems.");
                Util.sendMessage(sender, "&e/skc &6Element <player> <element>&r - Set the primary element of a player.");
        }
}
