package com.winthier.skills.skill;

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
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class HerbalismSkill extends AbstractSkill {
        // Skill Points
        private final EnumIntMap<TreeType> structureSpMap = new EnumIntMap<TreeType>(TreeType.class, 0);
        private final MaterialFractionMap harvestSpMap = new MaterialFractionMap(0);
        private final MaterialFractionMap fertilizeSpMap = new MaterialFractionMap(0);
        // Experience Points
        private final EnumIntMap<TreeType> structureXPMap = new EnumIntMap<TreeType>(TreeType.class, 0);
        private final MaterialFractionMap harvestXPMap = new MaterialFractionMap(0);
        private final MaterialFractionMap fertilizeXPMap = new MaterialFractionMap(0);

        public HerbalismSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onStructureGrow(StructureGrowEvent event) {
                final Player player = event.getPlayer();
                if (player == null) return;
                onTreeGrow(player, event.getSpecies());
        }

        public void onTreeGrow(Player player, TreeType species) {
                if (player == null) return;

                // Give SP.
                final int sp = structureSpMap.get(species);
                addSkillPoints(player, sp);

                // Give bonus XP.
                if (plugin.perksEnabled) {
                        final int xp = structureXPMap.get(species);
                        if (xp > 0) player.giveExp(multiplyXp(player, xp));
                }
        }

        /**
         * Check if a herbalism block is harvested.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onBlockBreak(BlockBreakEvent event) {
                final Player player = event.getPlayer();
                final Block block = event.getBlock();
                final Material mat = block.getType();

                // Filter out stuff that isn't fully grown.
                if (!canHarvest(block)) return;

                // Check if a player placed it. Bonemealed crops won't work for now.
                if (ExploitsPlugin.isPlayerPlaced(block)) return;

                // Give SP.
                final int skillPoints = harvestSpMap.get(mat);
                addSkillPoints(player, skillPoints);

                // Give bonus XP.
                if (plugin.perksEnabled) {
                        final int xp = harvestXPMap.get(mat);
                        if (xp > 0) player.giveExp(multiplyXp(player, xp));
                }
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
                final Material mat = block.getType();
                final int skillPoints = fertilizeSpMap.get(mat);

                // Give insta grow chance.
                if (plugin.perksEnabled && Util.random.nextInt(100) < getInstantBonemealChance(player)) {
                        if (setFullyGrown(block, player)) {
                                addSkillPoints(player, skillPoints);
                                int amount = item.getAmount() - 1;
                                if (amount > 0) {
                                        item.setAmount(amount);
                                } else {
                                        player.setItemInHand(null);
                                }
                                return;
                        }
                }

                // Give skill points if the plant grows.
                if (skillPoints <= 0) return;
                new BukkitRunnable() {
                        public void run() {
                                if (!isFullyGrown(block)) return;
                                addSkillPoints(player, skillPoints);
                                ExploitsPlugin.setPlayerPlaced(block, false);

                                // Give bonux XP.
                                if (plugin.perksEnabled) {
                                        final int xp = fertilizeXPMap.get(mat);
                                        if (xp > 0) player.giveExp(multiplyXp(player, xp));
                                }
                        }
                }.runTask(plugin);
        }

        private int getInstantBonemealChance(Player player) {
                return Math.min(100, getSkillLevel(player) / 9);
        }

        private static boolean canHarvest(Block block) {
                final Material mat = block.getType();
                final int data = block.getData();
                switch(mat) {
                case CROPS:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                        return data == 7;
                default:
                        return true;
                }
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
                case LEAVES:
                case SUGAR_CANE_BLOCK:
                case PUMPKIN:
                case MELON_BLOCK:
                case WATER_LILY:
                case YELLOW_FLOWER:
                case RED_ROSE:
                case VINE:
                case CACTUS:
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

        private boolean setFullyGrown(Block block, Player player) {
                final Material mat = block.getType();
                switch(mat) {
                case CROPS:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                        block.setData((byte)7);
                        ExploitsPlugin.setPlayerPlaced(block, false);
                        return true;
                case SAPLING:
                        return growTree(block, player);
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                        return growMushroom(block, player);
                default:
                        return false;
                }
        }

        private boolean growMushroom(Block block, Player player) {
                final Material id = block.getType();
                final byte data = block.getData();
                block.setType(Material.AIR);
                final World world = block.getWorld();
                final Location loc = block.getLocation();
                boolean result = false;
                switch (id) {
                case RED_MUSHROOM:
                        result = world.generateTree(loc, TreeType.RED_MUSHROOM);
                        //if (result) onTreeGrow(player, TreeType.RED_MUSHROOM);
                        break;
                case BROWN_MUSHROOM:
                        result = world.generateTree(loc, TreeType.BROWN_MUSHROOM);
                        //if (result) onTreeGrow(player, TreeType.BROWN_MUSHROOM);
                        break;
                }
                if (!result) {
                        block.setType(id);
                        block.setData(data);
                }
                if (result) ExploitsPlugin.setPlayerPlaced(block, false);
                return result;
        }

        private boolean growTree(Block block, Player player) {
                final int data = block.getData();
                block.setType(Material.AIR);
                final World world = block.getWorld();
                final Location loc = block.getLocation();
                final TreeType species;
                boolean result = false;
                switch (data & 0x03) {
                case 0: // Oak
                        result = world.generateTree(loc, TreeType.TREE);
                        //if (result) onTreeGrow(player, TreeType.TREE);
                        if (!result) result = world.generateTree(loc, TreeType.BIG_TREE);
                        //if (result) onTreeGrow(player, TreeType.BIG_TREE);
                        break;
                case 1: // Spruce
                        result = world.generateTree(loc, TreeType.REDWOOD);
                        //if (result) onTreeGrow(player, TreeType.REDWOOD);
                        break;
                case 2: // Birch
                        result = world.generateTree(loc, TreeType.BIRCH);
                        //if (result) onTreeGrow(player, TreeType.BIRCH);
                        break;
                case 3: // Jungle
                        result = growBigJungleTree(block);
                        //if (result) onTreeGrow(player, TreeType.JUNGLE);
                        if (!result) result = world.generateTree(loc, TreeType.SMALL_JUNGLE);
                        //if (result) onTreeGrow(player, TreeType.SMALL_JUNGLE);
                }
                if (!result) {
                        block.setType(Material.SAPLING);
                        block.setData((byte)data);
                } else {
                        ExploitsPlugin.setPlayerPlaced(block, false);
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
                Material mat[] = new Material[4];
                byte data[] = new byte[4];
                mat[0] = northWest.getType();
                mat[1] = northEast.getType();
                mat[2] = southEast.getType();
                mat[3] = southWest.getType();
                data[0] = northWest.getData();
                data[1] = northEast.getData();
                data[2] = southEast.getData();
                data[3] = southWest.getData();
                northWest.setType(Material.AIR);
                northEast.setType(Material.AIR);
                southEast.setType(Material.AIR);
                southWest.setType(Material.AIR);
                if (northWest.getWorld().generateTree(northWest.getLocation(), TreeType.JUNGLE)) return true;
                // Reset
                northWest.setType(mat[0]);
                northEast.setType(mat[1]);
                southEast.setType(mat[2]);
                southWest.setType(mat[3]);

                northWest.setData(data[0]);
                northEast.setData(data[1]);
                southEast.setData(data[2]);
                southWest.setData(data[3]);
                
                // northWest.setTypeIdAndData(id[0], data[0], false);
                // northEast.setTypeIdAndData(id[1], data[1], false);
                // southEast.setTypeIdAndData(id[2], data[2], false);
                // southWest.setTypeIdAndData(id[3], data[3], false);
                return false;
        }

        private static boolean isJungleSapling(Block block) {
                return (block.getType() == Material.SAPLING && (int)(block.getData() & 3) ==  3);
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Bonemeal has a " + getInstantBonemealChance(player) + "% chance of working instantly");
                result.add("Fertilizing or harvesting plants yields +" + (getXpMultiplier(player) - 100) + "% XP");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                // Load skill points.
                fertilizeSpMap.load(getConfig().getConfigurationSection("sp.fertilize"));
                harvestSpMap.load(getConfig().getConfigurationSection("sp.harvest"));
                structureSpMap.load(getConfig().getConfigurationSection("sp.structures"));
                // Load experience points.
                structureXPMap.load(getConfig().getConfigurationSection("xp.structures"));
                harvestXPMap.load(getConfig().getConfigurationSection("xp.harvest"));
                fertilizeXPMap.load(getConfig().getConfigurationSection("xp.fertilize"));
        }
}
