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
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerManager implements Listener {
        public final SkillsPlugin plugin;
        private final Map<String, PlayerInfo> players = new HashMap<String, PlayerInfo>();

        public PlayerManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                        getPlayerInfo(player);
                }
        }

        public void onDisable() {
                players.clear();
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event) {
                getPlayerInfo(event.getPlayer()).onJoin(event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
                final Player player = event.getPlayer();
                // clear data in 10 seconds if the player is still offline
                new BukkitRunnable() {
                        public void run() {
                                if (!player.isOnline()) {
                                        players.remove(player.getName());
                                }
                        }
                }.runTaskLater(plugin, 200L);
        }

        // public PlayerInfo getPlayerInfo(String playerName) {
        //         return players.get(playerName);
        // }

        public PlayerInfo getPlayerInfo(Player player) {
                final String playerName = player.getName();
                PlayerInfo info = players.get(playerName);
                if (info == null) {
                        info = new PlayerInfo(plugin, player);
                        players.put(playerName, info);
                        plugin.sqlManager.loadPlayerInfo(info);
                }
                return info;
        }
}
