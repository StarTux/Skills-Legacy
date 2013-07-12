package com.winthier.skills;

import com.winthier.skills.skill.SkillType;
import org.bukkit.ChatColor;

public enum ElementType {
        EARTH("Earth", ChatColor.DARK_GREEN),
        FIRE("Fire", ChatColor.DARK_RED),
        WATER("Water", ChatColor.DARK_BLUE),
        AIR("Air", ChatColor.WHITE);

        public final String displayName;
        public final ChatColor color;
        public final int skillAmount = 4;
        public SkillType[] skillTypes;

        ElementType(String displayName, ChatColor color) {
                this.displayName = displayName;
                this.color = color;
        }

        private void setSkills(SkillType primary, SkillType secondaryA, SkillType secondaryB) {
                skillTypes = new SkillType[3];
                skillTypes[0] = primary;
                skillTypes[1] = secondaryA;
                skillTypes[2] = secondaryB;
        }

        public String getDisplayName() {
                return displayName;
        }

        public ChatColor getColor() {
                return color;
        }

        /**
         * Returns a 3-array with the primary skill followed by
         * the secondary skills.
         */
        public SkillType[] getSkills() {
                if (skillTypes != null) return skillTypes;
                switch (this) {
                case EARTH:
                        setSkills(SkillType.MINING, SkillType.HERBALISM, SkillType.SMELTING);
                        break;
                case FIRE:
                        setSkills(SkillType.MELEE, SkillType.SMELTING, SkillType.ARCHERY);
                        break;
                case AIR:
                        setSkills(SkillType.TRAVELING, SkillType.ARCHERY, SkillType.WILDLIFE);
                        break;
                case WATER:
                        setSkills(SkillType.EATING, SkillType.WILDLIFE, SkillType.HERBALISM);
                        break;
                }
                return skillTypes;
        }
}
