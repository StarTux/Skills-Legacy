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
        private final String title;
        private final String description;
        private final int skillPointFactor;

        public SaveSkillInfoRequest(AbstractSkill skill) {
                this.skillType = skill.getSkillType();
                this.title = skill.getTitle();
                List<String> description = skill.getDescription();
                if (description.isEmpty()) {
                        this.description = "";
                } else {
                        StringBuilder sb = new StringBuilder(description.get(0));
                        for (int i = 1; i < description.size(); ++i) sb.append("\n").append(description.get(i));
                        this.description = sb.toString();
                }
                this.skillPointFactor = skill.getSkillPointFactor();
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement(
                        "INSERT INTO `skills_skills` (`skill`, `title`, `description`, `point_factor`)" +
                        " VALUES(?, ?, ?, ?)" +
                        " ON DUPLICATE KEY UPDATE" +
                        " `title` = ?," +
                        " `description` = ?," +
                        " `point_factor` = ?");
                int i = 1;
                s.setString(i++, skillType.getName());
                s.setString(i++, title);
                s.setString(i++, description);
                s.setInt(i++, skillPointFactor);
                s.setString(i++, title);
                s.setString(i++, description);
                s.setInt(i++, skillPointFactor);
                s.execute();
                s.close();
        }
}
