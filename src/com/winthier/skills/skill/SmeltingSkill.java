package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SmeltingSkill extends AbstractSkill {
        private int cachedAmount = -1;
        private MaterialFractionMap spMap = new MaterialFractionMap(0);
        private MaterialIntMap xpMap = new MaterialIntMap(-1);

        public SmeltingSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        /**
         * FurnaceExtractEvent is bugged, so we need to lusten to
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
                final int skillPoints = spMap.roll(mat, amount);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
                // set xp
                int xp = xpMap.get(mat);
                if (xp < 0) xp = event.getExpToDrop();
                event.setExpToDrop(multiplyXp(player, xp));
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
                spMap.load(getConfig().getConfigurationSection("sp"));
                xpMap.load(getConfig().getConfigurationSection("xp"));
        }
}
