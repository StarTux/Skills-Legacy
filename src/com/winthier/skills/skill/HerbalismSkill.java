package com.winthier.skills.skill;

//import org.bukkit.event.world.StructureGrowEvent;
import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.MaterialIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HerbalismSkill extends AbstractSkill {
        //private final EnumIntMap<TreeType> structureSpMap = new EnumIntMap<TreeType>(TreeType.class, 0);
        private final MaterialIntMap harvestSpMap = new MaterialIntMap(0);
        private final MaterialFractionMap fertilizeSpMap = new MaterialFractionMap(0);

        public HerbalismSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        // StructureGrowEvent is broken (getPlayer() always yields
        // null). See below for the replacement.
        // @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        // public void onStructureGrow(StructureGrowEvent event) {
        //         final Player player = event.getPlayer();
        //         if (player == null) return;
        //         // give sp
        //         final int skillPoints = structureSpMap.get(event.getSpecies());
        //         if (skillPoints > 0) addSkillPoints(player, skillPoints);
        // }

        /**
         * Check if a herbalism block is harvested.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onBlockBreak(BlockBreakEvent event) {
                final Player player = event.getPlayer();
                final Block block = event.getBlock();
                final Material mat = block.getType();
                // filter out stuff that isn't fully grown
                switch(mat) { case CROPS: case CARROT: case POTATO: if (block.getData() != 7) return; }
                // Check if a player placed it. Bonemealed crops won't work for now
                if (ExploitsPlugin.isPlayerPlaced(block)) return;
                // give sp
                final int skillPoints = harvestSpMap.get(mat);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
        }

        /**
         * This handler checks if bonemeal is used on a valid
         * block, and if the block is fully grown after, but not
         * before using the bonemeal.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
                final Player player = event.getPlayer();
                final ItemStack item = player.getItemInHand();
                if (item.getType() != Material.INK_SACK || item.getDurability() != 15) return;
                if (event.useItemInHand() == Result.DENY) return;
                final Block block = event.getClickedBlock();
                if (isFullyGrown(block)) return;
                final int skillPoints = fertilizeSpMap.get(block.getType());
                if (skillPoints <= 0) return;
                if (Util.random.nextInt(100) < getInstantBonemealChance(player)) {
                        if (setFullyGrown(block)) {
                                addSkillPoints(player, skillPoints);
                                return;
                        }
                }
                new BukkitRunnable() {
                        public void run() {
                                if (!isFullyGrown(block)) return;
                                addSkillPoints(player, skillPoints);
                        }
                }.runTask(plugin);
        }

        private int getInstantBonemealChance(Player player) {
                return Math.min(100, getSkillLevel(player) / 9);
        }

        private static boolean isFullyGrown(Block block) {
                final Material mat = block.getType();
                final int data = block.getData();
                switch(mat) {
                case CROPS:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                        return data == 7;
                case SAPLING:
                        return false;
                case LOG:
                        return true;
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                        return false;
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                        return true;
                default:
                        return false;
                }
        }

        private static boolean setFullyGrown(Block block) {
                final Material mat = block.getType();
                switch(mat) {
                case CROPS:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                        block.setData((byte)7);
                        return true;
                case SAPLING:
                        return growTree(block);
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                        return growMushroom(block);
                default:
                        return false;
                }
        }

        private static boolean growMushroom(Block block) {
                final Material id = block.getType();
                final byte data = block.getData();
                block.setType(Material.AIR);
                final World world = block.getWorld();
                final Location loc = block.getLocation();
                boolean result = false;
                switch (id) {
                case RED_MUSHROOM:
                        result = world.generateTree(loc, TreeType.RED_MUSHROOM);
                        break;
                case BROWN_MUSHROOM:
                        result = world.generateTree(loc, TreeType.BROWN_MUSHROOM);
                        break;
                }
                if (!result) {
                        block.setType(id);
                        block.setData(data);
                }
                return result;
        }

        private static boolean growTree(Block block) {
                final int data = block.getData();
                block.setType(Material.AIR);
                final World world = block.getWorld();
                final Location loc = block.getLocation();
                boolean result = false;
                switch (data & 0x03) {
                case 0: // Oak
                        result = world.generateTree(loc, TreeType.TREE);
                        if (!result) result = world.generateTree(loc, TreeType.BIG_TREE);
                        break;
                case 1: // Spruce
                        result = world.generateTree(loc, TreeType.REDWOOD);
                        break;
                case 2: // Birch
                        result = world.generateTree(loc, TreeType.BIRCH);
                        break;
                case 3: // Jungle
                        result = growBigJungleTree(block);
                        if (!result) result = world.generateTree(loc, TreeType.SMALL_JUNGLE);
                }
                if (!result) {
                        block.setType(Material.SAPLING);
                        block.setData((byte)data);
                }
                return result;
        }

        private static boolean growBigJungleTree(Block block) {
                final World world = block.getWorld();
                boolean result = false;
                final Block north = block.getRelative(BlockFace.NORTH);
                final Block east = block.getRelative(BlockFace.EAST);
                final Block northEast = block.getRelative(BlockFace.NORTH_EAST);
                if (isJungleSapling(north) && isJungleSapling(east) && isJungleSapling(northEast)) {
                        result = growBigJungleTree(north, northEast, east, block);
                        if (result) return result;
                }
                final Block south = block.getRelative(BlockFace.SOUTH);
                final Block southEast = block.getRelative(BlockFace.SOUTH_EAST);
                if (isJungleSapling(south) && isJungleSapling(east) && isJungleSapling(southEast)) {
                        result = growBigJungleTree(block, east, southEast, south);
                        if (result) return result;
                }
                final Block west = block.getRelative(BlockFace.WEST);
                final Block southWest = block.getRelative(BlockFace.SOUTH_WEST);
                if (isJungleSapling(south) && isJungleSapling(west) && isJungleSapling(southWest)) {
                        result = growBigJungleTree(west, block, south, southWest);
                        if (result) return result;
                }
                final Block northWest = block.getRelative(BlockFace.NORTH_WEST);
                if (isJungleSapling(north) && isJungleSapling(west) && isJungleSapling(northWest)) {
                        result = growBigJungleTree(northWest, north, block, west);
                        if (result) return result;
                }
                return result;
        }

        private static boolean growBigJungleTree(Block northWest, Block northEast, Block southEast, Block southWest) {
                int id[] = new int[4];
                byte data[] = new byte[4];
                id[0] = northWest.getTypeId();
                id[1] = northEast.getTypeId();
                id[2] = southEast.getTypeId();
                id[3] = southWest.getTypeId();
                data[0] = northWest.getData();
                data[1] = northEast.getData();
                data[2] = southEast.getData();
                data[3] = southWest.getData();
                northWest.setType(Material.AIR);
                northEast.setType(Material.AIR);
                southEast.setType(Material.AIR);
                southWest.setType(Material.AIR);
                if (northWest.getWorld().generateTree(northWest.getLocation(), TreeType.JUNGLE)) return true;
                northWest.setTypeIdAndData(id[0], data[0], false);
                northEast.setTypeIdAndData(id[1], data[1], false);
                southEast.setTypeIdAndData(id[2], data[2], false);
                southWest.setTypeIdAndData(id[3], data[3], false);
                return false;
        }

        private static boolean isJungleSapling(Block block) {
                return (block.getType() == Material.SAPLING && (int)(block.getData() & 3) ==  3);
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Bonemeal has a " + getInstantBonemealChance(player) + "% chance of working instantly");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                fertilizeSpMap.load(getConfig().getConfigurationSection("sp.fertilize"));
                harvestSpMap.load(getConfig().getConfigurationSection("sp.harvest"));
                //structureSpMap.load(TreeType.class, getConfig().getConfigurationSection("sp.structures"));
        }
}
