package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import org.bukkit.configuration.ConfigurationSection;

public class AbstractSpell {
        private final SkillsPlugin plugin;
        public final String name;
        public final String displayName;
        private ElementType element;
        private String description;

        public AbstractSpell(SkillsPlugin plugin, String name, String displayName, ElementType element) {
                this.plugin = plugin;
                this.name = name;
                this.displayName = displayName;
                this.element = element;
        }

        // Configuration routines

        protected ConfigurationSection getConfig() {
                return plugin.getConfig().getConfigurationSection("spells").getConfigurationSection(name);
        }

        public void loadConfig() {
                description = getConfig().getString("Description", "");
                loadConfiguration();
        }

        /** 
         * Override this if you have any additional configurations.
         */
        protected void loadConfiguration() {}
}
