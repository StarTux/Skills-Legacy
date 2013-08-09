package com.winthier.skills.spell;

import com.winthier.skills.ElementType;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

public class SpellManager implements Listener {
        private final SkillsPlugin plugin;
        private final Map<String, AbstractSpell> spellByName = new HashMap<String, AbstractSpell>();
        private final Map<ElementType, AbstractSpell[]> spellLists = new EnumMap<ElementType, AbstractSpell[]>(ElementType.class);

        public SpellManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                loadSpells();
                plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        public void onDisable() {
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onPlayerInteract(PlayerInteractEvent event) {
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
                        onUseTotem(player, item, event.getClickedBlock());
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
                final Player player = event.getPlayer();
                final ItemStack item = player.getItemInHand();
                if (item == null) return;
                if (!Totem.isTotem(item)) return;
                event.setCancelled(true);
                // PlayerInteractEntityEvent will take over from here.
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOW)
        public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
                if (!(event.getDamager() instanceof Player)) return;
                final Player player = (Player)event.getDamager();
                ItemStack item = player.getItemInHand();
                if (item == null) return;
                if (!Totem.isTotem(item)) return;
                event.setCancelled(true);
                onUseTotem(player, item, event.getEntity());
        }

        /**
         * Find the spell that is loaded on a totem.
         * @return The spell or null if none is loaded or the
         * loaded spell is invalid.
         */
        public AbstractSpell getSpell(ItemStack totem) {
                ItemMeta meta = totem.getItemMeta();
                List<String> lore = meta.getLore();
                if (lore.size() < 2) {
                        return null;
                } else {
                        String spellName = ChatColor.stripColor(lore.get(1));
                        return spellByName.get(spellName);
                }
        }

        public void setSpell(ItemStack totem, AbstractSpell spell) {
                ItemMeta meta = totem.getItemMeta();
                meta.setLore(Arrays.<String>asList(Totem.TOTEM_LORE_MAGIC, spell.totemName));
                meta.setDisplayName(spell.getTotemDisplayName());
                totem.setItemMeta(meta);
        }

        public AbstractSpell[] getSpells(ElementType element) {
                return spellLists.get(element);
        }

        // Totem use functions. A positive check for the item being a totem is assumed.

        public void onSwitchTotem(Player player, ItemStack totem) {
                AbstractSpell currentSpell = getSpell(totem);
                AbstractSpell nextSpell = null;
                if (currentSpell == null) {
                        final ElementType element = Totem.getTotemType(totem);
                        if (element == null) return;
                        nextSpell = spellLists.get(element)[0];
                } else {
                        final ElementType element = currentSpell.getElement();
                        final int index = currentSpell.getElementIndex() + 1;
                        final AbstractSpell[] spells = spellLists.get(element);
                        if (index < spells.length) {
                                nextSpell = spells[index];
                        } else {
                                nextSpell = spells[0];
                        }
                }
                setSpell(totem, nextSpell);
        }

        /**
         * Check if a player can use a certain spell.
         */
        private boolean canUseSpell(Player player, AbstractSpell spell) {
                if (spell == null) return false;
                if (player.getLevel() < spell.getXpCost()) return false;
                final int level = plugin.playerManager.getPlayerInfo(player).getElementalLevel(spell.getElement());
                if (level < spell.getMinLevel()) return false;
                return true;
        }

        /**
         * 
         */
        private void onUseSpell(Player player, AbstractSpell spell) {
                player.setLevel(Math.max(0, player.getLevel() - spell.getXpCost()));
                String particleName = null;
                String soundName = null;
                switch (spell.getElement()) {
                case EARTH:
                        soundName = "dig.grass";
                        particleName = "tilecrack_2_0";
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
                        particleName = "tilecrack_18_0";
                        break;
                }
                player.playSound(player.getEyeLocation(), soundName, 1.0f, 1.0f);
                Util.playParticleEffect(player, player.getEyeLocation(), particleName, 64, 0.5f, 0.01f);
        }

        public void onUseTotem(Player player, ItemStack totem) {
                AbstractSpell spell = getSpell(totem);
                if (canUseSpell(player, spell) && spell.cast(player)) {
                        onUseSpell(player, spell);
                } else {
                }
        }

        public void onUseTotem(Player player, ItemStack totem, Entity entity) {
                AbstractSpell spell = getSpell(totem);
                if (canUseSpell(player, spell) && spell.cast(player, entity)) {
                        onUseSpell(player, spell);
                } else {
                }
        }

        public void onUseTotem(Player player, ItemStack totem, Block block) {
                AbstractSpell spell = getSpell(totem);
                if (canUseSpell(player, spell) && spell.cast(player, block)) {
                        onUseSpell(player, spell);
                } else {
                }
        }

        public void loadSpells() {
                AbstractSpell[] spells = {
                        // Basic spells most players will get
                        // first. They correspond to the potion
                        // effect that comes most natural with the
                        // relevent element.
                        new PotionEffectSpell(plugin, ElementType.WATER, "health", PotionEffectType.HEAL),
                        new PotionEffectSpell(plugin, ElementType.WATER, "regeneration", PotionEffectType.REGENERATION),

                        new PotionEffectSpell(plugin, ElementType.FIRE,  "increase-damage", PotionEffectType.INCREASE_DAMAGE),
                        new PotionEffectSpell(plugin, ElementType.FIRE,  "fire-resistance", PotionEffectType.FIRE_RESISTANCE),

                        new PotionEffectSpell(plugin, ElementType.EARTH, "fast-digging", PotionEffectType.FAST_DIGGING),
                        new PotionEffectSpell(plugin, ElementType.EARTH, "damage-resistance", PotionEffectType.DAMAGE_RESISTANCE),

                        new PotionEffectSpell(plugin, ElementType.AIR,   "speed", PotionEffectType.SPEED),
                        new PotionEffectSpell(plugin, ElementType.AIR,   "jump", PotionEffectType.JUMP)
                };
                // Clear all available lists.
                spellByName.clear();
                spellLists.clear();
                // Prepare temporary spell lists.
                Map<ElementType, List<AbstractSpell>> tmpSpellList = new EnumMap<ElementType, List<AbstractSpell>>(ElementType.class);
                for (ElementType element : ElementType.values()) {
                        tmpSpellList.put(element, new ArrayList<AbstractSpell>(spells.length / 4));
                }
                // Prepare all spells and put them into the temporary lists.
                for (AbstractSpell spell : spells) {
                        spell.loadConfig();
                        tmpSpellList.get(spell.getElement()).add(spell);
                        spellByName.put(spell.getName(), spell);
                }
                // Put them in the final lists, sort them and set the indices.
                for (ElementType element : ElementType.values()) {
                        AbstractSpell list[] = tmpSpellList.get(element).toArray(new AbstractSpell[0]);
                        spellLists.put(element, list);
                        Arrays.<AbstractSpell>sort(list, new Comparator<AbstractSpell>() {
                                        public int compare(AbstractSpell o1, AbstractSpell o2) {
                                                return o1.minElementLevel - o2.minElementLevel;
                                        }
                                });
                        int i = 0;
                        for (AbstractSpell spell : list) spell.setElementIndex(i++);
                }
        }
}
