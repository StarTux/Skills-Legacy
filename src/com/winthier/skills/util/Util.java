package com.winthier.skills.util;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.winthier.skills.SkillsPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Util {
        public final static Random random = new Random(System.currentTimeMillis());
        public final static String ICON = "\u25A3";

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
                final int x = b.getBlockX() - a.getBlockX();
                final int y = b.getBlockY() - a.getBlockY();
                final int z = b.getBlockZ() - a.getBlockZ();
                return x * x + y * y + z * z;
        }

        public static int distance(Location a, Location b) {
                return sqrt(distanceSquared(a, b));
        }

        public static int horizontalDistanceSquared(Location a, Location b) {
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
}
