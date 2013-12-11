package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.util.Util;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpellManager implements Listener {
        private final SkillsPlugin plugin;
        private final Map<String, AbstractSpell> spellByName = new HashMap<String, AbstractSpell>();
        private final Map<ElementType, AbstractSpell[]> spellLists = new EnumMap<ElementType, AbstractSpell[]>(ElementType.class);
        private ConfigurationSection config;
        private boolean ignoreEvents = false;

        public SpellManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                loadSpells();
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void onDisable() {
        }

        public ConfigurationSection getConfig() {
                if (config == null) {
                        config = plugin.getConfigFile("spells.yml");
                }
                return config;
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onPlayerInteract(PlayerInteractEvent event) {
                if (ignoreEvents) return;

                final Player player = event.getPlayer();
                final ItemStack item = player.getItemInHand();
                if (item == null) return;
                if (!Totem.isTotem(item)) return;
                switch (event.getAction()) {
                case LEFT_CLICK_AIR:
                        event.setCancelled(true);
                        onUseTotem(player, item);
                        break;
                case LEFT_CLICK_BLOCK:
                        event.setCancelled(true);
                        onUseTotem(player, item, event.getClickedBlock(), event.getBlockFace());
                        break;
                case RIGHT_CLICK_AIR:
                case RIGHT_CLICK_BLOCK:
                        if (player.isSneaking()) {
                                event.setCancelled(true);
                                ElementType element = Totem.getTotemType(item);
                                if (element == null) return;
                                plugin.menuManager.openSpellsMenu(player, element);
                                player.setItemInHand(Util.addGlow(item));
                        } else {
                                event.setCancelled(true);
                                onSwitchTotem(player, item);
                                player.setItemInHand(Util.addGlow(item));
                        }
                        break;
                default:
                        return;
                }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                if (ignoreEvents) return;

                final Player player = event.getPlayer();
                final ItemStack item = player.getItemInHand();
                if (item == null) return;
                if (!Totem.isTotem(item)) return;
                event.setCancelled(true);
                // PlayerInteractEntityEvent will take over from here.
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
                if (ignoreEvents) return;

                if (!(event.getDamager() instanceof Player)) return;
                final Player player = (Player)event.getDamager();
                ItemStack item = player.getItemInHand();
                if (item == null) return;
                if (!Totem.isTotem(item)) return;
                event.setCancelled(true);
                onUseTotem(player, item, event.getEntity());
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onCraftItem(CraftItemEvent event) {
                if (ignoreEvents) return;

                if (!(event.getWhoClicked() instanceof Player)) return;
                Player player = (Player)event.getWhoClicked();
                for (ItemStack item : event.getInventory().getMatrix()) {
                        if (Totem.isTotem(item)) {
                                event.setCancelled(true);
                                Util.sendMessage(player, "&cCareful! Don't use your totem for crafting.");
                                return;
                        }
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onPlayerItemHeld(PlayerItemHeldEvent event) {
                final int slot = event.getNewSlot();
                final Player player = event.getPlayer();
                final PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                if (info.totemInformed) return;
                final ItemStack item = player.getInventory().getItem(slot);
                if (!Totem.isTotem(item)) return;
                info.totemInformed = true;
                new BukkitRunnable() {
                        public void run() {
                                if (!Totem.isTotem(player.getItemInHand())) {
                                        info.totemInformed = false;
                                        return;
                                }
                                Util.sendMessage(player, "&3This is your &b%s Totem&3.", Totem.getTotemType(item).getDisplayName());
                                Util.sendMessage(player, "&b&oLeft click&3 to cast a spell.");
                                Util.sendMessage(player, "&b&oRight click&3 to select a spell.");
                                Util.sendMessage(player, "&b&oShift &3and&b&o right click&3 to open the spell menu.");
                        }
                }.runTaskLater(plugin, 20L);
        }

        public AbstractSpell getSpell(String spellName) {
                return spellByName.get(spellName);
        }

        /**
         * Find the spell that is loaded on a totem.
         * @return The spell or null if none is loaded or the
         * loaded spell is invalid.
         */
        public AbstractSpell getSpell(ItemStack totem) {
                ItemMeta meta = totem.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore.size() < 1) {
                        return null;
                } else {
                        final String line = lore.get(0);
                        if (line.length() < Totem.TOTEM_LORE_MAGIC.length() + 2) return null;
                        final String spellName = line.substring(Totem.TOTEM_LORE_MAGIC.length() + 1, line.length());
                        return spellByName.get(spellName);
                }
        }

        public AbstractSpell[] getSpells(ElementType element) {
                return spellLists.get(element);
        }

        /**
         * Ignore events to prevent stack overflow;
         */
        public void ignoreEvents(boolean val) {
                ignoreEvents = val;
        }

        // Totem use functions. A positive check for the item being a totem is assumed.

        public boolean onSwitchTotem(Player player, ItemStack totem) {
                AbstractSpell currentSpell = getSpell(totem);
                AbstractSpell nextSpell = null;
                final ElementType element = Totem.getTotemType(totem);
                if (element == null) return false;
                final AbstractSpell[] spells = spellLists.get(element);
                if (currentSpell == null) {
                        // For a previously empty totem, look for
                        // the first activated spell.
                        for (AbstractSpell spell : spells) {
                                if (spell.hasActivated(player)) {
                                        nextSpell = spell;
                                        break;
                                }
                        }
                } else {
                        // A spell is already loaded on the
                        // totem. Attempt to find the next
                        // activated spell.
                        int index = currentSpell.getElementIndex();
                        for (int i = 0; i < spells.length; ++i) {
                                index += 1;
                                if (index >= spells.length) index = 0;
                                AbstractSpell spell = spells[index];
                                if (spell.hasActivated(player)) {
                                        nextSpell = spell;
                                        break;
                                }
                        }
                }
                if (nextSpell == null) return false;
                nextSpell.loadOnto(totem, player);
                return true;
        }

        /**
         * Check if a player can use a certain spell.
         */
        private boolean canUseSpell(Player player, AbstractSpell spell) {
                if (spell == null) return false;
                if (!spell.hasUnlocked(player)) return false;
                if (player.getLevel() < spell.getXPCost()) return false;
                //final int level = plugin.playerManager.getPlayerInfo(player).getElementalLevel(spell.getElement());
                return true;
        }

        /**
         * 
         */
        private void onUseSpell(Player player, Location loc, AbstractSpell spell) {
                int cost = spell.getXPCost();
                final PlayerInfo info = plugin.playerManager.getPlayerInfo(player);
                if (info.getPrimaryElement() == spell.getElement()) {
                        cost = plugin.proficiencySpellCostFactor.roll(cost);
                }
                player.setLevel(Math.max(0, player.getLevel() - spell.getXPCost()));
                String particleName = null;
                String soundName = null;
                switch (spell.getElement()) {
                case EARTH:
                        soundName = "dig.grass";
                        particleName = "blockcrack_2_0";
                        break;
                case FIRE:
                        soundName = "mob.ghast.fireball";
                        particleName = "flame";
                        break;
                case WATER:
                        soundName = "liquid.splash";
                        particleName = "splash";
                        break;
                case AIR:
                        soundName = "random.bow";
                        particleName = "blockcrack_18_0";
                        break;
                }
                player.playSound(loc, soundName, 0.5f, 1.0f);
                Util.playParticleEffect(player, loc, particleName, 64, 0.5f, 0.001f);
        }

        private void onFailSpell(Player player, Location loc, AbstractSpell spell) {
                player.playSound(loc, "liquid.lavapop", 0.66f, 0.66f);
        }

        public void onUseTotem(Player player, ItemStack totem) {
                AbstractSpell spell = getSpell(totem);
                if (canUseSpell(player, spell) && spell.cast(player)) {
                        onUseSpell(player, player.getEyeLocation(), spell);
                } else {
                        onFailSpell(player, player.getEyeLocation(), spell);
                }
        }

        public void onUseTotem(Player player, ItemStack totem, Entity entity) {
                AbstractSpell spell = getSpell(totem);
                Location loc = player.getLocation().add(0.5, 0.5, 0.5);
                if (canUseSpell(player, spell) && spell.cast(player, entity)) {
                        onUseSpell(player, loc, spell);
                } else {
                        onFailSpell(player, loc, spell);
                }
        }

        public void onUseTotem(Player player, ItemStack totem, Block block, BlockFace face) {
                AbstractSpell spell = getSpell(totem);
                Location loc = block.getRelative(face).getLocation().add(0.5, 0.5, 0.5);
                if (canUseSpell(player, spell) && spell.cast(player, block, face)) {
                        onUseSpell(player, loc, spell);
                } else {
                        onFailSpell(player, loc, spell);
                }
        }

        @SuppressWarnings("unchecked")
        public void loadSpells() {
                List<AbstractSpell> spells = new ArrayList<AbstractSpell>();
                ConfigurationSection config = getConfig();
                // Load all the spells and make sure the basic
                // configuration structure is okay.
                for (String key : config.getKeys(false)) {
                        ConfigurationSection section = config.getConfigurationSection(key);
                        if (section == null) {
                                plugin.getLogger().warning("[Spell] " + key + ": Not a section.");
                                continue;
                        }
                        String type = section.getString("Type");
                        if (type == null) {
                                plugin.getLogger().warning("[Spell] " + key + ": Missing type.");
                                continue;
                        }
                        String className = "com.winthier.skills.spell." + type + "Spell";
                        Class clazz = null;
                        try {
                                clazz = Class.forName(className);
                        } catch (Throwable t) {}
                        if (clazz == null) {
                                plugin.getLogger().warning("[Spell] " + key + ": Class not found: " + className);
                                continue;
                        }
                        Constructor ctor = null;
                        try {
                                ctor = clazz.getConstructor();
                        } catch (Throwable t) {}
                        if (ctor == null) {
                                plugin.getLogger().warning("[Spell] " + key + ": No suitable constructor found.");
                                continue;
                        }
                        Object o = null;
                        try {
                                o = ctor.newInstance();
                        } catch (Throwable t) {}
                        if (o == null || !(o instanceof AbstractSpell)) {
                                plugin.getLogger().warning("[Spell] " + key + ": Not an instance of AbstractSpell.");
                                continue;
                        }
                        AbstractSpell spell = (AbstractSpell)o;
                        spell.init(plugin, key);
                        spells.add(spell);
                }

                // Clear all available lists.
                spellByName.clear();
                spellLists.clear();
                // Prepare temporary spell lists.
                Map<ElementType, List<AbstractSpell>> tmpSpellList = new EnumMap<ElementType, List<AbstractSpell>>(ElementType.class);
                for (ElementType element : ElementType.values()) {
                        tmpSpellList.put(element, new ArrayList<AbstractSpell>(spells.size() / 3));
                }
                // Prepare all spells and put them into the temporary lists.
                for (AbstractSpell spell : spells) {
                        if (spell.loadConfig()) {
                                tmpSpellList.get(spell.getElement()).add(spell);
                                spellByName.put(spell.getName(), spell);
                        }
                }
                // Put them in the final lists, sort them and set the indices.
                for (ElementType element : ElementType.values()) {
                        AbstractSpell list[] = tmpSpellList.get(element).toArray(new AbstractSpell[0]);
                        spellLists.put(element, list);
                        // Arrays.<AbstractSpell>sort(list, new Comparator<AbstractSpell>() {
                        //                 public int compare(AbstractSpell o1, AbstractSpell o2) {
                        //                         return o1.getUnlockLevel(1) - o2.getUnlockLevel(1);
                        //                 }
                        //         });
                        int i = 0;
                        for (AbstractSpell spell : list) spell.setElementIndex(i++);
                }
        }
}
