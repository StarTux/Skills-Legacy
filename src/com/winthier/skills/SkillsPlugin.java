package com.winthier.skills;

import com.winthier.skills.command.ConfigCommand;
import com.winthier.skills.command.SkillCommand;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.player.PlayerManager;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.sql.SQLManager;
import com.winthier.skills.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillsPlugin extends JavaPlugin {
        public final PlayerManager playerManager = new PlayerManager(this);
        public final SQLManager sqlManager = new SQLManager(this);
        public final SkillCommand skillCommand = new SkillCommand(this);
        public final ConfigCommand configCommand = new ConfigCommand(this);

        @Override
        public void onEnable() {
                // setup config
                Util.copyResource(this, "config.yml", true);
                reloadConfig();
                // read config
                SkillType.initAll(this);
                sqlManager.onEnable();
                SkillType.loadAll();
                playerManager.onEnable();
                skillCommand.onEnable();
                configCommand.onEnable();
                // write config
                getConfig().options().copyDefaults(true);
                saveConfig();
        }

        @Override
        public void onDisable() {
                playerManager.onDisable();
                sqlManager.onDisable();
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                return false;
        }

        /**
         * Send skill information about player to sender.
         */
        public void sendSkillInfo(CommandSender sender, Player player, SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerInfo info = playerManager.getPlayerInfo(player);
                Util.sendMessage(sender, "&3=== %s\u25A3 &b%s %s\u25A3 &bSkill Info&3 ===", skillType.getColor(), skillType.getDisplayName(), skillType.getColor());
                Util.sendMessage(sender, "&3 &m   &b Elements &3&m   ");
                for (ElementType elem : skillType.getElements()) {
                        Util.sendMessage(sender, " %s\u25A3 &b%s", elem.getColor(), elem.getDisplayName());
                }
                Util.sendMessage(sender, "&3 &m   &b Description &3&m   ");
                for (String line : skill.getDescription().split("\n")) {
                        Util.sendMessage(sender, " &7%s", line);
                }
                Util.sendMessage(sender, "&3 &m   &b Perks &3&m   ");
                for (String line : skill.getPerkDescription(player)) {
                        Util.sendMessage(sender, " &7%s", line);
                }
                Util.sendMessage(sender, "&3 &m   &b Statistics &3&m   ");
                Util.sendMessage(sender, "&3 Level: &b%d&3 SP: &b%d&3 For next level: &b%s", info.getSkillLevel(skillType), info.getSkillPoints(skillType), info.getRequiredSkillPoints(skillType));
        }
}
