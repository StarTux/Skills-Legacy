package com.winthier.skills;

import com.winthier.skills.command.SkillCommand;
import com.winthier.skills.player.PlayerManager;
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
                Player player = null;
                if (sender instanceof Player) player = (Player)sender;
                if (args.length == 3 && args[0].equals("setlevel")) {
                        SkillType skillType = SkillType.fromString(args[1]);
                        if (skillType == null) {
                                sender.sendMessage("Bad skill type: " + args[1]);
                                return true;
                        }
                        int level;
                        try {
                                level = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                                sender.sendMessage("Bad skill level: " + args[2]);
                                e.printStackTrace();
                                return true;
                        }
                        skillType.getSkill().setSkillLevel(player, level);
                        player.sendMessage("Set skill level to " + level);
                        return true;
                }
                if (args.length == 1 && args[0].equals("starve")) {
                        player.setFoodLevel(0);
                }
                return false;
        }
}
