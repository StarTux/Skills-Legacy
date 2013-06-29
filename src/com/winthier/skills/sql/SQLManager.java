package com.winthier.skills.sql;

import com.winthier.libsql.ConnectionManager;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;

public class SQLManager {
        public final SkillsPlugin plugin;
        private ConnectionManager connectionManager;

        public SQLManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                connectionManager = new ConnectionManager(plugin, plugin.getConfig().getConfigurationSection("sql"));
                connectionManager.start();
                connectionManager.queueRequestBlocking(new CreateTableRequest());
        }

        public void onDisable() {
                connectionManager.stop();
        }

        public void setSkillPoints(String player, SkillType skillType, int points) {
                boolean ret;
                ret = connectionManager.queueRequest(new SetSkillPointsRequest(player, skillType, points));
                if (!ret) {
                        plugin.getLogger().warning("Settings " + skillType.getName() + " to " + points + " for " + player + " failed!");
                }
        }

        public void addSkillPoints(String player, SkillType skillType, int points) {
                boolean ret;
                ret = connectionManager.queueRequest(new AddSkillPointsRequest(player, skillType, points));
                if (!ret) {
                        plugin.getLogger().warning("Adding " + points + " to " + player + " for " + skillType.getName().toLowerCase() + " failed!");
                }
        }

        public void loadPlayerInfo(PlayerInfo playerInfo) {
                boolean ret;
                ret = connectionManager.queueRequest(new LoadPlayerInfoRequest(plugin, playerInfo));
                if (!ret) {
                        plugin.getLogger().warning("Loading player info of " + playerInfo.getName() + " failed!");
                }
        }

        public void saveSkillInfo(AbstractSkill skill) {
                connectionManager.queueRequest(new SaveSkillInfoRequest(skill));
        }
}
