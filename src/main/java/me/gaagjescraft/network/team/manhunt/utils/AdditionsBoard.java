package me.gaagjescraft.network.team.manhunt.utils;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;

public class AdditionsBoard {

    public Scoreboard board;
    private final Player player;
    private final Objective objective;
    private final int linecount;
    private final Manhunt plugin;

    private final HashMap<Integer, String> cache = new HashMap<>();

    public AdditionsBoard(Manhunt plugin, Player player, int linecount) {
        this.plugin = plugin;
        this.player = player;
        this.linecount = linecount;
        this.board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        this.objective = this.board.registerNewObjective("test", "dummy", "§6§lMANHUNT");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = linecount;
        for (int i = 0; i < linecount; i++) { // looping through the lines
            Team t = this.board.registerNewTeam("plus-" + i); // creating the team
            t.addEntry(ChatColor.values()[i] + ""); // assigning a color to the team
            t.setPrefix("§r");
            this.objective.getScore(ChatColor.values()[i] + "").setScore(score); // sets the score number
            score--;
        }
        this.player.setScoreboard(this.board); // sets the player scoreboard
    }

    public Scoreboard getScoreboard() {
        return board;
    }

    public void setTitle(String arg0) {
        if (arg0 == null) arg0 = ""; // title null, making it empty

        if (cache.containsKey(-1) && cache.get(-1).equals(arg0)) return; // if title is in cache, return
        cache.remove(-1); // removing the title from the cache
        cache.put(-1, arg0); // changing the title in the cache
        objective.setDisplayName(arg0); // sets the title of the scoreboard
    }

    public void setLine(int arg0, String arg1) {
        if (!player.getScoreboard().equals(this.board)) player.setScoreboard(this.board);

        Team arg2 = board.getTeam("plus-" + arg0); // Get the team we need
        if (arg1 == null) arg1 = ""; // Line null, making it empty
        arg1 = ChatColor.translateAlternateColorCodes('&', arg1);

        if (cache.containsKey(arg0) && cache.get(arg0).equals(arg1)) return; // Line hasn't changed
        cache.remove(arg0); // remove the old line
        cache.put(arg0, arg1); // add the new line

        ArrayList<String> arg3 = convertIntoPieces(arg1, 64);

        // Sanity check
        if (arg2 == null) return;

        arg2.setPrefix(arg3.get(0));

        // If line is less than 64, there won't be a suffix
        if (arg3.size() > 1) {
            arg2.setSuffix(arg3.get(1));
        }
    }

    private ArrayList<String> convertIntoPieces(String arg0, int arg1) {
        if (plugin.getUtil().getVersion() >= 16) {
            return Lists.newArrayList(arg0);
        }

        ArrayList<String> arg2 = new ArrayList<>();

        if (arg0.length() <= arg1) {
            arg2.add(arg0);
            arg2.add("");
        } else {
            if (!ChatColor.getLastColors(arg0.substring(arg1 - 2, arg1)).equals("")) {
                arg2.add(arg0.substring(0, arg1 - 2));
                arg2.add(arg0.substring(arg1 - 2));
            } else if (!ChatColor.getLastColors(arg0.substring(arg1 - 1, arg1 + 1)).equals("")) {
                arg2.add(arg0.substring(0, arg1 - 1));
                arg2.add(arg0.substring(arg1 - 1));
            } else {
                arg2.add(arg0.substring(0, arg1));
                String arg3 = ChatColor.getLastColors(arg2.get(0));
                arg2.add(arg3 + arg0.substring(arg1));
            }

            if (arg2.get(1).length() > arg1) {
                arg2.set(1, arg2.get(1).substring(0, arg1));
            }
        }

        return arg2;
    }

    public int getLinecount() {
        return linecount;
    }

    public String getLine(int number) {
        return cache.getOrDefault(number, "");
    }

    public Player getPlayer() {
        return player;
    }
}