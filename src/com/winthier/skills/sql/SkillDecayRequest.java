package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillDecayRequest implements SQLRequest {
        private final SkillsPlugin plugin;
        private final int days;
        private final double percentage;
        private final Set<String> players = new HashSet<String>();

        public SkillDecayRequest(SkillsPlugin plugin, int days, double percentage) {
                this.plugin = plugin;
                this.days = days;
                this.percentage = percentage;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                // Fetch player list
                s = c.prepareStatement(" SELECT `player`" +
                                       " FROM (" +
                                       "  SELECT `player`, MAX(`last_levelup`) AS date" +
                                       "  FROM `skills_sp`" +
                                       "  GROUP BY `player`" +
                                       " ) AS `tbl`" +
                                       " WHERE `date` < DATE_SUB(NOW(), INTERVAL ? DAY)");
                s.setInt(1, days);
                ResultSet result = s.executeQuery();
                while (result.next()) {
                        players.add(result.getString("player"));
                }
                s.close();
                if (players.isEmpty()) {
                        plugin.getLogger().info("Decayed 0 skills of 0 players.");
                        return;
                }
                // Reduce skill points
                StringBuilder sb = new StringBuilder();
                sb.append(" UPDATE `skills_sp`");
                sb.append(" SET `points` = FLOOR(? * `points`)");
                sb.append(" WHERE `player` IN (?");
                for (int i = 1; i < players.size(); ++i) sb.append(", ?");
                sb.append(")");
                s = c.prepareStatement(sb.toString());
                s.setDouble(1, percentage);
                int i = 2;
                for (String player : players) s.setString(i++, player);
                final int count = s.executeUpdate();
                plugin.getLogger().info("Decayed " + count + " skills of " + players.size() + " players.");
                // Update total skill levels
                new BukkitRunnable() {
                        public void run() {
                                plugin.sqlManager.updateTotalSkillLevel(players);
                        }
                }.runTask(plugin);
        }
}
