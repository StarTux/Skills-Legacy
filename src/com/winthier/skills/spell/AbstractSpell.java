package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.player.PlayerResponse;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * This is the base class of all spells.  A spell can be cast
 * using an item called a Totem.  Casting a spell costs an amount
 * of xp levels specific to the spell.  Players have to unlock
 * spells before using them.  Spells are unlocked in exchange for
 * a price and under the condition that a certain elemental skill
 * level is met.  Spells can be leveled up past the initial level
 * for another specific price and elemental skill level
 * requirement.
 *
 * Withing the context of this class, spell levels are interpreted
 * in the same way they are presented to the user, meaning the
 * first level is 1, not 0.
 */
public abstract class AbstractSpell {
        // Basic properties, defined by init().
        protected SkillsPlugin plugin;
        protected String name;
        protected String totemName; // The first line of lore.

        // 
        protected ElementType element;
        protected String displayName;
        protected String totemDisplayName = null;
        protected String description;
        protected List<String> totemDescription;

        private int elementIndex; // The index within the array of this element's spells

        //
        protected int maxLevel; // Highest level to upgrade this spell to.
        protected int xpCost; // xp cost to cast

        protected int unlockLevels[] = null; // elemental skill level to unlock
        protected int unlockPrices[] = null; // money to unlock

        // Icon
        protected Material iconMaterial = Material.BOOK;
        protected short iconDurability = 0;

        public AbstractSpell() {}

        public void init(SkillsPlugin plugin, String name) {
                // final variables
                this.plugin = plugin;
                this.name = name;
                this.totemName = Totem.TOTEM_LORE_MAGIC + " " + name;
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

        public ElementType getElement() {
                return element;
        }

        public int getXPCost() {
                return xpCost;
        }

        public int getMaxLevel() {
                return maxLevel;
        }

        public String getDescription() {
                return description;
        }

        public abstract String getLevelDescription(int level);


        public List<String> getTotemDescription() {
                return totemDescription;
        }

        /**
         * Get the required elemental skill level to upgrade this
         * spell to the desired level.
         */
        public int getUnlockLevel(int level) {
                return unlockLevels[level - 1];
        }

        /**
         * Get the price of unlocking a certain level.
         */
        public int getUnlockPrice(int level) {
                return unlockPrices[level - 1];
        }

        public Material getIconMaterial() {
                return iconMaterial;
        }

        public short getIconDurability() {
                return iconDurability;
        }

        // Casting spells.

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
        public boolean cast(Player player, Block target, BlockFace face) {
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

        /**
         * Attempt to unlock a spell level. Perform all checks and bookings.
         */
        public PlayerResponse unlockLevel(Player player, int level) {
                final PlayerInfo info = plugin.playerManager.getPlayerInfo(player);

                // Check elemental skill level.
                final int elementLevel = info.getElementalLevel(element);
                if (elementLevel < getUnlockLevel(level)) return PlayerResponse.SKILL_LEVEL_LOW;

                // Check money.
                final double price = (double)getUnlockPrice(level);
                if (!info.hasMoney(price)) return PlayerResponse.BALANCE_LOW;

                // Make the payment.
                if (!info.takeMoney(price)) return PlayerResponse.PAYMENT_ERROR;
                plugin.getLogger().info(player.getName() + " paid " + plugin.economyManager.format(price) + " to unlock " + name + "[" + level + "]");

                // Set the level.
                plugin.playerManager.getPlayerInfo(player).spellsInfo.setSpellLevel(this, level);
                return PlayerResponse.SUCCESS;
        }

        public PlayerResponse upgrade(Player player) {
                final int level = getLevel(player);
                if (level >= getMaxLevel()) return PlayerResponse.BAD_REQUEST;
                return unlockLevel(player, level + 1);
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

        /**
         * Load onto the player's item in hand, if possible.
         */
        public boolean loadOnto(Player player) {
                ItemStack item = player.getItemInHand();
                if (item.getType() != element.getTotemMaterial()) return false;
                if (!Totem.isTotem(item)) return false;
                if (!hasUnlocked(player)) return false;
                loadOnto(item, player);
                player.setItemInHand(Util.addGlow(item));
                return true;
        }

        /**
         * Load onto a totem. No checks if the item is a valid
         * totem are made here, so only call this if you have
         * checked it yourself.
         * @see loadOnto(Player)
         * @see SpellManager#onSwitchTotem(Player, ItemStack)
         */
        public void loadOnto(ItemStack totem, Player player) {
                ItemMeta meta = totem.getItemMeta();
                List<String> lore = new ArrayList<String>();
                lore.add(totemName);
                lore.add(Util.format("&3Level: &b%d", getLevel(player)));
                lore.add(Util.format("&3Use: &b%d XP Levels", getXPCost()));
                lore.addAll(totemDescription);
                meta.setLore(lore);
                meta.setDisplayName(getTotemDisplayName());
                totem.setItemMeta(meta);
        }

        // Configuration routines

        private ConfigurationSection getConfig() {
                ConfigurationSection result = plugin.spellManager.getConfig().getConfigurationSection(name);
                if (result == null) {
                        plugin.getLogger().warning("Spell had no configuration section: " + name);
                        result = plugin.spellManager.getConfig().createSection(name);
                }
                return result;
        }

        protected void logWarning(String msg) {
                plugin.getLogger().warning("[Spell] " + name + ": " + msg);
        }

        public boolean loadConfig() {
                ConfigurationSection config = getConfig();

                element = Util.enumFromString(ElementType.class, config.getString("Element"));
                if (element == null) {
                        logWarning("Missing element.");
                        return false;
                }

                displayName = config.getString("DisplayName");
                if (displayName == null) displayName = name;
                description = config.getString("Description");
                if (description == null) description = displayName;
                totemDescription = Util.fillParagraph(description, 33, ChatColor.RESET.toString());

                maxLevel = config.getInt("MaxLevel");
                unlockLevels = Util.toIntArray(config.getIntegerList("UnlockLevels"));
                unlockPrices = Util.toIntArray(config.getIntegerList("UnlockPrices"));

                // Get some helpful debug output.
                if (unlockLevels.length != maxLevel) {
                        logWarning("Unlock levels don't match max level.");
                        return false;
                }
                if (unlockPrices.length != maxLevel) {
                        logWarning("Unlock prices don't match max level.");
                        return false;
                }

                xpCost = config.getInt("XPCost");
                totemDisplayName = null; // "Flush" the cache.

                String icon = config.getString("Icon");
                if (icon != null) {
                        String tokens[] = icon.split(":");
                        if (tokens.length > 2) {
                                logWarning("Icon has too many colons.");
                                return false;
                        }
                        Material mat = Util.enumFromString(Material.class, tokens[0]);
                        if (mat == null) {
                                logWarning("Unknown material: " + tokens[0]);
                                return false;
                        }
                        iconMaterial = mat;
                        if (tokens.length >= 2) {
                                try {
                                        iconDurability = Short.parseShort(tokens[1]);
                                } catch (NumberFormatException nfe) {
                                        logWarning("Bad icon durability: " + tokens[1]);
                                        return false;
                                }
                        }
                }

                return loadConfiguration(config); // Load the spell specific config.
        }

        public abstract boolean loadConfiguration(ConfigurationSection config);
}
