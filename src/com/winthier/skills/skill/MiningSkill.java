package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningSkill extends AbstractSkill {
        private final MaterialFractionMap spMap = new MaterialFractionMap(0);
        private final MaterialIntMap xpMap = new MaterialIntMap(-1);

        public MiningSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onBlockBreak(BlockBreakEvent event) {
                final Player player = event.getPlayer();
                final Block block = event.getBlock();
                final boolean playerPlaced = ExploitsPlugin.isPlayerPlaced(block);
                final Material mat = block.getType();
                if (block.getDrops(player.getItemInHand()).isEmpty()) return;
                // give sp
                if (!playerPlaced) {
                        final int skillPoints = spMap.roll(mat, 1);
                        if (skillPoints > 0) addSkillPoints(player, skillPoints);
                }
                // set xp
                // If the xp map overrides the value and the block
                // is not player placed, we use that
                // value. Otherwise, we trust Minecraft.
                int xp = -1;
                xp = xpMap.get(mat);
                if (xp < 0 || playerPlaced) xp = event.getExpToDrop();
                event.setExpToDrop(multiplyXp(player, xp));
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Broken blocks drop +" + (getXpMultiplier(player) - 100) + "% XP");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                spMap.load(getConfig().getConfigurationSection("sp"));
                xpMap.load(getConfig().getConfigurationSection("xp"));
        }
}
