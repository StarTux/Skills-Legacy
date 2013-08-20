package com.winthier.skills.util;

import java.util.regex.Pattern;

public class Fraction {
        private final int dividend;
        private final int divisor;

        public Fraction(int dividend, int divisor) {
                this.dividend = dividend;
                this.divisor = divisor;
        }

        public int roll(int coefficient) {
                if (dividend <= 0) return 0;
                if (divisor == 1) return dividend * coefficient;
                return Util.rollFraction(coefficient, dividend, divisor);
        }

        public int get() {
                return roll(1);
        }

        public static Fraction parseFraction(String string) {
                String tokens[] = string.split(Pattern.quote("/"));
                if (tokens.length > 2) {
                        System.err.println("[Skills] Invalid fraction: " + string);
                        return new Fraction(0, 1);
                }
                int dividend;
                int divisor = 1;
                try {
                        dividend = Integer.parseInt(tokens[0]);
                } catch (NumberFormatException nfe) {
                        System.err.println("[Skills] Invalid dividend: " + string);
                        return new Fraction(0, 1);
                }
                if (tokens.length == 2) {
                        try {
                                divisor = Integer.parseInt(tokens[1]);
                        } catch (NumberFormatException nfe) {
                                System.err.println("[Skills] Invalid divisor: " + string);
                                return new Fraction(0, 1);
                        }
                }
                return new Fraction(dividend, divisor);
        }
}
