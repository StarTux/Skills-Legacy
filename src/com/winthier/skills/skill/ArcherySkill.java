package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class ArcherySkill extends AbstractSkill {
        private final EnumIntMap<EntityType> spMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        private int minKillDistance, normDistance;

        public ArcherySkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEntityDeath(EntityDeathEvent event) {
                LivingEntity entity = event.getEntity();
                final EntityDamageEvent lastDamage = entity.getLastDamageCause();
                if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;
                if (lastDamage.getDamage() <= 0) return;
                final EntityDamageByEntityEvent lastEntityDamage = (EntityDamageByEntityEvent)lastDamage;
                final Entity damager = lastEntityDamage.getDamager();
                if (!(damager instanceof Projectile)) return;
                final Projectile projectile = (Projectile)damager;
                final LivingEntity shooter = projectile.getShooter();
                if (!(shooter instanceof Player)) return;
                Player player = (Player)shooter;
                if (!canCollectSkillPoints(player)) return;
                // give sp
                int skillPoints = spMap.get(entity.getType());
                if (skillPoints == 0) return;
                if (ExploitsPlugin.getKillDistance(player) < minKillDistance) return;
                final int maxHealth = entity.getMaxHealth();
                final int playerDamage = Math.min(maxHealth, ExploitsPlugin.getPlayerDamage(entity));
                skillPoints = Util.rollFraction(skillPoints, playerDamage, maxHealth);
                int distance = Util.horizontalDistance(player.getLocation(), entity.getLocation());
                player.sendMessage("Distance = " + distance);
                skillPoints = Util.rollFraction(skillPoints, distance, normDistance);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
                // give bonus xp
                final int xp = event.getDroppedExp();
                event.setDroppedExp(multiplyXp(player, xp));
        }        

        @Override
        public void loadConfiguration() {
                minKillDistance = getConfig().getInt("MinKillDistance");
                normDistance = getConfig().getInt("NormDistance");
                spMap.load(EntityType.class, getConfig().getConfigurationSection("sp"));
        }
}
