package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.util.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SwitchElementRequest extends BukkitRunnable implements SQLRequest {
        private final SkillsPlugin plugin;
        private final PlayerInfo info;
        private final String name;
        private final ElementType element;
        private final int interval; // Element switch cooldown in minutes.
        // Result
        private int count = 0;
        private Date lastSwitch = null;

        public SwitchElementRequest(SkillsPlugin plugin, PlayerInfo info, ElementType element, int interval) {
                this.plugin = plugin;
                this.info = info;
                this.name = info.getName();
                this.element = element;
                this.interval = interval;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                s = c.prepareStatement("UPDATE `skills_players`" +
                                       " SET `element` = ?," +
                                       " `last-switch` = NOW()" +
                                       " WHERE `player` = ?" +
                                       " AND (`last-switch` IS NULL" +
                                       " OR `last-switch` <= DATE_SUB(NOW(), INTERVAL ? MINUTE))");
                s.setString(1, element.getName());
                s.setString(2, name);
                s.setInt(3, interval);
                count = s.executeUpdate();
                s.close();

                ResultSet result;
                if (count == 0) {
                        s = c.prepareStatement("SELECT `last-switch` FROM `skills_players` WHERE `player` = ?");
                        s.setString(1, name);
                        result = s.executeQuery();
                        if (result.next()) {
                                lastSwitch = result.getTimestamp("last-switch");
                        }
                }

                runTask(plugin);
        }

        @Override
        public void run() {
                final Player player = info.getPlayer();
                if (count == 0) {
                        if (lastSwitch == null) {
                                Util.sendMessage(player, "&cAn error occured.");
                        } else {
                                long time = System.currentTimeMillis() - lastSwitch.getTime(); // Elapsed time.
                                time /= 1000 * 60; // To minutes.
                                time = (long)interval - time; // Time left to wait.
                                if (time == 0l) {
                                        // Display 1 minute.
                                        Util.sendMessage(player, "&eYou have to wait another minute.");
                                } else if (time > 48l * 60l) {
                                        // Display days.
                                        time = (time - 1) / (24l * 60l) + 1;
                                        Util.sendMessage(player, "&eYou have to wait %d more days.", time);
                                } else if (time > 2l * 60l) {
                                        // Display hours.
                                        time = (time - 1) / 60l + 1;
                                        Util.sendMessage(player, "&eYou have to wait %d more hours.", time);
                                } else {
                                        // Display minutes.
                                        Util.sendMessage(player, "&eYou have to wait %d more minutes.", time);
                                }
                        }
                } else {
                        info.setPrimaryElement(element);
                        Util.sendMessage(player, "&b%s %s%s &3selected as your primary element.", element.getDisplayName(), element.getColor(), Util.ICON);
                }
        }
}
