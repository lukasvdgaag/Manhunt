package me.gaagjescraft.network.team.manhunt.utils.platform;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import me.gaagjescraft.network.team.manhunt.games.compass.CompassTracker;
import me.gaagjescraft.network.team.manhunt.games.compass.OriginalCompassTracker;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OriginalPlatformUtils implements PlatformUtils {

    @Override
    public CompassTracker getCompassTracker(GamePlayer gamePlayer) {
        return new OriginalCompassTracker(gamePlayer);
    }

    @Override
    public GameSetup initGameSetup(Player host, boolean allowTwists, int maxPlayers, boolean doDaylightCycle, boolean allowFriendlyFire, HeadstartType type) {
        return new GameSetup(host, allowTwists, maxPlayers, doDaylightCycle, allowFriendlyFire, type);
    }

    @Override
    public Game initGame(GameSetup setup, String host, UUID hostUUID) {
        if (Game.getGame(hostUUID) != null || Game.getGame(host) != null) return null;
        Game game = new Game(host, setup.isAllowTwists(), hostUUID, setup.getMaxPlayers());
        game.setAllowFriendlyFire(setup.isAllowFriendlyFire());
        game.setDoDaylightCycle(setup.isDoDaylightCycle());
        game.setHeadStart(setup.getHeadstart());
        return game;
    }

    @Override
    public Game initGame(GameSetup setup, Player host) {
        return initGame(setup, host.getName(), host.getUniqueId());
    }

    @Override
    public Game initGame(boolean twistsAllowed, String host, UUID hostUUID, int maxPlayers) {
        if (Game.getGame(host) != null) return null;
        return new Game(host, twistsAllowed, hostUUID, maxPlayers);
    }

    @Override
    public Game initGame(boolean twistsAllowed, Player host, int maxPlayers) {
        if (Game.getGame(host.getName()) != null || Game.getGame(host) != null) return null;
        return new Game(host.getName(), twistsAllowed, host.getUniqueId(), maxPlayers);
    }
}
