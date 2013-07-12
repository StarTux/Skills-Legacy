package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.skill.AbstractSkill;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTableRequest implements SQLRequest {
        public CreateTableRequest() {}

        @Override
        public void execute(Connection c) throws SQLException {
                Statement s;
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_sp` (" +
                          " `player` VARCHAR(16) NOT NULL," +
                          " `skill` VARCHAR(16) NOT NULL," +
                          " `points` INT(11) NOT NULL," +
                          " PRIMARY KEY (`player`, `skill`)" +
                          ") ENGINE=MyISAM");
                s.close();

                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_gaussian` (" +
                          " `points` INT(7) NOT NULL," +
                          " `level` INT(4) NOT NULL," +
                          " PRIMARY KEY(`points`)" +
                          ") ENGINE=MyISAM");
                s.close();
                for (int i = 0; i < AbstractSkill.MAX_LEVEL; ++i) {
                        int sp = AbstractSkill.gaussian(i);
                        s = c.createStatement();
                        s.execute("INSERT IGNORE INTO `skills_gaussian` (`points`, `level`) VALUES (" + sp + ", " + i + ")");
                        s.close();
                }

                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_skills` (" +
                          " `skill` VARCHAR(16) NOT NULL," +
                          " `title` VARCHAR(32) NOT NULL," +
                          " `description` VARCHAR(256) NOT NULL," +
                          " PRIMARY KEY(`skill`)" +
                          ") ENGINE=MyISAM");
                s.close();
        }
}
