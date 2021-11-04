package me.gaagjescraft.network.team.manhunt.commands;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to perform this command.");
            return true;
        }

        Player p = (Player) sender;

        PlayerStat sp = Manhunt.get().getPlayerStorage().getUser(p.getUniqueId());

        int wins = (sp.getHunterWins() + sp.getRunnerWins());
        int games = (sp.getHunterGamesPlayed() + sp.getRunnerGamesPlayed());
        int kills = (sp.getHunterKills() + sp.getRunnerKills());
        int winRate = (games == 0) ? 0 : (wins / games) * 100;

        for (String a : Manhunt.get().getCfg().statsMessage) {
            p.sendMessage(Util.c(a)
                    .replaceAll("%hunter_kills%", sp.getHunterKills() + "")
                    .replaceAll("%hunter_wins%", sp.getHunterWins() + "")
                    .replaceAll("%hunter_games_played%", sp.getHunterGamesPlayed() + "")
                    .replaceAll("%runner_kills%", sp.getRunnerKills() + "")
                    .replaceAll("%runner_wins%", sp.getRunnerWins() + "")
                    .replaceAll("%runner_games_played%", sp.getRunnerGamesPlayed() + "")
                    .replaceAll("%total_games_played%", games + "")
                    .replaceAll("%total_wins%", wins + "")
                    .replaceAll("%total_kills%", kills + "")
                    .replaceAll("%win_rate%", winRate + "")
                    .replaceAll("%runner_beat_time%", sp.getBestSpeedRunTime() == 0 ? "N/A" : Manhunt.get().getUtil().secondsToTimeString((int) sp.getBestSpeedRunTime(), "string")));
        }

        return true;
    }
}
