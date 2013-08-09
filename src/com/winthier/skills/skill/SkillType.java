package com.winthier.skills.skill;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;

/**
 * This enumeration not only helps identify the skill we are
 * dealing with, it also creates the actual skills and helps
 * retrieving references to them.
 *
 * Each skill has a hardcoded display name and chat color.
 *
 * The order of calling at startup should be:
 * - initAll - Create all skills.
 * - loadAll - Load all SkillType/AbstractSkill configurations
 *             from disk.
 */
public enum SkillType {
        MINING    (MiningSkill    .class, "Mining",     ChatColor.DARK_GREEN ),
        ALCHEMY   (AlchemySkill   .class, "Alchemy",    ChatColor.GOLD       ),
        MELEE     (MeleeSkill     .class, "Melee",      ChatColor.DARK_RED   ),
        ARCHERY   (ArcherySkill   .class, "Archery",    ChatColor.YELLOW     ),
        TRAVELING (TravelingSkill .class, "Traveling",  ChatColor.BLUE       ),
        WILDLIFE  (WildlifeSkill  .class, "Wildlife",   ChatColor.DARK_AQUA  ),
        EATING    (EatingSkill    .class, "Eating",     ChatColor.DARK_BLUE  ),
        HERBALISM (HerbalismSkill .class, "Herbalism",  ChatColor.GREEN      ),
        ENCHANTING(EnchantingSkill.class, "Enchanting", ChatColor.DARK_PURPLE),
        ;

        // Map lower case names to skill types to speed things up.
        private final static Map<String, SkillType> nameMap = new HashMap<String, SkillType>();
        // Map intuitive and shortcut names for the user's convenience.
        private final static Map<String, SkillType> userMap = new HashMap<String, SkillType>();

        
        private final Class<? extends AbstractSkill> clazz;
        private final String name;
        private final String displayName;
        private final ChatColor color;
        private AbstractSkill skill;
        private ElementType elements[];

        /**
         * Put all names in the name map.
         */
        static {
                for (SkillType skillType : values()) {
                        nameMap.put(skillType.getName(), skillType);
                }
        }

        /**
         * The constructor saves the name in lower case.
         */
        SkillType(Class<? extends AbstractSkill> clazz, String displayName, ChatColor color) {
                name = name().toLowerCase();
                this.clazz = clazz;
                this.displayName = displayName;
                this.color = color;
        }

        /**
         * Helper function to save a name in the user map. This
         * map stores intuitive and shortcut names for skill types
         * so a string received from a human user can be mapped to
         * a skill type easier.
         */
        private static void putUserMap(String name, SkillType skillType) {
                name = name.toLowerCase();
                for (int i = 1; i < name.length(); ++i) {
                        final String shortcut = name.substring(0, i);
                        if (!userMap.containsKey(shortcut)) {
                                userMap.put(shortcut, skillType);
                        }
                }
                userMap.put(name, skillType);
        }

        // Setup routines.

        /**
         * Create a corresponding skill.
         */
        public void enable(SkillsPlugin plugin) {
                try {
                        Constructor<? extends AbstractSkill> ctor = clazz.getConstructor(SkillsPlugin.class, SkillType.class);
                        skill = ctor.newInstance(plugin, this);
                        skill.onEnable();
                } catch (Throwable t) {
                        t.printStackTrace();
                }
        }

        public static void enableAll(SkillsPlugin plugin) {
                userMap.clear();
                for (SkillType skillType : values()) skillType.enable(plugin);
        }

        public void disable() {
                userMap.clear();
                skill = null;
                elements = null;
        }

        public static void disableAll() {
                for (SkillType skillType: values()) skillType.disable();
        }

        // Getter functions.

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

        /**
         * Get the elements that are relevant to this skill
         * type. The information is retrieved once from the
         * element types and then cached, from where it is
         * retrieved henceforth.
         *
         * @return Array of relevant element types.
         */
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
                elements = new ElementType[0];
                return elements;
        }

        // Find skill types based on strings.

        /**
         * Get a skill type by the exact name in lower case.
         */
        public static SkillType fromString(String string) {
                return nameMap.get(string);
        }

        /**
         * Get a skill type by an intuitive or shortcut name,
         * usually based on a user's input.
         */
        public static SkillType fromUserString(String string) {
                return userMap.get(string.toLowerCase());
        }

        // Configuration routines

        public static void loadAll() {
                userMap.clear();
                for (SkillType skillType : values()) {
                        skillType.loadConfiguration();
                }
        }

        public void loadConfiguration() {
                skill.loadConfig();
                //skill.plugin.sqlManager.saveSkillInfo(skill);
                putUserMap(name, this);
                putUserMap(displayName, this);
        }
}
