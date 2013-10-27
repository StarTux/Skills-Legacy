package com.winthier.skills.spell;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.Util;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VelocitySpell extends AbstractSpell {
        private double[] amplifiers;
        private int[] durations;
        public static Map<Player, VelocityTask> tasks = new WeakHashMap<Player, VelocityTask>();

        @Override
        public boolean cast(Player player) {
                final int level = getLevel(player);

                final double amplifier = amplifiers[level - 1];
                final int duration = durations[level - 1];

                if (null != tasks.get(player)) return false;
                final Vector vector = player.getLocation().getDirection().normalize().multiply(amplifier);
                VelocityTask task = new VelocityTask(player, vector, duration);
                tasks.put(player, task);
                task.start(plugin);

                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                final double amplifier = amplifiers[level - 1];
                final int duration = durations[level - 1];
                return String.format("Strength %.2f for %d ticks.", amplifier, duration);
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                // Load amplifiers.
                amplifiers = Util.toDoubleArray(config.getDoubleList("Amplifiers"));
                if (maxLevel != amplifiers.length) {
                        logWarning("Amplifiers don't match max level.");
                        return false;
                }

                // Load durations.
                durations = Util.toIntArray(config.getIntegerList("Durations"));
                if (maxLevel != durations.length) {
                        logWarning("Durations don't match max level.");
                        return false;
                }

                return true;
        }
}

class VelocityTask extends BukkitRunnable {
        private final Player player;
        private final Vector vector;
        private final int duration;
        // Counter
        private int lifetime = 0;

        public VelocityTask(Player player, Vector vector, int duration) {
                this.player = player;
                this.vector = vector;
                this.duration = duration;
        }

        public void start(SkillsPlugin plugin) {
                runTaskTimer(plugin, 0L, 1L);
        }

        @Override
        public void run() {
                lifetime += 1;
                if (lifetime > duration) {
                        VelocitySpell.tasks.remove(player);
                        try {
                                cancel();
                        } catch (IllegalArgumentException iae) {
                                // Do nothing.
                        }
                }

                // Add the speedup.
                Entity entity = player;
                while (entity.getVehicle() != null) {
                        entity = entity.getVehicle();
                }
                if (entity.getType() == EntityType.FIREWORK) return;
                Vector v = vector;
                if (entity != player) {
                        v = v.clone();
                        v.setY(0.0);
                        v = v.multiply(0.5);
                }

                Vector velocity = entity.getVelocity();
                velocity = velocity.add(v);
                entity.setVelocity(velocity);

                if (lifetime == 4) {
                        player.playSound(player.getLocation(), "fireworks.launch", 0.5f, 1.0f);
                }
        }
}
