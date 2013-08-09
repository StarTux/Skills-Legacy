package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectSpell extends AbstractSpell {
        private final PotionEffectType type;

        public PotionEffectSpell(SkillsPlugin plugin, ElementType element, String name, PotionEffectType type) {
                super(plugin, element, name);
                this.type = type;
        }

        public PotionEffect createPotionEffect(int level) {
                int duration = 1;
                if (!type.isInstant()) {
                        // 1 minute per 100 levels.
                        duration = (level * 60 * 20) / 100;
                }
                int amplifier = 0;
                if (level > 300) {
                        amplifier = 1;
                }
                final PotionEffect effect = new PotionEffect(type, duration, amplifier, false);
                return effect;
        }

        @Override
        public boolean cast(Player player) {
                int level = plugin.playerManager.getPlayerInfo(player).getElementalLevel(element);
                PotionEffect effect = createPotionEffect(level);
                return player.addPotionEffect(effect);
        }

        @Override
        public boolean cast(Player player, Entity entity) {
                if (!(entity instanceof LivingEntity)) return cast(player);
                LivingEntity living = (LivingEntity)entity;
                int level = plugin.playerManager.getPlayerInfo(player).getElementalLevel(element);
                PotionEffect effect = createPotionEffect(level);
                return living.addPotionEffect(effect);
        }
}
