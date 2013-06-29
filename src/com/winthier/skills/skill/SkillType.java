package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public enum SkillType {
        EATING(EatingSkill.class),
        HERBALISM(HerbalismSkill.class),
        MINING(MiningSkill.class),
        SMELTING(SmeltingSkill.class),
        TRAVELLING(TravellingSkill.class),
        WILDLIFE(WildlifeSkill.class),
        MELEE(MeleeSkill.class),
        ARCHERY(ArcherySkill.class),
        ;

        private final Class<? extends AbstractSkill> clazz;
        private final static Map<String, SkillType> nameMap = new HashMap<String, SkillType>();
        private final String name;
        private AbstractSkill skill;

        static {
                for (SkillType skillType : values()) {
                        nameMap.put(skillType.getName(), skillType);
                }
        }

        SkillType(Class<? extends AbstractSkill> clazz) {
                name = name().toLowerCase();
                this.clazz = clazz;
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
        }

        public static void initAll(SkillsPlugin plugin) {
                for (SkillType skillType : values()) skillType.init(plugin);
        }

        public AbstractSkill getSkill() {
                return skill;
        }

        public String getName() {
                return name;
        }

        public static SkillType fromString(String string) {
                return nameMap.get(string);
        }

        public static void loadAll() {
                for (SkillType skillType : values()) {
                        skillType.loadConfiguration();
                }
        }
}
