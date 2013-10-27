package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AlchemySkill extends AbstractSkill {
        private int cachedAmount = -1;
        private MaterialFractionMap furnaceSpMap = new MaterialFractionMap(0);
        private MaterialFractionMap blastSpMap = new MaterialFractionMap(0);
        private MaterialFractionMap brewingSpMap = new MaterialFractionMap(0);
        private MaterialFractionMap brewingXpMap = new MaterialFractionMap(0);

        public AlchemySkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        /**
         * FurnaceExtractEvent is bugged, so we need to listen to
         * InventoryClickEvent, which will be called before the
         * former, and record the amount.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onInventoryClick(InventoryClickEvent event) {
                cachedAmount = -1;
                if (event.getInventory().getType() != InventoryType.FURNACE) return;
                if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
                if (!event.isShiftClick()) return;
                if (!(event.getWhoClicked() instanceof Player)) return;
                Player player = (Player)event.getWhoClicked();
                final ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR) {
                        cachedAmount = item.getAmount();
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onFurnaceExtract(FurnaceExtractEvent event) {
                // This event is sometimes called twice, so take care of that.
                int cachedAmount = this.cachedAmount;
                this.cachedAmount = -2;
                if (cachedAmount == -2) return;

                final Player player = event.getPlayer();
                final Material mat = event.getItemType();
                int amount = event.getItemAmount();
                if (cachedAmount >= 0) {
                        amount = cachedAmount - amount;
                }
                if (amount == 0) return;

                // Give SP.
                final int skillPoints = furnaceSpMap.roll(mat, amount);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);

                // Give XP bonus.
                if (plugin.perksEnabled) {
                        event.setExpToDrop(multiplyXp(player, event.getExpToDrop()));
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEntityExplode(EntityExplodeEvent event) {
                if (!(event.getEntity() instanceof TNTPrimed)) return;
                final TNTPrimed tnt = (TNTPrimed)event.getEntity();
                if (!(tnt.getSource() instanceof Player)) return;
                final Player player = (Player)tnt.getSource();

                // Give SP.
                for (Block block : event.blockList()) {
                        if (ExploitsPlugin.isPlayerPlaced(block)) continue;
                        final int sp = blastSpMap.roll(block.getType(), 1);
                        addSkillPoints(player, sp);
                }
        }

        public void setBrewer(Block block, Player player) {
                if (player == null) {
                        Util.removeMetadata(plugin, block, "Brewer");
                        return;
                }
                Util.storeMetadata(plugin, block, "Brewer", player.getName());
        }

        public Player getBrewer(Block block) {
                final Object o = Util.getMetadata(plugin, block, "Brewer");
                if (o == null || !(o instanceof String)) return null;
                final String name = (String)o;
                final Player player = plugin.getServer().getPlayerExact(name);
                return player;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getClickedBlock().getType() != Material.BREWING_STAND) return;
                setBrewer(event.getClickedBlock(), event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onBrew(BrewEvent event) {
                final Player player = getBrewer(event.getBlock());
                if (player == null) return;
                final ItemStack ingredient = event.getContents().getIngredient();
                if (ingredient == null) return;

                // Give SP
                final int sp = brewingSpMap.get(ingredient.getType());
                addSkillPoints(player, sp);

                // Give XP
                if (plugin.perksEnabled) {
                        int xp = brewingXpMap.get(ingredient.getType());
                        xp = multiplyXp(player, xp);
                        if (xp > 0) player.giveExp(xp);
                }
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Smelted items and brewed potions drop +" + (getXpMultiplier(player) - 100) + "% xp");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                furnaceSpMap.load(getConfig().getConfigurationSection("sp.smelting"));
                blastSpMap.load(getConfig().getConfigurationSection("sp.blasting"));
                brewingSpMap.load(getConfig().getConfigurationSection("sp.brewing"));
                brewingXpMap.load(getConfig().getConfigurationSection("xp.brewing"));
        }
}
