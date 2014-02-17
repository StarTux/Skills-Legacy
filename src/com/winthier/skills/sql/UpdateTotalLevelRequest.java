package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class UpdateTotalLevelRequest implements SQLRequest {
        private final String[] players;

        public UpdateTotalLevelRequest(String... players) {
                this.players = players;
        }

        public UpdateTotalLevelRequest(Collection<String> players) {
                this.players = players.toArray(new String[0]);
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                StringBuilder sb;
                sb = new StringBuilder(" REPLACE INTO skills_total " +
                                       " SELECT `player`, SUM((" +
                                       "   SELECT MAX(`g`.`level`)" +
                                       "   FROM `skills_gaussian` AS `g`" +
                                       "   WHERE `g`.`points` <= `sp`.`points`" +
                                       " )) AS `level`" +
                                       " FROM `skills_sp` AS `sp`" +
                                       " WHERE `player` in (?");
                for (int i = 0; i < players.length - 1; ++i) {
                        sb.append(", ?");
                }
                sb.append(") GROUP BY `player`");
                s = c.prepareStatement(sb.toString());
                int i = 1;
                for (String player : players) {
                        s.setString(i++, player);
                }
                s.execute();
                s.close();
        }
}
