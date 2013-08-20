package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetSpellActiveRequest implements SQLRequest {
        private final String player;
        private final String spell;
        private final boolean active;

        public SetSpellActiveRequest(String player, String spell, boolean active) {
                this.player = player;
                this.spell = spell;
                this.active = active;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("UPDATE `skills_spells`" +
                                       " SET `active` = ?" +
                                       " WHERE `player` = ?" +
                                       " AND `spell` = ?");
                s.setBoolean(1, active);
                s.setString(2, player);
                s.setString(3, spell);
                s.executeUpdate();
                s.close();
        }
}
