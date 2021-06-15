package me.gaagjescraft.network.team.manhunt.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class VaultEcoHook {

    private Economy eco = null;

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }

    public double getBalance(Player p) {
        if (eco == null) return -1;
        return eco.getBalance(p);
    }

    public boolean hasBalance(Player p, double amount) {
        if (eco == null) return false;
        return eco.has(p, amount);
    }

    public void addBalance(Player p, double amount) {
        if (eco == null) return;
        eco.depositPlayer(p, amount);
    }

    public void removeBalance(Player p, double amount) {
        if (eco == null) return;
        eco.withdrawPlayer(p, amount);
    }

}
