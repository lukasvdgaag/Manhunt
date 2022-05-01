package me.gaagjescraft.network.team.manhunt.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PAPIHook extends PlaceholderExpansion {

    private Manhunt plugin;

    public PAPIHook(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "manhunt";
    }

    @Override
    public @NotNull String getAuthor() {
        return "GaagjesCraft Network Team (GCNT)";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        return res(player.getUniqueId(), id);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String id) {
        return res(player.getUniqueId(), id);
    }

    private String res(UUID uid, String id) {
        PlayerStat sp = plugin.getPlayerStorage().getUser(uid);
        if (sp == null) sp = plugin.getPlayerStorage().loadUser(uid);

        if (id.equalsIgnoreCase("hunter_wins")) return sp.getHunterWins() + "";
        else if (id.equalsIgnoreCase("hunter_kills")) return sp.getHunterKills() + "";
        else if (id.equalsIgnoreCase("hunter_games_played")) return sp.getHunterGamesPlayed() + "";
        else if (id.equalsIgnoreCase("runner_wins")) return sp.getRunnerWins() + "";
        else if (id.equalsIgnoreCase("runner_kills")) return sp.getRunnerKills() + "";
        else if (id.equalsIgnoreCase("runner_games_played")) return sp.getRunnerGamesPlayed() + "";
        else if (id.equalsIgnoreCase("total_kills")) return (sp.getRunnerKills() + sp.getHunterKills()) + "";
        else if (id.equalsIgnoreCase("total_wins")) return (sp.getHunterWins() + sp.getRunnerWins()) + "";
        else if (id.equalsIgnoreCase("total_games_played"))
            return (sp.getRunnerGamesPlayed() + sp.getHunterGamesPlayed()) + "";

        return null;
    }

}
