package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateLastLevelupRequest implements SQLRequest {
        private final String player;
        private final SkillType skillType;

        public UpdateLastLevelupRequest(String player, SkillType skillType) {
                this.player = player;
                this.skillType = skillType;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("UPDATE IGNORE `skills_sp`" +
                                       " SET `last_levelup` = NOW()" +
                                       " WHERE `player` = ?" +
                                       " AND `skill` = ?");
                s.setString(1, player);
                s.setString(2, skillType.getName());
                s.execute();
                s.close();
        }
}
