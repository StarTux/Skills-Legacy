package com.winthier.skills.menu;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

public class MenuManager implements Listener {
        public final SkillsPlugin plugin;
        private final Map<Player, Menu> openMenus = new HashMap<Player, Menu>();

        public MenuManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        /**
         * Remember an open menu screen. This is called by
         * Menu.open().
         */
        public void setOpenMenu(Player player, Menu menu) {
                openMenus.put(player, menu);
        }

        public Menu getOpenMenu(Player player) {
                return openMenus.get(player);
        }

        public Menu removeOpenMenu(Player player) {
                return openMenus.remove(player);
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
        public void onInventoryClose(InventoryCloseEvent event) {
                final Player player = (Player)event.getPlayer();
                Menu menu = removeOpenMenu(player);
                if (menu == null) return;
                menu.onClose(player);
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onInventoryDrag(InventoryDragEvent event) {
                final Player player = (Player)event.getWhoClicked();
                Menu menu = getOpenMenu(player);
                if (menu == null) return;
                event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onInventoryClick(InventoryClickEvent event) {
                final Player player = (Player)event.getWhoClicked();
                Menu menu = getOpenMenu(player);
                if (menu == null) return;
                event.setCancelled(true);
                // Only call the menu's hanler if the top inventory was clicked.
                final int raw = event.getRawSlot();
                if (raw >= 0 && raw < event.getView().getTopInventory().getSize()) {
                        menu.onClick(player, event.getSlot(), event.getClick());
                }
        }

        public void openSpellsMenu(Player player, ElementType element) {
                SpellsMenu menu = new SpellsMenu(plugin, player, element);
                menu.open(player);
        }
}
