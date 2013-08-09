package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class AlchemySkill extends AbstractSkill {
        private int cachedAmount = -1;
        private MaterialFractionMap furnaceSpMap = new MaterialFractionMap(0);
        private MaterialFractionMap blastSpMap = new MaterialFractionMap(0);
        private List<ItemStack> pickaxes = new ArrayList<ItemStack>();

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
                //player.sendMessage("amount=" + amount + ", cachedAmount=" + cachedAmount);
                if (cachedAmount >= 0) {
                        amount = cachedAmount - amount;
                }
                if (amount == 0) return;
                // give sp
                final int skillPoints = furnaceSpMap.roll(mat, amount);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
                // Give xp bonus.
                event.setExpToDrop(multiplyXp(player, event.getExpToDrop()));
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEntityExplode(EntityExplodeEvent event) {
                if (!(event.getEntity() instanceof TNTPrimed)) return;
                final TNTPrimed tnt = (TNTPrimed)event.getEntity();
                if (!(tnt.getSource() instanceof Player)) return;
                final Player player = (Player)tnt.getSource();
                for (Block block : event.blockList()) {
                        // Give skill points.
                        int sp = blastSpMap.roll(block.getType(), 1);
                        if (sp > 0) addSkillPoints(player, sp);
                }
                int fortune = getBlastFortune(player);
                if (fortune > 0) {
                        final ItemStack pickaxe = getPickaxe(fortune);
                        for (Block block : event.blockList()) {
                                player.sendMessage("Breaking " + block.getType().name() + " with Fortune " + fortune);
                                block.breakNaturally(pickaxe);
                                pickaxe.setDurability((short)0);
                        }
                        event.blockList().clear();
                }
        }

        private int getBlastFortune(Player player) {
                final int level = getSkillLevel(player);
                if (level > 600) return 3;
                if (level > 300) return 2;
                if (level > 100) return 1;
                return 0;
        }

        private ItemStack getPickaxe(int fortune) {
                if (fortune < 0) throw new IllegalArgumentException("Fortune level must be at least 1.");
                ItemStack item;
                for (int i = pickaxes.size(); i <= fortune; ++i) {
                        item = new ItemStack(Material.DIAMOND_PICKAXE);
                        if (i > 0) {
                                item.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, i);
                        }
                        pickaxes.add(item);
                }
                return pickaxes.get(fortune);
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Smelted items drop +" + (getXpMultiplier(player) - 100) + "% XP");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                furnaceSpMap.load(getConfig().getConfigurationSection("sp.smelting"));
                blastSpMap.load(getConfig().getConfigurationSection("sp.blasting"));
        }
}
