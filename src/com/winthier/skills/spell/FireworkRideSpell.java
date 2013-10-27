package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkRideSpell extends AbstractSpell {
        private int[] powers;

        @Override
        public boolean cast(Player player) {
                if (player.isInsideVehicle()) return false;
                final int level = getLevel(player);

                // Check if the location is valid (< skylimit).
                final Location loc = player.getLocation();
                if (loc.getY() >= 256.0) return false;

                // Try to spawn the entity.
                final Firework firework = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                if (firework == null) return false;
                plugin.magicWatchdog.addRide(firework);

                // Set the power.
                final FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(powers[level - 1]);
                firework.setFireworkMeta(meta);
                
                // Mount it.
                firework.setPassenger(player);

                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                return "Firework power: " + powers[level - 1];
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                powers = Util.toIntArray(config.getIntegerList("Powers"));
                if (maxLevel != powers.length) {
                        logWarning("Powers don't match max level.");
                        return false;
                }

                return true;
        }
}
