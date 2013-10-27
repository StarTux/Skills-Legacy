package com.winthier.skills.sql;

import com.winthier.libsql.SQLRequest;
import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.skill.SkillType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadPlayerInfoRequest extends BukkitRunnable implements SQLRequest {
        private final SkillsPlugin plugin;
        private final PlayerInfo playerInfo;
        private final String name;
        // Results
        private final int[] points;
        private final Map<String, SpellInfo> spells = new HashMap<String, SpellInfo>();
        private ElementType primaryElement = null;

        public LoadPlayerInfoRequest(SkillsPlugin plugin, PlayerInfo playerInfo) {
                this.plugin = plugin;
                this.playerInfo = playerInfo;
                this.name = playerInfo.getName();
                points = new int[SkillType.values().length];
                for (int i = 0; i < points.length; ++i) points[i] = 0;
        }

        @Override
        public void execute(Connection c) throws SQLException {
                PreparedStatement s;
                ResultSet result;

                // Load skill points.
                s = c.prepareStatement("SELECT `skill`, `points` FROM `skills_sp`" +
                                       " WHERE `player` = ?");
                s.setString(1, name);
                result = s.executeQuery();
                while (result.next()) {
                        SkillType skillType;
                        skillType = SkillType.fromString(result.getString("skill"));
                        if (skillType == null) continue;
                        points[skillType.ordinal()] = result.getInt("points");
                }
                s.close();

                // Load spell information.
                s = c.prepareStatement("SELECT `spell`, `level`, `active` FROM `skills_spells`" +
                                       " WHERE `player` = ?");
                s.setString(1, name);
                result = s.executeQuery();
                while (result.next()) {
                        final String spell = result.getString("spell");
                        final int level = result.getInt("level");
                        final boolean active = result.getBoolean("active");
                        spells.put(spell, new SpellInfo(level, active));
                }
                s.close();

                // Load player information.
                // Currently, this is only the primary element.
                s = c.prepareStatement("SELECT `element` FROM `skills_players` WHERE `player` = ?");
                s.setString(1, name);
                result = s.executeQuery();
                if (result.next()) {
                        String elem = result.getString("element");
                        if (elem != null) {
                                primaryElement = ElementType.fromString(elem);
                        }
                }
                s.close();

                // Setup player entry.
                // This is not really a loading routine, but doing
                // this will for now always coincide with loading
                // player data, so let's have it stay here for
                // now. Keep in mind to put it elsewhere if things
                // change.
                s = c.prepareStatement("INSERT IGNORE INTO `skills_players` SET `player` = ?");
                s.setString(1, name);
                s.execute();
                s.close();

                runTask(plugin);
        }

        @Override
        public void run() {
                for (int i = 0; i < points.length; ++i) {
                        playerInfo.setSkillPointsBare(SkillType.values()[i], points[i]);
                }
                for (Map.Entry<String, SpellInfo> entry : spells.entrySet()) {
                        final String spellName = entry.getKey();
                        final SpellInfo spellInfo = entry.getValue();
                        playerInfo.spellsInfo.setSpell(spellName, spellInfo.level, spellInfo.active);
                }
                if (primaryElement != null) playerInfo.setPrimaryElement(primaryElement);

                playerInfo.setLoaded(true);
        }
}

class SpellInfo {
        public final int level;
        public final boolean active;
        public SpellInfo(int level, boolean active) {
                this.level = level;
                this.active = active;
        }
}
