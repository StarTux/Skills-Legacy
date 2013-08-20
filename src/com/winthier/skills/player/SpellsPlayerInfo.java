package com.winthier.skills.player;

import com.winthier.skills.ElementType;
import com.winthier.skills.spell.AbstractSpell;
import java.util.HashMap;
import java.util.Map;

/**
 * Store all spells that this player has unlocked.  Most of these
 * functions will be called by AbstractSpell.
 */
public class SpellsPlayerInfo {
        private final PlayerInfo player;
        private final Map<String, SpellInfo> spells = new HashMap<String, SpellInfo>();
        
        public SpellsPlayerInfo(PlayerInfo player) {
                this.player = player;
        }

        // Loading.

        /**
         * Set, and if necessary override, all information
         * associated with a spell for this player.
         * This is called by LoadPlayerInfoRequest to load it from
         * the database.
         * In contrast to the other setters, this doesn't save to
         * the database.
         */
        public void setSpell(String spellName, int level, boolean active) {
                AbstractSpell spell = player.plugin.spellManager.getSpell(spellName);
                if (spell == null) {
                        player.plugin.getLogger().warning(player.getName() + " has unknown spell: " + spellName);
                        return;
                }
                if (level == 0) return;
                final int max = spell.getMaxLevel();
                if (level > max) {
                        player.plugin.getLogger().warning(player.getName() + " has too high level for spell " + spellName + ": " + level + ", maximum: " + max);
                        level = max;
                }
                spells.put(spellName, new SpellInfo(level, active));
        }

        // Unlocked.

        public boolean hasSpell(AbstractSpell spell) {
                return spells.keySet().contains(spell.getName());
        }

        // public void addSpell(AbstractSpell spell) {
        //         if (hasSpell(spell)) return;
        //         spells.put(spell.getName(), new SpellInfo());
        // }

        // Level.

        public int getSpellLevel(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return 0;
                return info.level;
        }

        public void setSpellLevel(AbstractSpell spell, int level) {
                SpellInfo info = spells.get(spell.getName());

                // Save to database.
                if (info == null || level != info.level) {
                        player.plugin.sqlManager.setSpellLevel(player.getName(), spell, level);
                }

                if (info == null) {
                        info = new SpellInfo();
                        spells.put(spell.getName(), info);
                }

                info.level = level;
        }

        // Active.

        public boolean isActive(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return false;
                return info.active;
        }

        public void setActive(AbstractSpell spell, boolean active) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return;

                // Save to database.
                if (active != info.active) {
                        player.plugin.sqlManager.setSpellActive(player.getName(), spell, active);
                }

                info.active = active;
        }

        public void toggleActive(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return;
                info.active = !info.active;

                // Save to database.
                player.plugin.sqlManager.setSpellActive(player.getName(), spell, info.active);
        }
}

class SpellInfo {
        public int level = 1;
        public boolean active = true;
        public SpellInfo() {}
        public SpellInfo(int level, boolean active) {
                this.level = level;
                this.active = active;
        }
}
