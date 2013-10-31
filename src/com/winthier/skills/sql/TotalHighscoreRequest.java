package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.SkillsPlugin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class TotalHighscoreRequest extends BukkitRunnable implements SQLRequest {
        protected final SkillsPlugin plugin;
        protected final int page;
        protected final int pageLength;
        // Results
        protected int count;
        protected final String[] names;
        protected final int[] levels;

        public TotalHighscoreRequest(SkillsPlugin plugin, int page, int pageLength) {
                names = new String[pageLength];
                levels = new int[pageLength];
                this.plugin = plugin;
                this.page = page;
                this.pageLength = pageLength;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;

                // Build statement.
                StringBuilder sb = new StringBuilder();
                //s = c.prepareStatement("SELECT player, skill, SUM((SELECT MAX(level) FROM skills_gaussian AS g WHERE g.points <= sp.points)) AS level FROM skills_sp AS sp GROUP BY player ORDER BY level DESC LIMIT ?, ?");
                s = c.prepareStatement(" SELECT `player`, `level`" +
                                       " FROM skills_total" +
                                       " ORDER BY `level` DESC" +
                                       " LIMIT ?, ?");
                s.setInt(1, page * pageLength);
                s.setInt(2, pageLength);

                // Execute and evaluate.
                ResultSet result = s.executeQuery();
                for (count = 0; count < pageLength; ++count) {
                        if (!result.next()) break;
                        names[count] = result.getString("player");
                        levels[count] = result.getInt("level");
                }
                s.close();


                runTask(plugin);
        }
}
