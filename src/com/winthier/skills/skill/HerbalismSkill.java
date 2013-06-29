package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.MaterialIntMap;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class HerbalismSkill extends AbstractSkill {
        private final EnumIntMap<TreeType> structureSpMap = new EnumIntMap<TreeType>(TreeType.class, 0);
        private final MaterialIntMap blockSpMap = new MaterialIntMap(0);

        public HerbalismSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onStructureGrow(StructureGrowEvent event) {
                final Player player = event.getPlayer();
                if (player == null) return;
                if (!canCollectSkillPoints(player)) return;
                // give sp
                player.sendMessage(event.getSpecies().name());
                final int skillPoints = structureSpMap.get(event.getSpecies());
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onBlockBreak(BlockBreakEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                final Block block = event.getBlock();
                final Material mat = block.getType();
                // filter out stuff that isn't fully grown
                switch(mat) { case CROPS: case CARROT: case POTATO: if (block.getData() != 7) return; }
                // Check if a player placed it. Bonemealed crops won't work for now
                if (ExploitsPlugin.isPlayerPlaced(block)) return;
                // give sp
                final int skillPoints = blockSpMap.get(mat);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
        }

        @Override
        public void loadConfiguration() {
                structureSpMap.load(TreeType.class, getConfig().getConfigurationSection("sp.structures"));
                blockSpMap.load(getConfig().getConfigurationSection("sp.blocks"));
        }
}
