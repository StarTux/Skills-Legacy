package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SaveSkillInfoRequest implements SQLRequest {
        private final SkillType skillType;
        private final String description;

        public SaveSkillInfoRequest(AbstractSkill skill) {
                this.skillType = skill.getSkillType();
                this.description = skill.getDescription();
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement(
                        "INSERT INTO `skills_skills` (`skill`, `title`, `description`)" +
                        " VALUES(?, ?, ?)" +
                        " ON DUPLICATE KEY UPDATE" +
                        " `title` = ?," +
                        " `description` = ?");
                int i = 1;
                s.setString(i++, skillType.getName());
                s.setString(i++, skillType.getDisplayName());
                s.setString(i++, description);
                s.setString(i++, skillType.getDisplayName());
                s.setString(i++, description);
                s.execute();
                s.close();
        }
}
