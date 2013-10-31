package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateTotalLevelRequest implements SQLRequest {
        private final String player;

        public UpdateTotalLevelRequest(String player) {
                this.player = player;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement(" REPLACE INTO skills_total " +
                                       " SELECT ? AS `player`, SUM((" +
                                       "   SELECT MAX(`g`.`level`)" +
                                       "   FROM `skills_gaussian` AS `g`" +
                                       "   WHERE `g`.`points` <= `sp`.`points`" +
                                       " )) AS `sum`" +
                                       " FROM `skills_sp` AS `sp`" +
                                       " WHERE `player` = ?");
                s.setString(1, player);
                s.setString(2, player);
                s.execute();
                s.close();
        }
}
