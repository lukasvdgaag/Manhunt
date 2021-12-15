package me.gaagjescraft.network.team.manhunt.utils.platform;

import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import me.gaagjescraft.network.team.manhunt.games.compass.CompassTracker;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlatformUtils {

    CompassTracker getCompassTracker(GamePlayer gamePlayer);

    GameSetup initGameSetup(Player host, boolean allowTwists, int maxPlayers, boolean doDaylightCycle, boolean allowFriendlyFire, HeadstartType type);

    Game initGame(GameSetup setup, String host, UUID hostUUID);

    Game initGame(GameSetup setup, Player host);

    Game initGame(boolean twistsAllowed, String host, UUID hostUUID, int maxPlayers);

    Game initGame(boolean twistsAllowed, Player host, int maxPlayers);

}
