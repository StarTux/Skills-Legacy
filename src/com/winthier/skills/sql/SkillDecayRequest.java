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
                                       " FROM `skills_sp`" +
                                       " WHERE `last_levelup` < DATE_SUB(NOW(), INTERVAL ? DAY)" +
                                       " GROUP BY `player`");
                s.setInt(1, days);
                ResultSet result = s.executeQuery();
                while (result.next()) {
                        players.add(result.getString("player"));
                }
                s.close();
                // Reduce skill points
                s = c.prepareStatement(" UPDATE `skills_sp`" +
                                       " SET `points` = FLOOR(? * `points`)" +
                                       " WHERE `last_levelup` < DATE_SUB(NOW(), INTERVAL ? DAY)");
                s.setDouble(1, percentage);
                s.setInt(2, days);
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
