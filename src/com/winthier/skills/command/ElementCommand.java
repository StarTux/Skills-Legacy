package com.winthier.skills.command;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ElementCommand implements CommandExecutor {
        private final SkillsPlugin plugin;

        public ElementCommand(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getCommand("Element").setExecutor(this);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                if (!(sender instanceof Player)) {
                        Util.sendMessage(sender, "&cPlayer expected.");
                        return true;
                }
                final Player player = (Player)sender;
                final PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                if (args.length == 0) {
                        final ElementType element = info.getPrimaryElement();
                        if (element == null) {
                                Util.sendMessage(sender, "&3You don't have a primary element selected.");
                        } else {
                                Util.sendMessage(sender, "&3Your primary element is %s%s &b%s.", element.getColor(), Util.ICON, element.getDisplayName());
                        }
                        sendUsageMessage(sender, label);
                        return true;
                }
                if (args.length > 1 || args[0].equalsIgnoreCase("Help")) {
                        sendUsageMessage(sender, label);
                        return true;
                }
                final ElementType element = ElementType.fromString(args[0]);
                if (element == null) {
                        sendUsageMessage(sender, label);
                        return true;
                }
                if (element == ElementType.MAGIC) {
                        Util.sendMessage(sender, "&cYou can't select Magic as your primary element.");
                        return true;
                }
                if (element == info.getPrimaryElement()) {
                        Util.sendMessage(sender, "&c%s already is your primary element.", element.getDisplayName());
                        return true;
                }

                plugin.sqlManager.switchElement(info, element);
                return true;
        }

        private void sendUsageMessage(CommandSender sender, String label) {
                Util.sendMessage(sender, "&7&oTo switch element, type &3/%s &bEarth&3|&bFire&3|&bAir&3|&bWater", label);
        }
}
