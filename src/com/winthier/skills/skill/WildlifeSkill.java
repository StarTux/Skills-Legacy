package com.winthier.skills.skill;

import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 * The wildlife skill combines several animal related activities:
 * - Breeding
 * - Taming
 * - Taming
 * - Fishing
 */
public class WildlifeSkill extends AbstractSkill {
        public final static String BREEDER_KEY = "breeder";
        public final EnumIntMap<EntityType> breedingSpMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        public final EnumIntMap<EntityType> breedingXpMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        public final EnumIntMap<EntityType> tamingSpMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        private int shearingSkillPoints, fishingSkillPoints;

        public WildlifeSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        /**
         * This handler makes an attempt to record who fed an
         * animal by guessing if it was able to breed when someone
         * right clicked it with something that could be accepted
         * as food by said animal.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                final Player player = event.getPlayer();
                final ItemStack item = player.getItemInHand();
                if (item == null || item.getType() == Material.AIR) return;
                final Material mat = item.getType();
                final Entity entity = event.getRightClicked();
                if (!(entity instanceof Ageable)) return;
                final Ageable ageable = (Ageable)entity;
                if (!ageable.canBreed()) return;
                switch(entity.getType()) {
                case CHICKEN:
                        if (mat != Material.SEEDS &&
                            mat != Material.MELON_SEEDS &&
                            mat != Material.PUMPKIN_SEEDS) return;
                        break;
                case COW:
                case MUSHROOM_COW:
                case SHEEP:
                        if (mat != Material.WHEAT) return;
                        break;
                case OCELOT:
                        if (mat != Material.RAW_FISH) return;
                        break;
                case PIG:
                        if (mat != Material.CARROT_ITEM) return;
                        break;
                case WOLF:
                        if (mat != Material.ROTTEN_FLESH &&
                            mat != Material.COOKED_BEEF &&
                            mat != Material.GRILLED_PORK &&
                            mat != Material.COOKED_CHICKEN &&
                            mat != Material.RAW_BEEF && 
                            mat != Material.PORK &&
                            mat != Material.RAW_CHICKEN) return;
                        break;
                default: return;
                }
                MetadataValue breederValue = new FixedMetadataValue(plugin, player.getName());
                entity.setMetadata(BREEDER_KEY, breederValue);
        }

        /**
         * When a creature spawns due to breeding, there is no
         * convenient way to figure out its parent, which is why
         * this listener has to search the surroundings of the
         * baby for two potential parent that may or may not have
         * been fed by the same player, according to what was
         * recorded in the above handler.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onCreatureSpawn(CreatureSpawnEvent event) {
                if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.BREEDING) return;
                final LivingEntity baby = event.getEntity();
                Entity mother = null, father = null;
                String motherBreeder = null, fatherBreeder = null;
        entityLoop:
                for (Entity entity : baby.getNearbyEntities(2.0, 2.0, 2.0)) {
                        if (entity == baby) continue;
                        if (entity.getType() != baby.getType()) continue;
                        if (entity.hasMetadata(BREEDER_KEY)) {
                                for (MetadataValue value : entity.getMetadata(BREEDER_KEY)) {
                                        if (value.getOwningPlugin() == plugin) {
                                                if (mother == null) {
                                                        mother = entity;
                                                        motherBreeder = value.asString();
                                                } else {
                                                        father = entity;
                                                        fatherBreeder = value.asString();
                                                        break entityLoop;
                                                }
                                        }
                                }
                        }
                }
                if (mother != null && father != null) {
                        mother.removeMetadata(BREEDER_KEY, plugin);
                        father.removeMetadata(BREEDER_KEY, plugin);
                        if (motherBreeder.equals(fatherBreeder)) {
                                final Player player = plugin.getServer().getPlayerExact(motherBreeder);
                                if (player != null) onPlayerBreed(player, baby);
                        }
                }
        }

        /**
         * If a breeder is found who is online, they will receive
         * the deserved SP and increased XP as a perk.
         */
        public void onPlayerBreed(Player player, LivingEntity baby) {
                if (!canCollectSkillPoints(player)) return;
                final EntityType entityType = baby.getType();
                // give sp
                final int skillPoints = breedingSpMap.get(entityType);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
                // give xp
                final int xp = multiplyXp(player, breedingXpMap.get(entityType));
                if (xp > 0) player.giveExp(xp);
                player.sendMessage("Gratz! SP=" + skillPoints + ", XP=" + xp);
        }

        /**
         * Give SP for shearing sheep (and mushroom cows?).
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerShearEntity(PlayerShearEntityEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                if (shearingSkillPoints > 0) addSkillPoints(player, shearingSkillPoints);
        }

        /**
         * Give SP for taming pets.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEntityTame(EntityTameEvent event) {
                if (!(event.getOwner() instanceof Player)) return;
                final Player player = (Player)event.getOwner();
                if (!canCollectSkillPoints(player)) return;
                // give sp
                final int skillPoints = tamingSpMap.get(event.getEntityType());
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
        }

        /**
         * This handler changes the activity of fishing in several
         * ways:
         * - Increase bite chance according to skill level
         * - Give SP and bonus XP for catching fish.
         * - Deal damage to rods even for failed attempts, to nerf
         *   afk fishing.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerFish(PlayerFishEvent event) {
                final Player player = event.getPlayer();
                if (!canCollectSkillPoints(player)) return;
                switch (event.getState()) {
                case FISHING:
                        final Fish fish = event.getHook();
                        double biteChance = fish.getBiteChance();
                        biteChance = biteChance * (100 + getSkillLevel(player)) / 100.0;
                        fish.setBiteChance(biteChance);
                        break;
                case CAUGHT_FISH:
                        // give sp
                        if (fishingSkillPoints > 0) addSkillPoints(player, fishingSkillPoints);
                        // give xp
                        event.setExpToDrop(multiplyXp(player, event.getExpToDrop()));
                        break;
                case FAILED_ATTEMPT:
                        // punish failed attempts to avoid afk fishing
                        ItemStack item = player.getItemInHand();
                        if (item != null && item.getType() == Material.FISHING_ROD) {
                                final int durability = item.getDurability();
                                if (durability > 1) item.setDurability((short)(durability - 1));
                        }
                }
        }

        @Override
        public void loadConfiguration() {
                breedingSpMap.load(EntityType.class, getConfig().getConfigurationSection("sp.breeding"));
                breedingXpMap.load(EntityType.class, getConfig().getConfigurationSection("xp.breeding"));
                shearingSkillPoints = getConfig().getInt("sp.Shearing");
                fishingSkillPoints = getConfig().getInt("sp.Fishing");
        }
}
