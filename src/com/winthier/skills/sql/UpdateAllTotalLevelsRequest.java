package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateAllTotalLevelsRequest implements SQLRequest {
        public UpdateAllTotalLevelsRequest() {
        }

        @Override
        public void execute(Connection c) throws SQLException {
                Statement s = c.createStatement();
                s.execute(" REPLACE INTO skills_total " +
                          " SELECT `player`, SUM((" +
                          "   SELECT MAX(`g`.`level`)" +
                          "   FROM `skills_gaussian` AS `g`" +
                          "   WHERE `g`.`points` <= `sp`.`points`" +
                          " )) AS `sum`" +
                          " FROM `skills_sp` AS `sp`" +
                          " GROUP BY `player`");
                 s.close();
        }
}
