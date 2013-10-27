package com.winthier.skills.spell;

import com.winthier.skills.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LightningSpell extends AbstractSpell {
        @Override
        public boolean cast(Player player) {
                Block block = player.getTargetBlock(null, 32);
                if (block == null || block.getType() == Material.AIR) return false;
                return cast(player, block.getRelative(BlockFace.UP));
        }

        @Override
        public boolean cast(Player player, Entity entity) {
                return cast(player, entity.getLocation().getBlock());
        }

        @Override
        public boolean cast(Player player, Block block, BlockFace face) {
                block = block.getRelative(face);
                return cast(player, block);
        }

        private boolean cast(Player player, Block block) {
                plugin.magicWatchdog.setBlockLightningFire(true);
                if (Util.canBuild(player, block)) {
                        block.getWorld().strikeLightning(block.getLocation());
                } else {
                        block.getWorld().strikeLightningEffect(block.getLocation());
                }
                plugin.magicWatchdog.setBlockLightningFire(false);

                return true;
        }

        @Override
        public String getLevelDescription(int level) {
                return null;
        }

        @Override
        public boolean loadConfiguration(ConfigurationSection config) {
                return true;
        }
}
