package com.winthier.skills.util;

import com.winthier.skills.util.Util;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;

public class EnumFractionMap<K extends Enum<K>> {
        private final Class<K> clazz;
        private final int dividends[];
        private final int divisors[];
        private final int defaultValue;

        public EnumFractionMap(Class<K> clazz, int defaultValue) {
                this.clazz = clazz;
                this.defaultValue = defaultValue;
                dividends = new int[clazz.getEnumConstants().length];
                divisors = new int[clazz.getEnumConstants().length];
                reset();
        }

        public void reset() {
                for (int i = 0; i < dividends.length; ++i) {
                        dividends[i] = defaultValue;
                        divisors[i] = 1;
                }
        }

        public Fraction getFraction(K k) {
                final int i = k.ordinal();
                return new Fraction(dividends[i], divisors[i]);
        }

        public int get(K k) {
                return roll(k, 1);
        }

        public int roll(K k, int coefficient) {
                final int i = k.ordinal();
                final int dividend = dividends[i];
                if (dividend <= 0) return dividend;
                final int divisor = divisors[i];
                if (divisor == 1) return dividend * coefficient;
                return Util.rollFraction(coefficient, dividend, divisor);
        }

        public void set(K k, int value) {
                final int i = k.ordinal();
                dividends[i] = value;
                divisors[i] = 1;
        }

        public void set(K k, int dividend, int divisor) {
                final int i = k.ordinal();
                dividends[i] = dividend;
                divisors[i] = divisor;
        }

        public void load(ConfigurationSection config) {
                for (String key : config.getKeys(false)) {
                        K k = null;
                        try {
                                k = Enum.valueOf(clazz, key.toUpperCase().replaceAll("-", "_"));
                        } catch (IllegalArgumentException iae) {
                                // Do nothing.
                        }
                        if (k == null) {
                                System.err.println("[Skills] Invalid material: " + key);
                                continue;
                        }
                        if (config.isInt(key)) {
                                set(k, config.getInt(key));
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
                        set(k, dividend, divisor);
                }
        }
}
