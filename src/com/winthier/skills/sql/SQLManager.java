package com.winthier.skills.sql;

import com.winthier.libsql.ConnectionManager;
import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.spell.AbstractSpell;
import java.util.Collection;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

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

        public void sendPlayerStatistics(CommandSender sender, String player) {
                connectionManager.queueRequest(new PlayerStatisticsRequest(plugin, sender, player));
        }

        public void sendHighscore(CommandSender sender, SkillType skill, int page) {
                connectionManager.queueRequest(new SendHighscoreRequest(plugin, sender, skill, page));
        }

        public void loadHighscoreScoreboard(SkillType skill) {
                connectionManager.queueRequest(new HighscoreScoreboardRequest(plugin, skill));
        }

        public void sendTotalHighscore(CommandSender sender, int page) {
                connectionManager.queueRequest(new SendTotalHighscoreRequest(plugin, sender, page));
        }

        public void loadTotalHighscoreScoreboard() {
                connectionManager.queueRequest(new TotalHighscoreScoreboardRequest(plugin));
        }

        public void saveSkillInfo(AbstractSkill skill) {
                connectionManager.queueRequest(new SaveSkillInfoRequest(skill));
        }

        public boolean setSpellLevel(String player, AbstractSpell spell, int level) {
                return connectionManager.queueRequest(new SetSpellLevelRequest(player, spell.getName(), level));
        }

        public boolean setSpellActive(String player, AbstractSpell spell, boolean active) {
                return connectionManager.queueRequest(new SetSpellActiveRequest(player, spell.getName(), active));
        }

        public void switchElement(PlayerInfo info, ElementType element) {
                connectionManager.queueRequest(new SwitchElementRequest(plugin, info, element, plugin.switchElementCooldown));
        }

        public void addSacrificeCount(Material mat, int count) {
                connectionManager.queueRequest(new AddSacrificeCountRequest(mat, count));
        }

        public void updateTotalSkillLevel(String... players) {
                connectionManager.queueRequest(new UpdateTotalLevelRequest(players));
        }

        public void updateTotalSkillLevel(Collection<String> players) {
                connectionManager.queueRequest(new UpdateTotalLevelRequest(players));
        }

        public void updateAllTotalSkillLevels() {
                connectionManager.queueRequest(new UpdateAllTotalLevelsRequest());
        }

        public void updateLastLevelup(String player, SkillType skill) {
                connectionManager.queueRequest(new UpdateLastLevelupRequest(player, skill));
        }

        public void decaySkills(int days, double percentage) {
                connectionManager.queueRequest(new SkillDecayRequest(plugin, days, percentage));
        }

        public void checkSkillDecay(int days, double percentage) {
                connectionManager.queueRequest(new CheckSkillDecayRequest(plugin, days, percentage));
        }
}
