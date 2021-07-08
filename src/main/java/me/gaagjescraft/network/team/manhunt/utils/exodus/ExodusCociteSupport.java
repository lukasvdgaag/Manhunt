package me.gaagjescraft.network.team.manhunt.utils.exodus;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import net.exodus.cocitesupport.CociteSupport;
import net.exodus.cocitesupport.cocite.CociteApi;
import org.bukkit.Bukkit;

import java.util.UUID;

public class ExodusCociteSupport {

    private CociteApi cociteApi;

    public ExodusCociteSupport() {
        if (Bukkit.getPluginManager().isPluginEnabled("CociteSupport"))
            this.cociteApi = CociteSupport.get().getCociteApi();
    }

    public void addExp(UUID uid, int amount) {
        Bukkit.getScheduler().runTask(Manhunt.get(), () -> {
            if (cociteApi != null) cociteApi.addExpToPlayer(uid, amount);
        });
    }

    public void addToken(UUID uid, int amount) {
        Bukkit.getScheduler().runTask(Manhunt.get(), () -> {
            if (cociteApi != null) cociteApi.addTokensToPlayer(uid, amount);
        });
    }

    public void addCredits(UUID uid, int amount) {
        Bukkit.getScheduler().runTask(Manhunt.get(), () -> {
            if (cociteApi != null) cociteApi.addCreditsToPlayer(uid, amount);
        });

    }

}
