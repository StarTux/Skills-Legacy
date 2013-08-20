package com.winthier.skills.menu;

import com.winthier.skills.SkillsPlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Menu {
        public final SkillsPlugin plugin;
        protected final List<ItemStack> items = new ArrayList<ItemStack>();
        protected Inventory inventory = null;
        protected String title;

        public Menu(SkillsPlugin plugin, String title) {
                this.plugin = plugin;
                setTitle(title);
        }

        public void addItem(ItemStack item) {
                items.add(item);
        }

        public void setTitle(String title) {
                if (title.length() > 32) title = title.substring(0, 32);
                this.title = title;
        }

        public void open(Player player) {
                if (inventory == null) createInventory();
                player.openInventory(inventory);
                // InventoryCloseEvent is called.
                plugin.menuManager.setOpenMenu(player, this);
        }

        protected void createInventory() {
                int size = ((items.size() - 1) / 9 + 1) * 9;
                inventory = Bukkit.getServer().createInventory(null, size, title);
                int i = 0;
                for (ItemStack item : items) {
                        if (item == null) {
                                i += 1;
                        } else {
                                inventory.setItem(i++, item);
                        }
                }
        }

        public Inventory getInventory() {
                return inventory;
        }

        /**
         * Called when a player closes this menu.
         */
        public void onClose(Player player) {}

        /**
         * Called when a player clicks in this inventory.
         */
        public void onClick(Player player, int slot, ClickType type) {}
}
