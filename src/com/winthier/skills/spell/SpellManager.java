package com.winthier.skills.spell;

import com.winthier.skills.SkillsPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpellManager implements Listener {
        private final SkillsPlugin plugin;
        private final Map<String, AbstractSpell> spellMap = new HashMap<String, AbstractSpell>();

        public SpellManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                loadSpells();
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void onDisable() {
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteract(PlayerInteractEvent event) {
                
        }

        public void loadSpells() {
                // TODO
        }
}
