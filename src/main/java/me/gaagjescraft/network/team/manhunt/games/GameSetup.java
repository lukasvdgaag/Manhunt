package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.entity.Player;

public class GameSetup {

    private final GameSetupBungee bungeeSetup;
    private final Manhunt plugin;
    private final Player host;

    private Game game;
    private int maxPlayers;
    private boolean allowTwists;
    private boolean doDaylightCycle;
    private boolean allowFriendlyFire;
    private HeadstartType headstart;

    public GameSetup(Manhunt plugin, Player host, boolean allowTwists, int maxPlayers, boolean doDaylightCycle, boolean allowFriendlyFire, HeadstartType type) {
        this.plugin = plugin;
        this.host = host;
        this.allowTwists = allowTwists;
        this.maxPlayers = maxPlayers;
        this.doDaylightCycle = doDaylightCycle;
        this.game = null;
        this.allowFriendlyFire = allowFriendlyFire;
        this.headstart = type;
        this.bungeeSetup = new GameSetupBungee(plugin, this);
    }

    public GameSetupBungee getBungeeSetup() {
        return bungeeSetup;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public HeadstartType getHeadStart() {
        return headstart;
    }

    public void setHeadstart(HeadstartType headstart, boolean announce) {
        this.headstart = headstart;
        if (getGame() != null) {
            getGame().setHeadStart(headstart);
            if (announce) {
                getGame().sendMessage(null, Util.c(plugin.getCfg().headstartChangeMessage.replaceAll("%time%", plugin.getUtil().secondsToTimeString(headstart.getSeconds(), "string")).replaceAll("%player%", host.getName())));
            }
        }
    }

    public Player getHost() {
        return host;
    }

    public boolean isAllowTwists() {
        return allowTwists;
    }

    public void setAllowTwists(boolean allowTwists, boolean announce) {
        this.allowTwists = allowTwists;
        if (getGame() != null) {
            getGame().setTwistsAllowed(this.allowTwists);
            for (GamePlayer gp : this.game.getPlayers()) {
                gp.prepareForGame(GameStatus.WAITING);
            }
            if (announce) {
                getGame().sendMessage(null, Util.c(this.allowTwists ? plugin.getCfg().toggleTwistsEnabledMessage : plugin.getCfg().toggleTwistsDisabledMessage).replaceAll("%player%", host.getName()));
            }
        }
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isDoDaylightCycle() {
        return doDaylightCycle;
    }

    public void setDoDaylightCycle(boolean doDaylightCycle, boolean announce) {
        this.doDaylightCycle = doDaylightCycle;
        if (getGame() != null) {
            getGame().setDoDaylightCycle(this.doDaylightCycle);
            if (announce) {
                getGame().sendMessage(null, Util.c(this.doDaylightCycle ? plugin.getCfg().toggleDaylightEnabledMessage : plugin.getCfg().toggleDaylightDisabledMessage).replaceAll("%player%", host.getName()));
            }
        }
    }

    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean allowFriendlyFire, boolean announce) {
        this.allowFriendlyFire = allowFriendlyFire;
        if (getGame() != null) {
            getGame().setAllowFriendlyFire(this.allowFriendlyFire);
            if (announce) {
                getGame().sendMessage(null, Util.c(this.allowFriendlyFire ? plugin.getCfg().toggleFriendlyFireEnabledMessage : plugin.getCfg().toggleFriendlyFireDisabledMessage).replaceAll("%player%", host.getName()));
            }
        }
    }

    @Override
    public String toString() {
        return "'max_players': " + maxPlayers +
                ", 'allow_twists': " + allowTwists +
                ", 'daylight_cycle': " + doDaylightCycle +
                ", 'friendly_fire': " + allowFriendlyFire +
                ", 'headstart': '" + headstart.name() + "'";
    }
}
