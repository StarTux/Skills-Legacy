package com.winthier.skills;

import com.winthier.skills.skill.SkillType;
import com.winthier.skills.spell.Totem;
import com.winthier.skills.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum ElementType {
        EARTH("Earth", ChatColor.DARK_GREEN),
        FIRE("Fire", ChatColor.DARK_RED),
        WATER("Water", ChatColor.DARK_BLUE),
        AIR("Air", ChatColor.BLUE);

        public final String name;
        public final String displayName;
        public final ChatColor color;
        public final int skillAmount = 4;
        public SkillType[] skillTypes;

        ElementType(String displayName, ChatColor color) {
                this.displayName = displayName;
                this.color = color;
                this.name = name().toLowerCase();
        }

        private void setSkills(SkillType primary, SkillType secondaryA, SkillType secondaryB) {
                skillTypes = new SkillType[3];
                skillTypes[0] = primary;
                skillTypes[1] = secondaryA;
                skillTypes[2] = secondaryB;
        }

        public String getName() {
                return name;
        }

        public String getDisplayName() {
                return displayName;
        }

        public ChatColor getColor() {
                return color;
        }

        public Material getTotemMaterial() {
                return Totem.getTotemMaterial(this);
        }

        /**
         * Returns a 3-array with the primary skill followed by
         * the secondary skills.
         */
        public SkillType[] getSkills() {
                if (skillTypes != null) return skillTypes;
                switch (this) {
                case EARTH:
                        setSkills(SkillType.MINING, SkillType.HERBALISM, SkillType.ALCHEMY);
                        break;
                case FIRE:
                        setSkills(SkillType.MELEE, SkillType.ALCHEMY, SkillType.ARCHERY);
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

        public static ElementType fromString(String string) {
                return Util.enumFromString(ElementType.class, string);
        }
}
