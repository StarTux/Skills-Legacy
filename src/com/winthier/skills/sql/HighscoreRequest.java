package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class HighscoreRequest extends BukkitRunnable implements SQLRequest {
        protected final SkillsPlugin plugin;
        protected final SkillType skill;
        protected final int page;
        protected final int pageLength;
        // results
        protected int count;
        protected final String[] names;
        protected final int[] levels;

        public HighscoreRequest(SkillsPlugin plugin, SkillType skill, int page, int pageLength) {
                names = new String[pageLength];
                levels = new int[pageLength];
                this.plugin = plugin;
                this.skill = skill;
                this.page = page;
                this.pageLength = pageLength;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                StringBuilder sb = new StringBuilder();
                s = c.prepareStatement("SELECT `player`, `points` FROM `skills_sp` WHERE skill = ? ORDER BY points DESC LIMIT ?, ?");
                s.setString(1, skill.getName());
                s.setInt(2, page * pageLength);
                s.setInt(3, pageLength);
                ResultSet result = s.executeQuery();
                for (count = 0; count < pageLength; ++count) {
                        if (!result.next()) break;
                        names[count] = result.getString("player");
                        levels[count] = AbstractSkill.getLevelForSkillPoints(result.getInt("points"));
                }
                s.close();
                runTask(plugin);
        }

}
