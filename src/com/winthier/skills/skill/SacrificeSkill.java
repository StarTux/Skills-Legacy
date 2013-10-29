package com.winthier.skills.skill;

// import org.bukkit.event.entity.ItemSpawnEvent;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.MaterialFractionMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class SacrificeSkill extends AbstractSkill {
        private final MaterialFractionMap spMap = new MaterialFractionMap(1, 64);
        private final MaterialFractionMap xpMap = new MaterialFractionMap(1, 16);
        private final Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
        private final Map<String, ItemStack[]> armorContents = new HashMap<String, ItemStack[]>();

        public SacrificeSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        public final void setDropper(Item item, Player player) {
                Util.storeMetadata(plugin, item, "Dropper", player);
        }

        public final Player getDropper(Item item) {
                Object o = Util.getMetadata(plugin, item, "Dropper");
                if (o != null && o instanceof Player) return (Player)o;
                return null;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerDropItem(PlayerDropItemEvent event) {
                setDropper(event.getItemDrop(), event.getPlayer());
        }

        // // Death Drops
        // private Player lastDeathPlayer = null;
        // private long lastDeathTime = 0L;
        // @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        // public void onPlayerDeath(PlayerDeathEvent event) {
        //         lastDeathPlayer = event.getEntity();
        //         lastDeathTime = event.getEntity().getWorld().getFullTime();
        // }

        // @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        // public void onItemSpawn(ItemSpawnEvent event) {
        //         if (lastDeathPlayer == null) return;
        //         if (lastDeathTime != event.getEntity().getWorld().getFullTime()) return;
        //         setDropper(event.getEntity(), lastDeathPlayer);
        // }

        public int getKeepItemPercentage(Player player) {
                return Math.min(50, getSkillLevel(player) / 16);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void onPlayerDeath(PlayerDeathEvent event) {
                if (!plugin.perksEnabled) return;

                final Player player = event.getEntity();
                if (player.getHealth() > 0.0f) return;

                // Figure out chance.
                final int chance = getKeepItemPercentage(player);
                if (chance <= 0) return;

                // Clear drops.
                final List<ItemStack> drops = event.getDrops();
                drops.clear();

                final ItemStack[] inventory = player.getInventory().getContents();
                final ItemStack[] armorContent = player.getInventory().getArmorContents();
                for (int j = 0; j < 2; ++j) {
                        ItemStack[] items = null;
                        switch (j) {
                        case 0:
                                items = inventory;
                                break;
                        case 1:
                                items = armorContent;
                                break;
                        default:
                                break;
                        }
                        for (int i = 0; i < items.length; ++i) {
                                ItemStack item = items[i];
                                if (item == null) continue;
                                if (Util.random.nextInt(100) >= chance) {
                                        items[i] = null;
                                        drops.add(item);
                                }
                        }
                }
                inventories.put(player.getName(), inventory);
                armorContents.put(player.getName(), armorContent);
        }

        @EventHandler(priority = EventPriority.LOW)
        public void onPlayerRespawn(PlayerRespawnEvent event) {
                if (!plugin.perksEnabled) return;
                final Player player = event.getPlayer();

                // Fetch and remove stored inventories.
                final ItemStack[] inventory = inventories.remove(player.getName());
                final ItemStack[] armorContent = armorContents.remove(player.getName());

                // Give to player.
                if (inventory != null) player.getInventory().setContents(inventory);
                if (armorContent != null) player.getInventory().setArmorContents(armorContent);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void onEntityCombust(EntityCombustEvent event) {
                if (!(event.getEntity() instanceof Item)) return;
                final Item item = (Item)event.getEntity();
                final Player player = getDropper(item);
                if (player == null || !player.isOnline()) return;

                // Check the actual item.
                final ItemStack stack = item.getItemStack();
                if (spMap.isNull(stack.getType())) return;
                //item.getWorld().strikeLightningEffect(item.getLocation());
                Util.sendMessage(player, "(&cCreeper Overlord&r) &4Thank you for the %s.", Util.niceItemName(stack));
                Util.playParticleEffect(player, item.getLocation().add(0.0, 1.5, 0.0), "magicCrit", stack.getAmount(), 0.4f, 0.1f);
                item.remove(); // Make it disappear.

                // Give SP.
                final int sp = spMap.roll(stack.getType(), stack.getAmount());
                addSkillPoints(player, sp);

                // Give Bonus XP
                if (plugin.perksEnabled) {
                        int xp = xpMap.roll(stack.getType(), stack.getAmount());
                        xp = multiplyXp(player, xp);
                        if (xp > 0) player.giveExp(xp);
                }
        }

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("" + getKeepItemPercentage(player) + "% chance to keep your items upon death");
                result.add("Sacrificing items grants you +" + (getXpMultiplier(player) - 100) + "% xp");
                return result;
        }

        @Override
        public void loadConfiguration() {
                spMap.load(getConfig().getConfigurationSection("sp"));
                xpMap.load(getConfig().getConfigurationSection("xp"));
        }
}
