package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import java.util.Collection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class ClearPotionEffectsSpell extends AbstractSpell {
        @Override
        public boolean cast(Player player) {
                return clear(player);
                
        }

        @Override
        public boolean cast(Player player, Entity entity) {
                // When another entity is affected, we better check if this is prohibited.
                if (!Util.canHurt(plugin, player, entity)) return false;
                if (!(entity instanceof LivingEntity)) return false;
                return clear((LivingEntity)entity);
        }

        public boolean clear(LivingEntity living) {
                final Collection<PotionEffect> active = living.getActivePotionEffects();
                if (active.isEmpty()) return false;
                for (PotionEffect effect : active) {
                        living.removePotionEffect(effect.getType());
                }
                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                return null;
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                return true;
        }
}
