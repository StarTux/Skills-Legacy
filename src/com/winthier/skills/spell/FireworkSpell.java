package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkSpell extends AbstractSpell {
        private List<FireworkEffect> effects = new ArrayList<FireworkEffect>();

        @Override
        public boolean cast(Player player) {
                Location loc = player.getEyeLocation();
                Firework firework = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffects(effects);
                int power = player.isSneaking() ? 0 : 1;
                meta.setPower(power);
                firework.setFireworkMeta(meta);
                return true;
        }

        @Override
        public boolean cast(Player player, Block block, BlockFace face) {
                Location loc = block.getRelative(face).getLocation();
                loc.add(0.5, 0.5, 0.5);
                Firework firework = (Firework)loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffects(effects);
                int power = player.isSneaking() ? 0 : 1;
                meta.setPower(power);
                firework.setFireworkMeta(meta);
                return true;
        }

        private List<Color> parseColors(List<String> list) {
                List<Color> colors = new ArrayList<Color>();
                for (String string : list) {
                        DyeColor color = Util.enumFromString(DyeColor.class, string);
                        if (color == null) {
                                logWarning("Unknown dye color: " + string);
                                return null;
                        }
                        colors.add(color.getColor());
                }
                return colors;
        }

        @Override
        public String getLevelDescription(int level) {
                return null;
        }
        
        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                for (Map<?, ?> map : config.getMapList("effects")) {
                        MemoryConfiguration mem = new MemoryConfiguration();
                        ConfigurationSection eConfig = mem.createSection("x", map);
                        FireworkEffect.Type type = Util.enumFromString(FireworkEffect.Type.class, eConfig.getString("Effect", ""));
                        if (type == null) {
                                logWarning("Missing effect type.");
                                return false;
                        }
                        List<Color> colors = parseColors(eConfig.getStringList("Colors"));
                        if (colors == null) return false;
                        if (colors.isEmpty()) {
                                logWarning("Missing colors");
                                return false;
                        }
                        List<Color> fades = parseColors(eConfig.getStringList("Fades"));
                        if (fades == null) return false;
                        boolean flicker = eConfig.getBoolean("Flicker");
                        boolean trail = eConfig.getBoolean("Trail");
                        FireworkEffect.Builder builder = FireworkEffect.builder();
                        builder.with(type);
                        builder.withColor(colors);
                        if (!fades.isEmpty()) builder.withFade(fades);
                        if (flicker) builder.withFlicker();
                        if (trail) builder.withTrail();
                        effects.add(builder.build());
                }
                if (effects.isEmpty()) {
                        logWarning("Missing effects.");
                        return false;
                }
                return true;
        }
}
