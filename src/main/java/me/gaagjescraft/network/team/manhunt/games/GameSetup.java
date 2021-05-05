package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.entity.Player;

public class GameSetup {

    private int maxPlayers;
    private boolean allowTwists;
    private boolean doDaylightCycle;
    private boolean allowFriendlyFire;
    private Game game;
    private Player host;
    private HeadstartType headstart;
    private GameSetupBungee bungeeSetup;

    public GameSetup(Player host, boolean allowTwists, int maxPlayers, boolean doDaylightCycle, boolean allowFriendlyFire, HeadstartType type) {
        this.host = host;
        this.allowTwists = allowTwists;
        this.maxPlayers = maxPlayers;
        this.doDaylightCycle = doDaylightCycle;
        this.game = null;
        this.allowFriendlyFire = allowFriendlyFire;
        this.headstart = type;
        this.bungeeSetup = new GameSetupBungee(this);
    }

    public GameSetupBungee getBungeeSetup() {
        return bungeeSetup;
    }

    public Game getGame() {
        return game;
    }

    public HeadstartType getHeadstart() {
        return headstart;
    }

    public void setHeadstart(HeadstartType headstart, boolean announce) {
        this.headstart = headstart;
        if (getGame() != null) {
            getGame().setHeadStart(headstart);
            if (announce) {
                getGame().sendMessage(null, Util.c(Manhunt.get().getCfg().headstartChangeMessage.replaceAll("%time%", Manhunt.get().getUtil().secondsToTimeString(headstart.getSeconds(), "string")).replaceAll("%player%", host.getName())));
            }
        }
    }

    public void setGame(Game game) {
        this.game = game;
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
                getGame().sendMessage(null, Util.c(this.allowTwists ? Manhunt.get().getCfg().toggleTwistsEnabledMessage : Manhunt.get().getCfg().toggleTwistsDisabledMessage).replaceAll("%player%", host.getName()));
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
                getGame().sendMessage(null, Util.c(this.doDaylightCycle ? Manhunt.get().getCfg().toggleDaylightEnabledMessage : Manhunt.get().getCfg().toggleDaylightDisabledMessage).replaceAll("%player%", host.getName()));
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
                getGame().sendMessage(null, Util.c(this.allowFriendlyFire ? Manhunt.get().getCfg().toggleFriendlyFireEnabledMessage : Manhunt.get().getCfg().toggleFriendlyFireDisabledMessage).replaceAll("%player%", host.getName()));
            }
        }
    }


}
