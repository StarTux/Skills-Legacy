package com.winthier.skills.player;

/**
 * This enum is supposed to be returned by functions to indicate
 * whether they did as asked and if not, identify the reason of
 * failure.
 */
public enum PlayerResponse {
        SUCCESS,

        BAD_REQUEST, // The request itself is faulty.

        XP_LEVEL_LOW, // Not enough xp levels.
        SKILL_LEVEL_LOW, // Not high enough skill level.
        BALANCE_LOW, // Not enough money.
        PAYMENT_ERROR, // Payment error.
        ;

        public boolean success() {
                return this == SUCCESS;
        }
}
