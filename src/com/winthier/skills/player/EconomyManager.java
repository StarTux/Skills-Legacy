package com.winthier.skills.player;

import com.winthier.skills.SkillsPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
        private final SkillsPlugin plugin;
        private Economy economy = null;

        public EconomyManager(SkillsPlugin plugin) {
                this.plugin = plugin;
        }

        public void onEnable() {
                getEconomy();
        }

        private Economy getEconomy() {
                if (economy == null) {
                        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
                        if (economyProvider != null) {
                                economy = economyProvider.getProvider();
                        }
                }
                return economy;
        }

        public String format(double amount) {
                return getEconomy().format(amount);
        }

        public boolean has(Player player, double amount) {
                return getEconomy().has(player.getName(), amount);
        }

        public boolean take(Player player, double amount) {
                return getEconomy().withdrawPlayer(player.getName(), amount).transactionSuccess();
        }

        public boolean give(Player player, double amount) {
                return getEconomy().depositPlayer(player.getName(), amount).transactionSuccess();
        }
}
