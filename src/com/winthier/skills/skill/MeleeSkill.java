package com.winthier.skills.skill;

import com.winthier.exploits.ExploitsPlugin;
import com.winthier.skills.SkillsPlugin;
import com.winthier.skills.util.EnumIntMap;
import com.winthier.skills.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MeleeSkill extends AbstractSkill {
        private final EnumIntMap<EntityType> spMap = new EnumIntMap<EntityType>(EntityType.class, 0);
        private int minKillDistance;

        public MeleeSkill(SkillsPlugin plugin, SkillType skillType) {
                super(plugin, skillType);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEntityDeath(EntityDeathEvent event) {
                LivingEntity entity = event.getEntity();
                final EntityDamageEvent lastDamage = entity.getLastDamageCause();
                if (lastDamage == null || !(lastDamage instanceof EntityDamageByEntityEvent)) return;
                final EntityDamageByEntityEvent lastEntityDamage = (EntityDamageByEntityEvent)lastDamage;
                final Entity damager = lastEntityDamage.getDamager();
                if (!(damager instanceof Player)) return;
                Player player = (Player)damager;
                // give sp
                int skillPoints = spMap.get(entity.getType());
                if (skillPoints == 0) return;
                if (ExploitsPlugin.getKillDistance(player) < minKillDistance) return;
                final int maxHealth = (int)entity.getMaxHealth();
                final int playerDamage = Math.min(maxHealth, ExploitsPlugin.getPlayerDamage(entity));
                skillPoints = Util.rollFraction(skillPoints, playerDamage, maxHealth);
                if (skillPoints > 0) addSkillPoints(player, skillPoints);
                // give bonus xp
                final int xp = event.getDroppedExp();
                event.setDroppedExp(multiplyXp(player, xp));
        }

        // User output

        public List<String> getPerkDescription(Player player) {
                List<String> result = new ArrayList<String>(1);
                result.add("Killed mobs drop +" + (getXpMultiplier(player) - 100) + "% xp");
                return result;
        }

        // Configuration routines

        @Override
        public void loadConfiguration() {
                minKillDistance = getConfig().getInt("MinKillDistance");
                spMap.load(EntityType.class, getConfig().getConfigurationSection("sp"));
        }
}
