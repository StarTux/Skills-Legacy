package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadPlayerInfoRequest extends BukkitRunnable implements SQLRequest {
        private final SkillsPlugin plugin;
        private final PlayerInfo playerInfo;
        private final int[] points;

        public LoadPlayerInfoRequest(SkillsPlugin plugin, PlayerInfo playerInfo) {
                this.plugin = plugin;
                this.playerInfo = playerInfo;
                points = new int[SkillType.values().length];
                for (int i = 0; i < points.length; ++i) points[i] = 0;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("SELECT `skill`, `points` FROM `skills_sp`" +
                                       " WHERE `player` = ?");
                s.setString(1, playerInfo.getName());
                ResultSet result = s.executeQuery();
                while (result.next()) {
                        SkillType skillType;
                        skillType = SkillType.fromString(result.getString("skill"));
                        if (skillType == null) continue;
                        points[skillType.ordinal()] = result.getInt("points");
                }
                s.close();
                runTask(plugin);
        }

        @Override
        public void run() {
                for (int i = 0; i < points.length; ++i) {
                        playerInfo.setSkillPointsBare(SkillType.values()[i], points[i]);
                }
        }
}
