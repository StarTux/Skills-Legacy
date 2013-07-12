package com.winthier.skills.skill;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;

public enum SkillType {
        MINING(MiningSkill.class, "Mining", ChatColor.DARK_GREEN),
        SMELTING(SmeltingSkill.class, "Smelting", ChatColor.GOLD),
        MELEE(MeleeSkill.class, "Melee", ChatColor.DARK_RED),
        ARCHERY(ArcherySkill.class, "Archery", ChatColor.YELLOW),
        TRAVELING(TravelingSkill.class, "Traveling", ChatColor.WHITE),
        WILDLIFE(WildlifeSkill.class, "Wildlife", ChatColor.BLUE),
        EATING(EatingSkill.class, "Eating", ChatColor.DARK_BLUE),
        HERBALISM(HerbalismSkill.class, "Herbalism", ChatColor.DARK_AQUA),
        ;

        private final Class<? extends AbstractSkill> clazz;
        private final static Map<String, SkillType> nameMap = new HashMap<String, SkillType>();
        private final static Map<String, SkillType> userMap = new HashMap<String, SkillType>();
        private final String name;
        private final String displayName;
        private final ChatColor color;
        private AbstractSkill skill;
        private ElementType elements[];

        static {
                for (SkillType skillType : values()) {
                        nameMap.put(skillType.getName(), skillType);
                }
        }

        SkillType(Class<? extends AbstractSkill> clazz, String displayName, ChatColor color) {
                name = name().toLowerCase();
                this.clazz = clazz;
                this.displayName = displayName;
                this.color = color;
        }

        private static void putUserMap(String name, SkillType skillType) {
                name = name.toLowerCase();
                for (int i = 1; i < name.length(); ++i) {
                        userMap.put(name.substring(0, i), skillType);
                }
                userMap.put(name, skillType);
        }

        public void init(SkillsPlugin plugin) {
                try {
                        Constructor<? extends AbstractSkill> ctor = clazz.getConstructor(SkillsPlugin.class, SkillType.class);
                        skill = ctor.newInstance(plugin, this);
                        skill.onEnable();
                } catch (Throwable t) {
                        t.printStackTrace();
                }
        }

        public void loadConfiguration() {
                skill.loadConfig();
                skill.plugin.sqlManager.saveSkillInfo(skill);
                putUserMap(name, this);
                putUserMap(displayName, this);
        }

        public static void initAll(SkillsPlugin plugin) {
                userMap.clear();
                for (SkillType skillType : values()) skillType.init(plugin);
        }

        public AbstractSkill getSkill() {
                return skill;
        }

        /**
         * Get the short name used primarily for MySQL references.
         */
        public String getName() {
                return name;
        }

        /**
         * Get a convenient display name for the user interface.
         */
        public String getDisplayName() {
                return displayName;
        }

        public ChatColor getColor() {
                return color;
        }

        public ElementType[] getElements() {
                if (elements != null) return elements;
                for (ElementType elem : ElementType.values()) {
                        SkillType skillTypes[] = elem.getSkills();
                        if (skillTypes[0] == this) {
                                elements = new ElementType[1];
                                elements[0] = elem;
                                return elements;
                        }
                        if (skillTypes[1] == this || skillTypes[2] == this) {
                                if (elements == null) {
                                        elements = new ElementType[2];
                                        elements[0] = elem;
                                } else {
                                        elements[1] = elem;
                                        return elements;
                                }
                        }
                }
                return null; // unreachable
        }

        public static SkillType fromString(String string) {
                return nameMap.get(string);
        }

        public static SkillType fromUserString(String string) {
                return userMap.get(string.toLowerCase());
        }

        public static void loadAll() {
                userMap.clear();
                for (SkillType skillType : values()) {
                        skillType.loadConfiguration();
                }
        }
}
