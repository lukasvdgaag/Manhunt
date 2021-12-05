package me.gaagjescraft.network.team.manhunt.utils.platform;

import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.compass.CompassTracker;
import me.gaagjescraft.network.team.manhunt.games.compass.OriginalCompassTracker;

public class OriginalPlatformUtils implements PlatformUtils {

    @Override
    public CompassTracker getCompassTracker(GamePlayer gamePlayer) {
        return new OriginalCompassTracker(gamePlayer);
    }
}
