package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EntityRideSpell extends AbstractSpell {
        private EntityType entityType;

        @Override
        public boolean cast(Player player) {
                if (player.isInsideVehicle()) return false;

                // Check if the location is valid (< skylimit).
                final Location loc = player.getLocation();
                if (loc.getY() >= 256.0) return false;

                // Try to spawn the entity.
                final Entity entity = loc.getWorld().spawnEntity(loc, entityType);
                if (entity == null) return false;
                entity.setVelocity(player.getVelocity());
                plugin.magicWatchdog.addRide(entity);

                // Mount it.
                entity.setPassenger(player);

                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                return null;
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                entityType = Util.enumFromString(EntityType.class, config.getString("EntityType"));
                if (entityType == null) {
                        logWarning("EntityType missing or unknown.");
                        return false;
                }

                return true;
        }
}
