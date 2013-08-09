package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public abstract class AbstractSpell {
        public final SkillsPlugin plugin;
        public final ElementType element;
        public final String name;
        public final String totemName;
        protected String displayName;
        protected String totemDisplayName = null;
        protected String description;
        protected int minElementLevel; // elemental skill level to unlock
        protected int xpCost; // xp cost to cast
        protected int price; // money to unlock
        private int elementIndex; // The index within the array of this element's spells

        public AbstractSpell(SkillsPlugin plugin, ElementType element, String name) {
                // final variables
                this.plugin = plugin;
                this.element = element;
                this.name = name;
                this.totemName = "" + ChatColor.BLACK + ChatColor.MAGIC + name;
        }

        public String getName() {
                return name;
        }

        public String getTotemName() {
                return totemName;
        }

        public String getDisplayName() {
                return displayName;
        }

        public String getTotemDisplayName() {
                if (totemDisplayName == null) {
                        totemDisplayName = "" + element.getColor() + displayName;
                }
                return totemDisplayName;
        }

        public int getXpCost() {
                return xpCost;
        }

        public int getMinLevel() {
                return minElementLevel;
        }

        public ElementType getElement() {
                return element;
        }

        /**
         * Called when a player casts this spell.
         */
        public abstract boolean cast(Player player);

        /**
         * Called when a player casts this spell on a target
         * entity.
         * This default implementation calls the overloaded
         * equivalent without any parameters.
         */
        public boolean cast(Player player, Entity target) {
                return cast(player);
        }

        /**
         * Called when a player casts this spell on a target
         * block.
         * This default implementation calls the overloaded
         * equivalent without any parameters.
         */
        public boolean cast(Player player, Block target) {
                return cast(player);
        }

        // Householding functions

        public final void setElementIndex(int elementIndex) {
                this.elementIndex = elementIndex;
        }

        public final int getElementIndex() {
                return elementIndex;
        }

        // Player Information

        public boolean hasUnlocked(Player player) {
                return plugin.playerManager.getPlayerInfo(player).spellsInfo.hasSpell(this);
        }

        public void setUnlocked(Player player) {
                plugin.playerManager.getPlayerInfo(player).spellsInfo.addSpell(this);
        }

        public int getLevel(Player player) {
                return plugin.playerManager.getPlayerInfo(player).spellsInfo.getSpellLevel(this);
        }

        public boolean hasActivated(Player player) {
                PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                return info.spellsInfo.hasSpell(this) && info.spellsInfo.isActive(this);
        }

        public void setActivated(Player player, boolean activated) {
                plugin.playerManager.getPlayerInfo(player).spellsInfo.setActive(this, activated);
        }

        public void toggleActivated(Player player) {
                plugin.playerManager.getPlayerInfo(player).spellsInfo.toggleActive(this);
        }

        // Configuration routines

        protected ConfigurationSection getConfig() {
                ConfigurationSection result = plugin.getConfig().getConfigurationSection("spells").getConfigurationSection(name);
                if (result == null) {
                        plugin.getLogger().warning("Spell had no configuration section: " + name);
                        result = plugin.getConfig().getConfigurationSection("spells").createSection(name);
                }
                return result;
        }

        public void loadConfig() {
                ConfigurationSection config = getConfig();

                if (!config.getDefaultSection().isSet("DisplayName")) config.addDefault("DisplayName", name);
                if (!config.getDefaultSection().isSet("Description")) config.addDefault("Description", name);
                if (!config.getDefaultSection().isSet("MinElementLevel")) config.addDefault("MinElementLevel", 10);
                if (!config.getDefaultSection().isSet("XPCost")) config.addDefault("XPCost", 5);
                if (!config.getDefaultSection().isSet("Price")) config.addDefault("Price", 1000);

                displayName = config.getString("DisplayName");
                description = config.getString("Description");
                minElementLevel = config.getInt("MinElementLevel");
                xpCost = config.getInt("XPCost");
                price = config.getInt("Price");

                totemDisplayName = null;
        }
}
