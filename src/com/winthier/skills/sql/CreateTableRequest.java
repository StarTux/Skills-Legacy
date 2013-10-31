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

                // Create table for players.
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_players` (" +
                          " `player` VARCHAR(16) NOT NULL," +
                          " `element` VARCHAR(16) DEFAULT NULL," +
                          " `last-switch` DATETIME DEFAULT NULL," +
                          " PRIMARY KEY(`player`)" +
                          ") ENGINE=MyISAM");
                s.close();

                // Create table for skill points.
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_sp` (" +
                          " `player` VARCHAR(16) NOT NULL," +
                          " `skill` VARCHAR(16) NOT NULL," +
                          " `points` INT(11) UNSIGNED NOT NULL," +
                          " PRIMARY KEY (`player`, `skill`)" +
                          ") ENGINE=MyISAM");
                s.close();

                // Create total skill level table.
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_total` (" +
                          " `player` VARCHAR(16) NOT NULL," +
                          " `level` INT(11) UNSIGNED NOT NULL," +
                          " PRIMARY KEY (`player`)" +
                          ") ENGINE=MyISAM");
                s.close();

                // Create table for gauss numbers.
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_gaussian` (" +
                          " `points` INT(8) UNSIGNED NOT NULL," +
                          " `level` SMALLINT(4) UNSIGNED NOT NULL," +
                          " PRIMARY KEY(`points`)" +
                          ") ENGINE=MyISAM");
                s.close();
                for (int i = 0; i < AbstractSkill.MAX_LEVEL; ++i) {
                        int sp = AbstractSkill.gaussian(i);
                        s = c.createStatement();
                        s.execute("INSERT IGNORE INTO `skills_gaussian` (`points`, `level`) VALUES (" + sp + ", " + i + ")");
                        s.close();
                }

                // s = c.createStatement();
                // s.execute("CREATE TABLE IF NOT EXISTS `skills_skills` (" +
                //           " `skill` VARCHAR(16) NOT NULL," +
                //           " `title` VARCHAR(32) NOT NULL," +
                //           " `description` VARCHAR(256) NOT NULL," +
                //           " PRIMARY KEY(`skill`)" +
                //           ") ENGINE=MyISAM");
                // s.close();

                // Create table for spell levels.
                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_spells` (" +
                          " `player` VARCHAR(16) NOT NULL," +
                          " `spell` VARCHAR(32) NOT NULL," +
                          " `level` TINYINT(2) UNSIGNED NOT NULL," +
                          " `active` BOOL NOT NULL DEFAULT TRUE," +
                          " PRIMARY KEY(`player`, `spell`)" +
                          ") ENGINE=MyISAM");
                s.close();

                s = c.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS `skills_sacrifice` (" +
                          " `material` VARCHAR(32) NOT NULL," +
                          " `count` INT(11) UNSIGNED DEFAULT 0," +
                          " PRIMARY KEY (`material`)" +
                          ") ENGINE=MyISAM");
                s.close();
        }
}
