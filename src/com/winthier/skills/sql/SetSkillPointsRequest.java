package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetSkillPointsRequest implements SQLRequest {
        private final String player;
        private final SkillType skillType;
        private final int points;

        public SetSkillPointsRequest(String player, SkillType skillType, int points) {
                this.player = player;
                this.skillType = skillType;
                this.points = points;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("INSERT INTO `skills_sp`" +
                                       " (`player`, `skill`, `points`)" +
                                       " VALUES (?, ?, ?)" +
                                       " ON DUPLICATE KEY" +
                                       " UPDATE `points` = ?");
                s.setString(1, player);
                s.setString(2, skillType.getName());
                s.setInt(3, points);
                s.setInt(4, points);
                s.execute();
                s.close();
        }
}
