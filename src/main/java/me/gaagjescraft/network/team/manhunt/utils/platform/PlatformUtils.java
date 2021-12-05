package me.gaagjescraft.network.team.manhunt.utils.platform;

import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.compass.CompassTracker;

public interface PlatformUtils {

    CompassTracker getCompassTracker(GamePlayer gamePlayer);

}
