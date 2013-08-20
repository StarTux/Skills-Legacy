package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ArrowSpell extends AbstractSpell {
        double velocities[];

        /**
         * Create the arrow and set velocity and shooter.
         *
         * Since Bukkit doesn't support setting whether a stuck
         * arrow can be picked up, we have to go through this most
         * painful method.
         */
        @Override
        public boolean cast(Player player) {
                final int level = getLevel(player);
                final Location loc = player.getEyeLocation().clone();
                final Vector dir = loc.getDirection().normalize();
                loc.add(dir);
                Arrow arrow = (Arrow)player.getWorld().spawnEntity(loc, EntityType.ARROW);
                //Arrow arrow = player.launchProjectile(Arrow.class);
                if (arrow == null) return false;
                arrow.setShooter(player);
                arrow.getLocation().setPitch(loc.getPitch());
                arrow.getLocation().setYaw(loc.getYaw());
                arrow.setVelocity(dir.multiply(velocities[level - 1]));
                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                final double velocity = velocities[level - 1];
                return Util.format("Speed %.2f", velocity);
        }
        
        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                velocities = Util.toDoubleArray(config.getDoubleList("Velocities"));
                if (velocities.length != maxLevel) {
                        logWarning("Velocities don't match max level.");
                        return false;
                }
                return true;
        }
}
