package com.winthier.skills;

import com.winthier.skills.command.ConfigCommand;
import com.winthier.skills.command.ElementCommand;
import com.winthier.skills.command.HighscoreCommand;
import com.winthier.skills.command.SkillCommand;
import com.winthier.skills.listener.NoCheatPlusListener;
import com.winthier.skills.menu.MenuManager;
import com.winthier.skills.player.EconomyManager;
import com.winthier.skills.player.PlayerInfo;
import com.winthier.skills.player.PlayerManager;
import com.winthier.skills.scoreboard.ScoreboardManager;
import com.winthier.skills.skill.AbstractSkill;
import com.winthier.skills.skill.SkillType;
import com.winthier.skills.spell.SpellManager;
import com.winthier.skills.sql.SQLManager;
import com.winthier.skills.util.Fraction;
import com.winthier.skills.util.MagicWatchdog;
import com.winthier.skills.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillsPlugin extends JavaPlugin {
        public final SQLManager sqlManager = new SQLManager(this);
        public final EconomyManager economyManager = new EconomyManager(this);
        public final PlayerManager playerManager = new PlayerManager(this);
        public final SpellManager spellManager = new SpellManager(this);
        public final ScoreboardManager scoreboardManager = new ScoreboardManager(this);
        public final MenuManager menuManager = new MenuManager(this);
        public final MagicWatchdog magicWatchdog = new MagicWatchdog(this);
        // Configuration
        public boolean perksEnabled = true;
        public int switchElementCooldown = 60; // In minutes.
        public double rewardFactor = 0.1;
        public Fraction proficiencySpellCostFactor = null;
        public Fraction proficiencySkillPointFactor = null;
        // Config cache
        private ConfigurationSection skillsConfig = null;
        private ConfigurationSection challengeConfig = null;

        // NoCheatPlus
        public final NoCheatPlusListener ncpListener = new NoCheatPlusListener(this);

        @Override
        public void onEnable() {
                // Setup config.
                reloadConfig();

                // Other managers rely on SQL, so enable this first.
                sqlManager.onEnable();
                // Economy doesn't need enabling, but it can't hurt.
                economyManager.onEnable();
                // EntityListener
                ncpListener.onEnable();
                // MagicWatchdog
                magicWatchdog.onEnable();

                // Trigger skill decay.
                if (getConfig().getBoolean("SkillDecay")) {
                        sqlManager.checkSkillDecay();
                }

                // Initialize all skills and load their configurations.
                perksEnabled = getConfig().getBoolean("PerksEnabled");
                switchElementCooldown = getConfig().getInt("SwitchElementCooldown");
                rewardFactor = getConfig().getDouble("RewardFactor");
                proficiencySpellCostFactor = Fraction.parseFraction(getConfig().getString("ProficiencySpellCostFactor"));
                proficiencySkillPointFactor = Fraction.parseFraction(getConfig().getString("ProficiencySkillPointFactor"));
                SkillType.enableAll(this);
                SkillType.loadAll();

                // Enable all managers.
                playerManager.onEnable();
                spellManager.onEnable();
                scoreboardManager.onEnable();
                menuManager.onEnable();

                // Set command executors.
                new SkillCommand(this).onEnable();
                new ConfigCommand(this).onEnable();
                new HighscoreCommand(this).onEnable();
                new ElementCommand(this).onEnable();

                // Write config. Make sure that defauls are written to disk.
                getConfig().options().copyDefaults(true);
                saveConfig();
        }

        @Override
        public void onDisable() {
                // Disable everything that was enabled and needs
                // to be disabled, in reverse order.
                scoreboardManager.onDisable();
                spellManager.onDisable();
                playerManager.onDisable();
                SkillType.disableAll();
                sqlManager.onDisable();
                ncpListener.onDisable();
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
                getLogger().warning(sender.getName() + " used unmapped command '" + label + "'.");
                return false;
        }

        public ConfigurationSection getSkillsConfig() {
                if (skillsConfig == null) {
                        skillsConfig = getConfigFile("skills.yml");
                }
                return skillsConfig;
        }

        public ConfigurationSection getChallengeConfig() {
                if (challengeConfig == null) {
                        challengeConfig = getConfigFile("challenge.yml");
                }
                return challengeConfig;
        }

        public ConfigurationSection getConfigFile(String path) {
                return YamlConfiguration.loadConfiguration(getResource(path));
        }

        /**
         * Send skill information about player to sender.
         */
        public void sendSkillInfo(CommandSender sender, Player player, SkillType skillType) {
                AbstractSkill skill = skillType.getSkill();
                PlayerInfo info = playerManager.getPlayerInfo(player);
                Util.sendMessage(sender, "%s%s &b%s &bSkill Info %s%s", skillType.getColor(), Util.ICON, skillType.getDisplayName(), skillType.getColor(), Util.ICON);
                ElementType elems[] = skillType.getElements();
                if (elems.length > 0) {
                        Util.sendMessage(sender, "&3 &m   &b Elements &3&m   ");
                        for (ElementType elem : elems) {
                                Util.sendMessage(sender, " %s%s &b%s", elem.getColor(), Util.ICON, elem.getDisplayName());
                        }
                }
                Util.sendMessage(sender, "&3 &m   &b Description &3&m   ");
                for (String line : skill.getDescription().split("\n")) {
                        Util.sendMessage(sender, " &7%s", line);
                }
                if (perksEnabled) {
                        Util.sendMessage(sender, "&3 &m   &b Perks &3&m   ");
                        for (String line : skill.getPerkDescription(player)) {
                                Util.sendMessage(sender, " &7%s", line);
                        }
                }
                Util.sendMessage(sender, "&3 &m   &b Statistics &3&m   ");
                Util.sendMessage(sender, "&3 Level: &b%d&3 SP: &b%d&3 For next level: &b%s", info.getSkillLevel(skillType), info.getSkillPoints(skillType), info.getRequiredSkillPoints(skillType));
                {
                        StringBuilder sb = new StringBuilder();
                        sb.append(ChatColor.DARK_AQUA).append(" Progress [").append(ChatColor.AQUA);
                        int needs = info.getRequiredSkillPoints(skillType);
                        int total = info.getSkillLevel(skillType) + 1;
                        int pct = needs * 40 / total;
                        int i = 0;
                        for (i = 0; i < 40 - pct; ++i) sb.append("|");
                        sb.append(ChatColor.DARK_GRAY);
                        for (; i < 40; ++i) sb.append("|");
                        sb.append(ChatColor.DARK_AQUA).append("]");
                        sender.sendMessage(sb.toString());
                }
        }

        public void sendHighscore(CommandSender sender, String name, ChatColor color, int count, String[] names, int[] levels, int page) {
                Util.sendMessage(sender, "%s%s &b%s Highscore (page %d) %s%s", color, Util.ICON, name, page + 1, color, Util.ICON);
                Util.sendMessage(sender, "&7&oFor skill highscores, type &3/hi &b[&oskill&b] [&opage&b]");
                Util.sendMessage(sender, " &3Rank &fLevel &bName");
                for (int i = 0; i < count; ++i) {
                        final int rank = page * names.length + i + 1;
                        Util.sendMessage(sender, " %s %s &b%s",
                                         Util.shadowZeros(rank, 2, ChatColor.BLACK, ChatColor.DARK_AQUA),
                                         Util.shadowZeros(levels[i], 3, ChatColor.DARK_GRAY, ChatColor.WHITE),
                                         names[i]);
                }
        }
}
