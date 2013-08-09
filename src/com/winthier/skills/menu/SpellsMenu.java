package com.winthier.skills.menu;

import com.winthier.skills.ElementType;
import com.winthier.skills.spell.AbstractSpell;
import com.winthier.skills.util.Util;
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

        public SpellsMenu(MenuManager manager, Player player, ElementType element) {
                super(manager, "" + element.getColor() + Util.ICON + ChatColor.DARK_GRAY + " " + element.getDisplayName() + " Spells " + element.getColor() + Util.ICON);
                this.player = player;
                this.element = element;

                final AbstractSpell spells[] = manager.plugin.spellManager.getSpells(element);
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
                final AbstractSpell spells[] = manager.plugin.spellManager.getSpells(element);
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
                final boolean unlocked = spell.hasUnlocked(player);
                if (unlocked) {
                        final int level = spell.getLevel(player);
                        item.setType(Material.BOOK);
                        item.setAmount(level);
                        final boolean active = spell.hasActivated(player);
                        if (active) {
                                inventory.setItem(slot, Util.addGlow(item));
                        } else {
                                inventory.setItem(slot, Util.removeGlow(item));
                        }
                } else {
                        item.setType(Material.CHEST);
                        item.setAmount(1);
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
                        spell.setUnlocked(player);
                        break;
                case LEFT:
                        // Select and activate.
                        spell.setActivated(player, true);
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
