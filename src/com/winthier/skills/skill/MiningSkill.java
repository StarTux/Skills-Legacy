package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialIntMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class MiningSkill extends AbstractSkill {
        private final MaterialIntMap spMap = new MaterialIntMap(0);
        private final MaterialIntMap xpMap = new MaterialIntMap(-1);

        public MiningSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onBlockBreak(BlockBreakEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                final Block block = event.getBlock();
                final boolean playerPlaced = ExploitsPlugin.isPlayerPlaced(block);
                final Material mat = block.getType();
                // give sp
                int skillPoints = 0;
                if (!playerPlaced) {
                        skillPoints = spMap.get(mat);
                        if (skillPoints > 0) addSkillPoints(player, skillPoints);
                }
                // set xp
                int xp = -1;
                if (!playerPlaced) xp = xpMap.get(mat);
                if (xp < 0) xp = event.getExpToDrop();
                event.setExpToDrop(multiplyXp(player, xp));
        }

        // Configuration functions

        @Override
        public void loadConfiguration() {
                spMap.load(getConfig().getConfigurationSection("sp"));
                xpMap.load(getConfig().getConfigurationSection("xp"));
        }
}
