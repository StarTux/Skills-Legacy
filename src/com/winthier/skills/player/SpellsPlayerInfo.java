package com.winthier.skills.player;

import com.winthier.skills.ElementType;
import com.winthier.skills.spell.AbstractSpell;
import java.util.HashMap;
import java.util.Map;

/**
 * Store all spells that this player has unlocked.
 */
public class SpellsPlayerInfo {
        private final PlayerInfo player;
        private final Map<String, SpellInfo> spells = new HashMap<String, SpellInfo>();
        
        public SpellsPlayerInfo(PlayerInfo player) {
                this.player = player;
        }

        public boolean hasSpell(AbstractSpell spell) {
                return spells.keySet().contains(spell.getName());
        }

        public int getSpellLevel(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return 0;
                return info.level;
        }

        public void addSpell(AbstractSpell spell) {
                if (hasSpell(spell)) return;
                spells.put(spell.getName(), new SpellInfo());
        }

        public void setSpellLevel(AbstractSpell spell, int level) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) {
                        info = new SpellInfo();
                        spells.put(spell.getName(), info);
                }
                info.level = level;
        }

        public void setActive(AbstractSpell spell, boolean active) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return;
                info.active = active;
        }

        public void toggleActive(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return;
                info.active = !info.active;
        }

        public boolean isActive(AbstractSpell spell) {
                SpellInfo info = spells.get(spell.getName());
                if (info == null) return false;
                return info.active;
        }
}

class SpellInfo {
        public int level = 1;
        public boolean active = true;
}
