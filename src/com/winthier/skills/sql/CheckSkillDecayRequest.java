package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckSkillDecayRequest implements SQLRequest {
        private final SkillsPlugin plugin;
        private final int days;
        private final double percentage;

        public CheckSkillDecayRequest(SkillsPlugin plugin, int days, double percentage) {
                this.plugin = plugin;
                this.days = days;
                this.percentage = percentage;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                Statement s;
                ResultSet result;
                // Check if there is an entry for today.
                s = c.createStatement();
                result = s.executeQuery(" SELECT * FROM `skills_dates`" +
                                        " WHERE `name` = 'decay'" +
                                        " AND `date` = CURRENT_DATE");
                boolean hasEntry = result.next();
                s.close();
                if (hasEntry) {
                        plugin.getLogger().info("Omitting skill decay until another day");
                        return;
                }
                plugin.getLogger().info("Triggering skill decay...");
                // Set entry for today
                s = c.createStatement();
                s.executeUpdate(" INSERT INTO `skills_dates`" +
                                " (`name`, `date`) VALUES('decay', CURRENT_DATE)" +
                                " ON DUPLICATE KEY" +
                                " UPDATE `date` = CURRENT_DATE");
                s.close();
                // Trigger skill decay
                new BukkitRunnable() {
                        public void run() {
                                plugin.sqlManager.decaySkills(days, percentage);
                        }
                }.runTask(plugin);
        }
}
