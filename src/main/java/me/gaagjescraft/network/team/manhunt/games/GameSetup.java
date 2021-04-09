package me.gaagjescraft.network.team.manhunt.games;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import org.bukkit.entity.Player;

public class GameSetup {

    private int maxPlayers;
    private boolean allowTwists;
    private boolean doDaylightCycle;
    private boolean allowFriendlyFire;
    private Game game;
    private Player host;
    private HeadstartType headstart;

    public GameSetup(Player host, boolean allowTwists, int maxPlayers, boolean doDaylightCycle, boolean allowFriendlyFire, HeadstartType type) {
        this.host = host;
        this.allowTwists = allowTwists;
        this.maxPlayers = maxPlayers;
        this.doDaylightCycle = doDaylightCycle;
        this.game = null;
        this.allowFriendlyFire = allowFriendlyFire;
        this.headstart = type;
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
            if (announce) getGame().sendMessage(null, "§b" + host.getName() + "§e has changed the runners headstart to §a§l"+ Manhunt.get().getUtil().secondsToTimeString(headstart.getSeconds(), "string") + "§e.");
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
            if (announce) getGame().sendMessage(null, "§b" + host.getName() + "§e has " + (this.allowTwists ? "enabled" : "disabled") + " §a§lTwists§e.");
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
            if (announce) getGame().sendMessage(null, "§b" + host.getName() + "§e has " + (this.doDaylightCycle ? "enabled" : "disabled") + " §a§lDaylight Cycle§e.");
        }
    }

    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    public void setAllowFriendlyFire(boolean allowFriendlyFire, boolean announce) {
        this.allowFriendlyFire = allowFriendlyFire;
        if (getGame() != null) {
            getGame().setAllowFriendlyFire(this.allowFriendlyFire);
            if (announce) getGame().sendMessage(null, "§b" + host.getName() + "§e has " + (this.allowFriendlyFire ? "enabled" : "disabled") + " §a§lFriendly Fire§e.");
        }
    }


}
