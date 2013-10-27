package com.winthier.skills.util;

import com.winthier.skills.util.Util;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class MaterialFractionMap {
        private final int dividends[];
        private final int divisors[];
        private final int defaultValue;

        public MaterialFractionMap(int defaultValue) {
                this.defaultValue = defaultValue;
                dividends = new int[Material.values().length];
                divisors = new int[Material.values().length];
                reset();
        }

        public void reset() {
                for (int i = 0; i < dividends.length; ++i) {
                        dividends[i] = defaultValue;
                        divisors[i] = 1;
                }
        }

        public Fraction getFraction(Material mat) {
                final int i = mat.ordinal();
                return new Fraction(dividends[i], divisors[i]);
        }

        public boolean isNull(Material mat) {
                return dividends[mat.ordinal()] == 0;
        }

        public int get(Material mat) {
                return roll(mat, 1);
        }

        public int roll(Material mat, int coefficient) {
                final int i = mat.ordinal();
                final int dividend = dividends[i];
                if (dividend <= 0) return dividend;
                final int divisor = divisors[i];
                if (divisor == 1) return dividend * coefficient;
                return Util.rollFraction(coefficient, dividend, divisor);
        }

        public void set(Material mat, int value) {
                final int i = mat.ordinal();
                dividends[i] = value;
                divisors[i] = 1;
        }

        public void set(Material mat, int dividend, int divisor) {
                final int i = mat.ordinal();
                dividends[i] = dividend;
                divisors[i] = divisor;
        }

        public void load(ConfigurationSection config) {
                for (String key : config.getKeys(false)) {
                        Material mat = Material.matchMaterial(key.replaceAll("-", "_"));
                        if (mat == null) {
                                System.err.println("[Skills] Invalid material: " + key);
                                continue;
                        }
                        if (config.isInt(key)) {
                                set(mat, config.getInt(key));
                                continue;
                        }
                        final String string = config.getString(key);
                        String tokens[] = string.split(Pattern.quote("/"));
                        if (tokens.length != 2) {
                                System.err.println("[Skills] Invalid fraction: " + string);
                                continue;
                        }
                        int dividend, divisor;
                        try {
                                dividend = Integer.parseInt(tokens[0]);
                                divisor = Integer.parseInt(tokens[1]);
                        } catch (NumberFormatException e) {
                                System.err.println("[Skills] Invalid fraction: " + string);
                                continue;
                        }
                        set(mat, dividend, divisor);
                }
        }
}
