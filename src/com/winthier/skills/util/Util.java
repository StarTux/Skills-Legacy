package com.winthier.skills.util;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.winthier.skills.SkillsPlugin;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public class Util {
        public final static Random random = new Random(System.currentTimeMillis());
        //public final static String ICON = "\u25A3"; // The filled square.
        public final static String ICON = "\u2726";

        final static int[] sqrtTable = {
                0,    16,  22,  27,  32,  35,  39,  42,  45,  48,  50,  53,  55,  57,
                59,   61,  64,  65,  67,  69,  71,  73,  75,  76,  78,  80,  81,  83,
                84,   86,  87,  89,  90,  91,  93,  94,  96,  97,  98,  99, 101, 102,
                103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118,
                119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132,
                133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145,
                146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155, 156, 157,
                158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
                169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178,
                179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188,
                189, 189, 190, 191, 192, 192, 193, 193, 194, 195, 195, 196, 197, 197,
                198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206,
                207, 208, 208, 209, 209, 210, 211, 211, 212, 212, 213, 214, 214, 215,
                215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223,
                224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230, 230, 231,
                231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238,
                239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246,
                246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253,
                253, 254, 254, 255
        };
        
        public static void copyResource(SkillsPlugin plugin, String filename, boolean force) {
                copyResource(plugin, filename, filename, force);
        }

        public static String shadowZeros(int number, int digits, ChatColor zeros, ChatColor nonzeros) {
                String result = "" + number;
                final String ZEROS = "000000000000000000000000000000000"; // xD
                if (digits > result.length()) {
                        return "" + zeros + ZEROS.substring(0, digits - result.length()) + nonzeros + result;
                } else {
                        return "" + nonzeros + result;
                }
        }

        public static String shadowZeros(String string, ChatColor zeros, ChatColor nonzeros) {
                Pattern pattern = Pattern.compile("^(0*)([^0].*)?$");
                Matcher matcher = pattern.matcher(string);
                if (!matcher.matches()) return "";
                return "" + zeros + matcher.group(1) + nonzeros + matcher.group(2);
        }

        public static void copyResource(SkillsPlugin plugin, String filename, String dest, boolean force) {
                File destFile = new File(plugin.getDataFolder(), dest);
                if (!force && destFile.exists()) return;
                InputStream in = plugin.getResource(filename);
                if (in == null) return;
                FileOutputStream out = null;
                try {
                        destFile.getParentFile().mkdirs();
                        out = new FileOutputStream(destFile);
                        int b;
                        while (-1 != (b = in.read())) {
                                out.write(b);
                        }
                } catch (IOException ioe) {
                        ioe.printStackTrace();
                        return;
                } finally {
                        try {
                                in.close();
                                if (out != null) out.close();
                        } catch (IOException ioe) {}
                }
        }

        public static int rollFraction(int coefficient, int dividend, int divisor) {
                if (divisor == 0) return 0;
                dividend *= coefficient;
                int result = dividend / divisor;
                final int remainder = dividend % divisor;
                if (remainder > 0 && random.nextInt(divisor) < remainder) result += 1;
                return result;
        }

        public static int distanceSquared(Location a, Location b) {
                if (a.getWorld() != b.getWorld()) return 0;
                final int x = b.getBlockX() - a.getBlockX();
                final int y = b.getBlockY() - a.getBlockY();
                final int z = b.getBlockZ() - a.getBlockZ();
                return x * x + y * y + z * z;
        }

        public static int distance(Location a, Location b) {
                return sqrt(distanceSquared(a, b));
        }

        public static int horizontalDistanceSquared(Location a, Location b) {
                if (a.getWorld() != b.getWorld()) return 0;
                final int x = b.getBlockX() - a.getBlockX();
                final int z = b.getBlockZ() - a.getBlockZ();
                return x * x + z * z;
        }

        public static int horizontalDistance(Location a, Location b) {
                return sqrt(horizontalDistanceSquared(a, b));
        }

        public static String format(String msg, Object... args) {
                msg = ChatColor.translateAlternateColorCodes('&', msg);
                msg = String.format(msg, args);
                return msg;
        }

        public static void sendMessage(CommandSender sender, String msg, Object... args) {
                sender.sendMessage(format(msg, args));
        }

        private static final String roman[] = { "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII" };
        public static String roman(int n) {
                if (n <= 0 || n > roman.length) return "" + n;
                return roman[n - 1];
        }

        public static int sqrt(int x) {
                int xn;
                if (x >= 0x10000) {
                        if (x >= 0x1000000) {
                                if (x >= 0x10000000) {
                                        if (x >= 0x40000000) {
                                                xn = sqrtTable[x >> 24] << 8;
                                        } else {
                                                xn = sqrtTable[x >> 22] << 7;
                                        }
                                } else {
                                        if (x >= 0x4000000) {
                                                xn = sqrtTable[x >> 20] << 6;
                                        } else {
                                                xn = sqrtTable[x >> 18] << 5;
                                        }
                                }
                                xn = (xn + 1 + (x / xn)) >> 1;
                                xn = (xn + 1 + (x / xn)) >> 1;
                                return ((xn * xn) > x) ? --xn : xn;
                        } else {
                                if (x >= 0x100000) {
                                        if (x >= 0x400000) {
                                                xn = sqrtTable[x >> 16] << 4;
                                        } else {
                                                xn = sqrtTable[x >> 14] << 3;
                                        }
                                } else {
                                        if (x >= 0x40000) {
                                                xn = sqrtTable[x >> 12] << 2;
                                        } else {
                                                xn = sqrtTable[x >> 10] << 1;
                                        }
                                }
                                xn = (xn + 1 + (x / xn)) >> 1;
                                return ((xn * xn) > x) ? --xn : xn;
                        }
                } else {
                        if (x >= 0x100) {
                                if (x >= 0x1000) {
                                        if (x >= 0x4000) {
                                                xn = (sqrtTable[x >> 8]) + 1;
                                        } else {
                                                xn = (sqrtTable[x >> 6] >> 1) + 1;
                                        }
                                } else {
                                        if (x >= 0x400) {
                                                xn = (sqrtTable[x >> 4] >> 2) + 1;
                                        } else {
                                                xn = (sqrtTable[x >> 2] >> 3) + 1;
                                        }
                                }
                                return ((xn * xn) > x) ? --xn : xn;
                        } else {
                                if (x >= 0) {
                                        return sqrtTable[x] >> 4;
                                }
                        }
                }
                throw new IllegalArgumentException("Attemt to take the square root of negative number");
        }

        public static ItemStack addGlow(ItemStack item) {
                item = MinecraftReflection.getBukkitItemStack(item);
                NbtCompound compound = (NbtCompound)NbtFactory.fromItemTag(item);
                compound.put(NbtFactory.ofList("ench"));
                return item;
        }

        public static ItemStack removeGlow(ItemStack item) {
                item = MinecraftReflection.getBukkitItemStack(item);
                NbtCompound compound = (NbtCompound)NbtFactory.fromItemTag(item);
                compound.remove("ench");
                return item;
        }

        public static int[] toIntArray(List<Integer> list) {
                int result[] = new int[list.size()];
                for (int i = 0; i < result.length; ++i) {
                        result[i] = list.get(i);
                }
                return result;
        }

        public static double[] toDoubleArray(List<Double> list) {
                double result[] = new double[list.size()];
                for (int i = 0; i < result.length; ++i) {
                        result[i] = list.get(i);
                }
                return result;
        }

        /**
         * Wrap around Enum.valueOf(), but be a little more
         * tolerant of the input.
         */
        public static <T extends Enum<T>> T enumFromString(Class<T> type, String name) {
                if (name == null) return null;
                name = name.toUpperCase().replaceAll("-", "_");
                try {
                        return Enum.valueOf(type, name);
                } catch (IllegalArgumentException iae) {
                        return null;
                }
        }

        public static void storeMetadata(SkillsPlugin plugin, Metadatable storage, String key, Object value) {
                storage.setMetadata(key, new FixedMetadataValue(plugin, value));
        }

        public static void removeMetadata(SkillsPlugin plugin, Metadatable storage, String key) {
                storage.removeMetadata(key, plugin);
        }

        public static Object getMetadata(SkillsPlugin plugin, Metadatable storage, String key) {
                for (MetadataValue val : storage.getMetadata(key)) {
                        if (val.getOwningPlugin() == plugin) return val.value();
                }
                return null;
        }

        public static void storeSourceLocation(SkillsPlugin plugin, Entity ent, Location loc) {
                ent.setMetadata("SourceLocation", new FixedMetadataValue(plugin, loc));
        }

        public static Location getSourceLocation(SkillsPlugin plugin, Entity ent) {
                for (MetadataValue val : ent.getMetadata("SourceLocation")) {
                        if (val.getOwningPlugin() == plugin) {
                                Object o = val.value();
                                if (o instanceof Location) return (Location)o;
                        }
                }
                return null;
        }

        public static int sourceDistance(SkillsPlugin plugin, Entity ent, Location loc) {
                Location src = getSourceLocation(plugin, ent);
                if (src == null) return 0;
                return distance(src, loc);
        }

        public static int horizontalSourceDistance(SkillsPlugin plugin, Entity ent, Location loc) {
                Location src = getSourceLocation(plugin, ent);
                if (src == null) return 0;
                return horizontalDistance(src, loc);
        }

        public static String locationToString(Location loc) {
                return String.format("%s %d,%d,%d", loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        public static List<String> fillParagraph(String par, int width, String prefix) {
                List<String> result = new ArrayList<String>();
                StringBuilder line = new StringBuilder(prefix);
                for (String word : par.split("\\s+")) {
                        if (line.length() + word.length() - prefix.length() + 1 > width) {
                                result.add(line.toString());
                                line = new StringBuilder(prefix).append(word);
                        } else {
                                if (line.length() > prefix.length()) line.append(" ");
                                line.append(word);
                        }
                }
                if (line.length() > 0) result.add(line.toString());
                return result;
        }

        public static void playParticleEffect(Player player, Location location, String particleName, int count, float offset, float speed) {
                try {
                        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
                        PacketContainer packet = protocolManager.createPacket(63);
                        packet.getStrings().write(0, particleName);
                        // location
                        packet.getFloat().write(0, (float)location.getX());
                        packet.getFloat().write(1, (float)location.getY());
                        packet.getFloat().write(2, (float)location.getZ());
                        // offset
                        packet.getFloat().write(3, offset);
                        packet.getFloat().write(4, offset);
                        packet.getFloat().write(5, offset);
                        // speed
                        packet.getFloat().write(6, speed);
                        // count
                        packet.getIntegers().write(0, count);
                        protocolManager.sendServerPacket(player, packet);
                } catch (Throwable t) {
                        t.printStackTrace();
                }
        }

        public static boolean canBuild(Player player, Block block) {
                // boolean result = true;

                // try {
                //         NCPExemptionManager.exemptPermanently(player);
                //         BlockBreakEvent event = new BlockBreakEvent(block, player);
                //         Bukkit.getServer().getPluginManager().callEvent(event);
                //         if (event.isCancelled()) result = false;
                // } finally {
                //         NCPExemptionManager.unexempt(player);
                // }

                // return result;
                final Location loc = block.getLocation();
                if (!WGBukkit.getPlugin().canBuild(player, loc)) return false;
                if (GriefPrevention.instance.allowBuild(player, loc) != null) return false;
                return true;
        }

        public static boolean canHurt(SkillsPlugin plugin, Player player, Entity entity) {
                EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, DamageCause.MAGIC, 0.0);
                plugin.spellManager.ignoreEvents(true);
                plugin.getServer().getPluginManager().callEvent(event);
                plugin.spellManager.ignoreEvents(false);
                return !event.isCancelled();
        }

        public static String niceItemName(ItemStack stack) {
                ItemInfo info = Items.itemByStack(stack);
                return info.getName();
        }

        public static ItemStack getMobHead(LivingEntity entity) {
                boolean valid = false;
                short damage = 0;
                String owner = null;
                String name = null;
                switch (entity.getType()) {
                case SKELETON:
                        final Skeleton skeleton = (Skeleton)entity;
                        if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER) {
                                //damage = (short)1;
                                // We don't want to give that out.
                                return null;
                        } else {
                                damage = (short)0;
                        }
                        valid = true;
                        break;
                case ZOMBIE:
                        final Zombie zombie = (Zombie)entity;
                        if (zombie.isBaby()) return null;
                        if (zombie.isVillager()) {
                                damage = (short)3;
                                owner = "MHF_Villager";
                                name = "Villager Head";
                                valid = true;
                        } else {
                                damage = (short)2;
                                valid = true;
                        }
                        break;
                case CREEPER:
                        damage = (short)4;
                        valid = true;
                        break;
                case BLAZE:
                        damage = (short)3;
                        owner = "MHF_Blaze";
                        name = "Blaze Head";
                        valid = true;
                        break;
                case CAVE_SPIDER:
                        damage = (short)3;
                        owner = "MHF_CaveSpider";
                        name = "Cave Spider Head";
                        valid = true;
                        break;
                case CHICKEN:
                        damage = (short)3;
                        owner = "MHF_Chicken";
                        name = "Chicken Head";
                        valid = true;
                        break;
                case COW:
                        damage = (short)3;
                        owner = "MHF_Cow";
                        name = "Cow Head";
                        valid = true;
                        break;
                case ENDERMAN:
                        damage = (short)3;
                        owner = "MHF_Enderman";
                        name = "Enderman Head";
                        valid = true;
                        break;
                case GHAST:
                        damage = (short)3;
                        owner = "MHF_Ghast";
                        name = "Ghast Head";
                        valid = true;
                        break;
                case IRON_GOLEM:
                        damage = (short)3;
                        owner = "MHF_Golem";
                        name = "Iron Golem Head";
                        valid = true;
                        break;
                case MAGMA_CUBE:
                        damage = (short)3;
                        owner = "MHF_LavaSlime";
                        name = "Magma Cube Head";
                        valid = true;
                        break;
                case MUSHROOM_COW:
                        damage = (short)3;
                        owner = "MHF_MushroomCow";
                        name = "Mushroom Cow Head";
                        valid = true;
                        break;
                case OCELOT:
                        damage = (short)3;
                        owner = "MHF_Ocelot";
                        name = "Ocelot Head";
                        valid = true;
                        break;
                case PIG:
                        damage = (short)3;
                        owner = "MHF_Pig";
                        name = "Pig Head";
                        valid = true;
                        break;
                case PIG_ZOMBIE:
                        damage = (short)3;
                        owner = "MHF_PigZombie";
                        name = "Pig Zombie Head";
                        valid = true;
                        break;
                case SHEEP:
                        damage = (short)3;
                        owner = "MHF_Sheep";
                        name = "Sheep Head";
                        valid = true;
                        break;
                case SLIME:
                        damage = (short)3;
                        owner = "MHF_Slime";
                        name = "Slime Head";
                        valid = true;
                        break;
                case SPIDER:
                        damage = (short)3;
                        owner = "MHF_Spider";
                        name = "Spider Head";
                        valid = true;
                        break;
                case SQUID:
                        damage = (short)3;
                        owner = "MHF_Squid";
                        name = "Squid Head";
                        valid = true;
                        break;
                case VILLAGER:
                        damage = (short)3;
                        owner = "MHF_Villager";
                        name = "Villager Head";
                        valid = true;
                        break;
                }
                if (!valid) return null;
                final ItemStack result = new ItemStack(Material.SKULL_ITEM, 1, damage);
                if (damage == (short)3) {
                        if (owner == null) return null;
                        final SkullMeta meta = (SkullMeta)result.getItemMeta();
                        meta.setOwner(owner);
                        if (name != null) {
                                meta.setDisplayName(ChatColor.RESET + name);
                        }
                        result.setItemMeta(meta);
                }
                return result;
        }
}
