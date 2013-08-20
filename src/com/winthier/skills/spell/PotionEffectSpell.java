package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectSpell extends AbstractSpell {
        private PotionEffectType type;
        private int[] amplifiers = null;
        private int[] durations = null;

        public PotionEffect createPotionEffect(Player player) {
                final int level = getLevel(player);
                int duration = 1;
                if (!type.isInstant()) {
                        duration = durations[level - 1];
                }
                int amplifier = amplifiers[level - 1];
                final PotionEffect effect = new PotionEffect(type, duration, amplifier, false);
                return effect;
        }

        protected boolean isHarmful() {
                return
                        type.equals(PotionEffectType.POISON) ||
                        type.equals(PotionEffectType.HARM) ||
                        type.equals(PotionEffectType.WITHER)
                        ;
        }

        @Override
        public boolean cast(Player player) {
                if (type.equals(PotionEffectType.HEAL)) {
                        if (player.getHealth() >= player.getMaxHealth()) return false;
                }
                if (isHarmful() && !player.isSneaking()) return false;
                final int level = getLevel(player);
                PotionEffect effect = createPotionEffect(player);
                return player.addPotionEffect(effect);
        }

        @Override
        public boolean cast(Player player, Entity entity) {
                if (!(entity instanceof LivingEntity)) return cast(player);
                LivingEntity living = (LivingEntity)entity;
                PotionEffect effect = createPotionEffect(player);
                return living.addPotionEffect(effect);
        }

        @Override
        public String getLevelDescription(int level) {
                if (level < 1 || level > maxLevel) return null;
                if (durations == null) {
                        int amplifier = amplifiers[level - 1];
                        return Util.format("Potion Level %s", Util.roman(amplifier + 1));
                } else {
                        int amplifier = amplifiers[level - 1];
                        int duration = durations[level - 1];
                        int minutes = duration / 20 / 60;
                        int seconds = (duration / 20) % 60;
                        return Util.format("Potion Level %s, %d:%02d", Util.roman(amplifier + 1), minutes, seconds);
                }
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                type = PotionEffectType.getByName(config.getString("PotionEffect").replaceAll("-", "_"));
                if (type == null) {
                        logWarning("Missing potion effect.");
                        return false;
                }

                // Load amplifiers.
                amplifiers = Util.toIntArray(config.getIntegerList("Amplifiers"));
                for (int i = 0; i < amplifiers.length; ++i) amplifiers[i] -= 1;
                if (maxLevel != amplifiers.length) {
                        logWarning("Durations don't match max level.");
                        return false;
                }

                // Load durations, unless this potion effect type is instant.
                if (!type.isInstant()) {
                        durations = Util.toIntArray(config.getIntegerList("Durations"));
                        for (int i = 0; i < durations.length; ++i) durations[i] *= 20;
                        if (maxLevel != amplifiers.length) {
                                logWarning("Amplifiers don't match max level.");
                                return false;
                        }
                }

                return true;
        }
}
