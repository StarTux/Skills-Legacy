package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

// " (SELECT max(level)" +
// "   FROM skills_gaussian AS g" +
// "   WHERE g.points <= sp.points" +
// " ) AS level" +
public class PlayerStatisticsRequest extends BukkitRunnable implements SQLRequest {
        private SkillsPlugin plugin;
        private CommandSender sender;
        private String player;
        // skill points per skill type
        private EnumIntMap<SkillType> skillPoints = new EnumIntMap<SkillType>(SkillType.class, 0);
        private EnumIntMap<SkillType> skillLevels = new EnumIntMap<SkillType>(SkillType.class, 0);
        // elemental skill points
        private EnumIntMap<ElementType> elements = new EnumIntMap<ElementType>(ElementType.class, 0);
        // total skill points
        private int total;

        public PlayerStatisticsRequest(SkillsPlugin plugin, CommandSender sender, String player) {
                this.plugin = plugin;
                this.sender = sender;
                this.player = player;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement(
                        "SELECT skill, points" +
                        " FROM skills_sp" +
                        " WHERE player = ?");
                s.setString(1, player);
                ResultSet result = s.executeQuery();
                while (result.next()) {
                        SkillType skillType = SkillType.fromString(result.getString("skill"));
                        if (skillType == null) continue;
                        final int points = result.getInt("points");
                        final int level = AbstractSkill.getLevelForSkillPoints(points);
                        skillPoints.set(skillType, points);
                        skillLevels.set(skillType, level);
                        total += level;
                }
                for (ElementType elem : ElementType.values()) {
                        int sum = 0;
                        SkillType skillTypes[] = elem.getSkills();
                        sum += skillPoints.get(skillTypes[0]) * 2;
                        sum += skillPoints.get(skillTypes[1]);
                        sum += skillPoints.get(skillTypes[2]);
                        sum /= 2;
                        elements.set(elem, AbstractSkill.getLevelForSkillPoints(sum));
                }
                s.close();
                runTask(plugin);
        }

        @Override
        public void run() {
                Util.sendMessage(sender, "&3=== &bSkill Statistics for %s&3 ===", player);

                Util.sendMessage(sender, "&3 &m    &b Elements &3&m    ", player);
                for (ElementType elem : ElementType.values()) {
                        Util.sendMessage(sender, " %s %s\u25A3 &b%s", Util.shadowZeros(elements.get(elem), 3, ChatColor.DARK_GRAY, ChatColor.WHITE), elem.getColor(), elem.getDisplayName());
                }

                Util.sendMessage(sender, "&3 &m    &b Skills &3&m    ", player);
                for (SkillType skillType : SkillType.values()) {
                        Util.sendMessage(sender, " %s %s\u25A3 &b%s", Util.shadowZeros(skillLevels.get(skillType), 3, ChatColor.DARK_GRAY, ChatColor.WHITE), skillType.getColor(), skillType.getDisplayName());
                }

                Util.sendMessage(sender, "&3 &m    &b Total &3&m    ", player);
                Util.sendMessage(sender, " %s &b\u25A3 Total Skill Level", Util.shadowZeros(total, 3, ChatColor.DARK_GRAY, ChatColor.WHITE));
        }
}
