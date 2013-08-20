package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetSpellLevelRequest implements SQLRequest {
        private final String player;
        private final String spell;
        private final int level;

        public SetSpellLevelRequest(String player, String spell, int level) {
                this.player = player;
                this.spell = spell;
                this.level = level;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("INSERT INTO `skills_spells`" +
                                       " (`player`, `spell`, `level`)" +
                                       " VALUES (?, ?, ?)" +
                                       " ON DUPLICATE KEY" +
                                       " UPDATE `level` = ?");
                s.setString(1, player);
                s.setString(2, spell);
                s.setInt(3, level);
                s.setInt(4, level);
                s.execute();
                s.close();
        }
}
