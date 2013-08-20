package com.winthier.skills.player;

import com.winthier.skills.SkillsPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerManager implements Listener {
        public final SkillsPlugin plugin;
        private final Map<String, PlayerInfo> players = new HashMap<String, PlayerInfo>();

        public PlayerManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                        getPlayerInfo(player).load();
                }
        }

        public void onDisable() {
                players.clear();
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
                final Player player = event.getPlayer();
                getPlayerInfo(player).onJoin(player);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
                final Player player = event.getPlayer();
                getPlayerInfo(player).onQuit(player);
        }

        /**
         * Return a player info for a player name, if there is one
         * in memory.
         */
        public PlayerInfo getPlayerInfo(String playerName) {
                return players.get(playerName);
        }

        public PlayerInfo getPlayerInfo(Player player) {
                final String playerName = player.getName();
                PlayerInfo info = players.get(playerName);
                if (info == null) {
                        info = new PlayerInfo(plugin, player);
                        players.put(playerName, info);
                }
                return info;
        }

        /**
         * Remove a player information from the cache.
         */
        public void remove(String playerName) {
                PlayerInfo info = players.remove(playerName);
                if (info != null) info.onRemoval();
        }
}
