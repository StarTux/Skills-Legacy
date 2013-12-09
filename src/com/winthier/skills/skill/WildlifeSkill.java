package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumFractionMap;
import com.winthier.skills.util.Fraction;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
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
        // Breeding
        private final EnumFractionMap<EntityType> breedingSPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        private final EnumFractionMap<EntityType> breedingXPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        // Taming
        private final EnumFractionMap<EntityType> tamingSPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        private final EnumFractionMap<EntityType> tamingXPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        // Shearing
        private final EnumFractionMap<EntityType> shearingSPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        private final EnumFractionMap<EntityType> shearingXPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        // Butchering
        private final EnumFractionMap<EntityType> butcherSPMap = new EnumFractionMap<EntityType>(EntityType.class, 0);
        // Fishing
        Fraction fishingSkillPoints = null;

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
                final EntityType entityType = baby.getType();

                // Give SP.
                final int skillPoints = breedingSPMap.get(entityType);
                addSkillPoints(player, skillPoints);

                // Give bonus XP.
                if (plugin.perksEnabled) {
                        final int xp = multiplyXp(player, breedingXPMap.get(entityType));
                        if (xp > 0) player.giveExp(xp);
                }
        }

        /**
         * Give SP for shearing sheep (and mushroom cows?).
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onPlayerShearEntity(PlayerShearEntityEvent event) {
                final Player player = event.getPlayer();
                final EntityType entityType = event.getEntity().getType();

                // Give SP.
                final int sp = shearingSPMap.get(entityType);
                addSkillPoints(player, sp);

                // Give bonus XP.
                if (plugin.perksEnabled) {
                        final int xp = shearingXPMap.get(entityType);
                        player.giveExp(multiplyXp(player, xp));
                }
        }

        /**
         * Give SP for taming pets.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEntityTame(EntityTameEvent event) {
                if (!(event.getOwner() instanceof Player)) return;
                final Player player = (Player)event.getOwner();

                // Give SP.
                final int skillPoints = tamingSPMap.get(event.getEntityType());
                addSkillPoints(player, skillPoints);

                // Give bonus XP.
                if (plugin.perksEnabled) {
                        final int xp = tamingXPMap.get(event.getEntityType());
                        player.giveExp(multiplyXp(player, xp));
                }
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
                switch (event.getState()) {
                case CAUGHT_FISH:
                        // Give SP.
                        addSkillPoints(player, fishingSkillPoints.get());
                        // Give Bonus XP.
                        if (plugin.perksEnabled) {
                                event.setExpToDrop(multiplyXp(player, event.getExpToDrop()));
                        }
                        break;
                }
        }

        /**
         * Butchering animals is part of this skill.
         */
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onEntityDeath(EntityDeathEvent event) {
                final LivingEntity entity = event.getEntity();
                if (entity.getHealth() > 0.0) return;
                if (entity instanceof Ageable && !((Ageable)entity).isAdult()) return;

                // Figure out damager. Accepted is killing by hand
                // or arrow; not by potion.
                Player player = null;
                {
                        final EntityDamageEvent lastDamage = entity.getLastDamageCause();
                        if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;
                        if (lastDamage.getDamage() <= 0) return;
                        final EntityDamageByEntityEvent lastEntityDamage = (EntityDamageByEntityEvent)lastDamage;
                        final Entity damager = lastEntityDamage.getDamager();
                        if (damager instanceof Player) {
                                player = (Player)damager;
                        } else if (damager instanceof Arrow) {
                                final Arrow arrow = (Arrow)damager;
                                final LivingEntity shooter = arrow.getShooter();
                                if (shooter instanceof Player) player = (Player)shooter;
                        }
                }
                if (player == null) return;

                final int maxHealth = (int)entity.getMaxHealth();
                final int playerDamage = ExploitsPlugin.getPlayerDamage(entity);

                // Give SP.
                int sp = butcherSPMap.get(event.getEntity().getType());
                sp = Util.rollFraction(sp, playerDamage, maxHealth);
                if (sp == 0) return;
                addSkillPoints(player, sp);

                if (plugin.perksEnabled) {
                        // Give bonus XP.
                        final int xp = event.getDroppedExp();
                        event.setDroppedExp(multiplyXp(player, xp));

                        // Drop the head.
                        if (Util.random.nextInt(1000) < getSkullDropPermil(player)) {
                                ItemStack skull = Util.getMobHead(entity);
                                if (skull != null) {
                                        event.getDrops().add(skull);
                                }
                        }
                }
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        public void onPlayerEggThrow(PlayerEggThrowEvent event) {
                if (!event.isHatching()) return;
                if (!(event.getEgg().getShooter() instanceof Player)) return;
                final Player player = (Player)event.getEgg().getShooter();

                // Give SP.
                final int skillPoints = breedingSPMap.get(event.getHatchingType());
                addSkillPoints(player, skillPoints);

                if (plugin.perksEnabled) {
                        // Give bonux XP.
                        final int xp = breedingXPMap.get(event.getHatchingType());
                        if (xp > 0) player.giveExp(multiplyXp(player, xp));

                        // Set bonus hatches.
                        final int hatches = event.getNumHatches();
                        event.setNumHatches((byte)(hatches + getBonusHatches(player)));
                }
        }

        public int getBonusHatches(Player player) {
                return getSkillLevel(player) / 75;
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(3);

                // Skull Drop
                final int skullPermil = getSkullDropPermil(player);
                if (skullPermil > 0) {
                        result.add("Butchered animals drop their head " + Util.printPermilAsPercent(skullPermil) + "% of the time.");
                }

                // Taming XP bonus
                result.add("Taming, fishing, breeding and shearing animals drops +" + (getXpMultiplier(player) - 100) + "% XP");

                // Bonus hatches
                final int bonusHatches = getBonusHatches(player);
                if (bonusHatches > 1) {
                        result.add("Eggs hatch " + getBonusHatches(player) + " bonus chickens");
                } else if (bonusHatches == 1) {
                        result.add("Eggs hatch 1 bonus chicken");
                }
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                // Load breeding SP and XP.
                breedingSPMap.load(getConfig().getConfigurationSection("sp.breeding"));
                breedingXPMap.load(getConfig().getConfigurationSection("xp.breeding"));

                // Load taming SP and XP.
                tamingSPMap.load(getConfig().getConfigurationSection("sp.taming"));
                tamingXPMap.load(getConfig().getConfigurationSection("xp.taming"));

                // Load shearing SP and XP.
                shearingSPMap.load(getConfig().getConfigurationSection("sp.shearing"));
                shearingXPMap.load(getConfig().getConfigurationSection("xp.shearing"));

                // Load butchering SP.
                butcherSPMap.load(getConfig().getConfigurationSection("sp.butcher"));

                // Load fishing SP.
                fishingSkillPoints = Fraction.parseFraction(getConfig().getString("sp.Fishing"));
        }
}
