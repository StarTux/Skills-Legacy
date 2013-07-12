package com.winthier.skills.util;

import org.bukkit.configuration.ConfigurationSection;

public class EnumIntMap<K extends Enum<K>> {
        private final int values[];
        private final int defaultValue;

        public EnumIntMap(Class<K> clazz, int defaultValue) {
                this.defaultValue = defaultValue;
                values = new int[clazz.getEnumConstants().length];
                reset();
        }

        public void reset() {
                for (int i = 0; i < values.length; ++i) values[i] = defaultValue;
        }

        public int get(K key) {
                return values[key.ordinal()];
        }

        public void set(K key, int value) {
                values[key.ordinal()] = value;
        }

        public void put(K key, int value) {
                values[key.ordinal()] = value;
        }

        public void load(Class<K> clazz, ConfigurationSection config) {
                for (String strKey : config.getKeys(false)) {
                        K key = null;
                        try {
                                key = Enum.valueOf(clazz, strKey.toUpperCase().replaceAll("-", "_"));
                        } catch (IllegalArgumentException e) {
                                // do nothing
                        }
                        if (key == null) {
                                System.err.println("[Skills] Invalid " + clazz.getSimpleName() + ": " + strKey);
                                continue;
                        }
                        set(key, config.getInt(strKey));
                }
        }
}
