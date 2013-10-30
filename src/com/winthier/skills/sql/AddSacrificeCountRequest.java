package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.bukkit.Material;

public class AddSacrificeCountRequest implements SQLRequest {
        private final Material mat;
        private final int count;

        public AddSacrificeCountRequest(Material mat, int count) {
                this.mat = mat;
                this.count = count;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("INSERT INTO `skills_sacrifice`" +
                                       " (`material`, `count`)" +
                                       " VALUES (?, ?)" +
                                       " ON DUPLICATE KEY" +
                                       " UPDATE `count` = `count` + ?");
                final String name = mat.name().toLowerCase();
                s.setString(1, name);
                s.setInt(2, count);
                s.setInt(3, count);
                s.execute();
                s.close();
        }
}
