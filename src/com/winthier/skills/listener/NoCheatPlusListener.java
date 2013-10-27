package com.winthier.skills.listener;

import com.winthier.skills.SkillsPlugin;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;
import fr.neatmonster.nocheatplus.hooks.NCPHook;
import fr.neatmonster.nocheatplus.hooks.NCPHookManager;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoCheatPlusListener implements Listener, NCPHook {
        private final SkillsPlugin plugin;
        private final Map<Player, Long> flyExemptions = new WeakHashMap<Player, Long>();

        public NoCheatPlusListener(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                NCPHookManager.addHook(CheckType.MOVING_SURVIVALFLY, this);
        }

        public void onDisable() {
                NCPHookManager.removeHook(this);
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
                final Player player = event.getPlayer();
                final Entity vehicle = player.getVehicle();
                if (vehicle == null || vehicle.getType() != EntityType.FIREWORK) return;

                flyExemptions.put(player, System.currentTimeMillis() + 1000);
        }

        @Override
        public String getHookName() {
                return "Strings";
        }
        
        @Override
        public String getHookVersion() {
                return "0.1";
        }

        @Override
        public boolean onCheckFailure(CheckType checkType, Player player, IViolationInfo info) {
                Long exempted = flyExemptions.get(player);
                if (exempted == null || exempted < System.currentTimeMillis()) return false;
                return true;
        }
}
