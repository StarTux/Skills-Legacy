package com.winthier.skills.util;

import com.winthier.skills.SkillsPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An instance of this will assist in protecting the outcome of
 * various magic spells to avoid damage and exploitation.
 */
public class MagicWatchdog implements Listener {
        private final SkillsPlugin plugin;
        private BukkitRunnable runnable = null;

        // Things to watch.
        private final List<Entity> rides = new ArrayList<Entity>();
        private boolean blockLightningFire = false;

        public MagicWatchdog(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void start() {
                if (runnable != null) return;

                final MagicWatchdog inst = this;
                runnable = new BukkitRunnable() {
                        public void run() {
                                inst.run();
                        }
                };
                runnable.runTaskTimer(plugin, 0L, 0L);
        }

        public void stop() {
                if (runnable == null) return;
                try {
                        runnable.cancel();
                } catch (IllegalStateException ise) {}
                runnable = null;
        }

        public void tryStop() {
                if (rides.isEmpty()) stop();
        }

        public void run() {
                // Watch rides.
                List<Entity> removeRides = new ArrayList<Entity>();
                for (Entity ride : rides) {
                        if (!ride.isValid() || ride.getPassenger() == null) {
                                removeRides.add(ride);
                                ride.remove();
                        }
                }
                rides.removeAll(removeRides);

                // Stop if nothing's left to watch.
                tryStop();
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
                final Entity ride = event.getPlayer().getVehicle();
                if (ride != null && rides.contains(ride)) {
                        ride.remove();
                        rides.remove(ride);
                        tryStop();
                }
        }

        @EventHandler
        public void onBlockIgnite(BlockIgniteEvent event) {
                if (!blockLightningFire) return;
                if (event.getCause() != BlockIgniteEvent.IgniteCause.LIGHTNING) return;
                event.setCancelled(true);
        }

        public void addRide(Entity entity) {
                rides.add(entity);
                start();
        }

        public void setBlockLightningFire(boolean v) {
                blockLightningFire = v;
        }
}
