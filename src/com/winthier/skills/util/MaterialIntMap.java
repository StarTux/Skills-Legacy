package com.winthier.skills.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class MaterialIntMap {
        private final int mats[];
        private final int defaultValue;

        public MaterialIntMap(int defaultValue) {
                this.defaultValue = defaultValue;
                mats = new int[Material.values().length];
                reset();
        }

        public void reset() {
                for (int i = 0; i < mats.length; ++i) mats[i] = defaultValue;
        }

        public int get(Material mat) {
                return mats[mat.ordinal()];
        }

        public void set(Material mat, int value) {
                mats[mat.ordinal()] = value;
        }

        public void load(ConfigurationSection config) {
                for (String key : config.getKeys(false)) {
                        Material mat = Material.matchMaterial(key.replaceAll("-", "_"));
                        if (mat == null) {
                                System.err.println("[Skills] Invalid material: " + key);
                                continue;
                        }
                        set(mat, config.getInt(key));
                }
        }
}
