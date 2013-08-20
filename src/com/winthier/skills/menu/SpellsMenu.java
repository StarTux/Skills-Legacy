package com.winthier.skills.menu;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerResponse;
import com.winthier.skills.spell.AbstractSpell;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SpellsMenu extends Menu {
        private final Player player;
        private final ElementType element;

        public SpellsMenu(SkillsPlugin plugin, Player player, ElementType element) {
                super(plugin, "" + element.getColor() + Util.ICON + ChatColor.DARK_GRAY + " " + element.getDisplayName() + " Spells " + element.getColor() + Util.ICON);
                this.player = player;
                this.element = element;

                final AbstractSpell spells[] = plugin.spellManager.getSpells(element);
                for (AbstractSpell spell : spells) {
                        ItemStack item = new ItemStack(Material.BOOK);
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(spell.getTotemDisplayName());
                        item.setItemMeta(meta);
                        addItem(item);
                }
                createInventory();
                for (int i = 0; i < spells.length; ++i) updateSlot(i);
        }

        public AbstractSpell getSpell(int slot) {
                final AbstractSpell spells[] = plugin.spellManager.getSpells(element);
                if (spells.length <= slot) return null;
                return spells[slot];
        }

        /**
         * Make sure that a specific slot is updated with the
         * correct item, level and lore.  This is called at
         * initialization and whenever the specified slot may have
         * changed.
         */
        private void updateSlot(int slot) {
                final ItemStack item = getInventory().getItem(slot);
                if (item == null) return;
                final AbstractSpell spell = getSpell(slot);
                if (spell == null) return;

                final ItemMeta meta = item.getItemMeta();
                final List<String> lore = new ArrayList<String>();

                //lore.add(Util.format("%s%s &b%s", spell.getElement().getColor(), Util.ICON, spell.getDisplayName()));
                
                // Change item based on user information.
                final boolean unlocked = spell.hasUnlocked(player);
                final int level = spell.getLevel(player);
                final boolean active = spell.hasActivated(player);
                if (unlocked) lore.add(Util.format("&3Level: &b%d/%d", level, spell.getMaxLevel()));
                lore.add(Util.format("&3Use: &b%d XP Levels", spell.getXPCost()));
                lore.addAll(spell.getTotemDescription());
                if (level > 0) {
                        final String thisLevel = spell.getLevelDescription(level);
                        if (thisLevel != null) {
                                lore.add("");
                                lore.add(Util.format("&3This: &b%s", thisLevel));
                                if (level + 1 <= spell.getMaxLevel()) {
                                        final String nextLevel = spell.getLevelDescription(level + 1);
                                        if (nextLevel != null) lore.add(Util.format("&3Next: &b%s", nextLevel));
                                }
                        }
                }
                if (level < spell.getMaxLevel()) {
                        lore.add("");
                        if (level == 0) {
                                lore.add(Util.format("&3%s level to unlock: &b%s", spell.getElement().getDisplayName(), spell.getUnlockLevel(level + 1)));
                                lore.add(Util.format("&3Price to unlock: &b%s", plugin.economyManager.format(spell.getUnlockPrice(level + 1))));
                        } else {
                                lore.add(Util.format("&3%s level to upgrade: &b%s", spell.getElement().getDisplayName(), spell.getUnlockLevel(level + 1)));
                                lore.add(Util.format("&3Price to upgrade: &b%s", plugin.economyManager.format(spell.getUnlockPrice(level + 1))));
                        }
                }
                lore.add("");
                if (unlocked) {
                        if (level < spell.getMaxLevel()) {
                                lore.add(Util.format("&3&oShift Click &bto upgrade"));
                        }
                        lore.add(Util.format("&3&oLeft Click &bto load onto totem."));
                        item.setType(spell.getIconMaterial());
                        item.setDurability(spell.getIconDurability());
                        item.setAmount(level);
                        if (active) {
                                lore.add(Util.format("&3&oRight Click &bto unequip."));
                        } else {
                                lore.add(Util.format("&3&oRight Click &bto equip."));
                        }
                } else {
                        lore.add(Util.format("&3&oShift Click &bto unlock"));
                        item.setType(Material.CHEST);
                        item.setAmount(1);
                }

                // Update item.
                meta.setLore(lore);
                item.setItemMeta(meta);
                if (unlocked && active) {
                        inventory.setItem(slot, Util.addGlow(item));
                } else {
                        inventory.setItem(slot, Util.removeGlow(item));
                }
        }

        @Override
        public void onClick(Player player, int slot, ClickType type) {
                AbstractSpell spell = getSpell(slot);
                if (spell == null) return;
                switch (type) {
                case SHIFT_LEFT:
                        // Unlock or upgrade.
                        PlayerResponse result = spell.upgrade(player);
                        switch (result) {
                        case SUCCESS:
                                final int level = spell.getLevel(player);
                                if (level == 1) {
                                        Util.sendMessage(player, "&bUnlocked %s.", spell.getDisplayName());
                                } else {
                                        Util.sendMessage(player, "&bUpgraded %s to level %d.", spell.getDisplayName(), level);
                                }
                                spell.setActivated(player, true);
                                spell.loadOnto(player);
                                break;
                        case BAD_REQUEST:
                                Util.sendMessage(player, "&cCan't upgrade this spell any further.");
                                break;
                        case SKILL_LEVEL_LOW:
                                Util.sendMessage(player, "&cYour %s skill level is too low.", element.getDisplayName());
                                break;
                        case BALANCE_LOW:
                                Util.sendMessage(player, "&cYou don't have enough money.", element.getDisplayName());
                                break;
                        default:
                                Util.sendMessage(player, "&cAn error has occured. Please report this.");
                        }
                        break;
                case LEFT:
                        // Select and activate.
                        spell.setActivated(player, true);
                        spell.loadOnto(player);
                        break;
                case RIGHT:
                        // Toggle active.
                        spell.toggleActivated(player);
                        break;
                default:
                        // Do nothing.
                        break;
                }
                updateSlot(slot);
                player.updateInventory();
        }
}
