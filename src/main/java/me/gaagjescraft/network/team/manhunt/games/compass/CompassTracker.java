package me.gaagjescraft.network.team.manhunt.games.compass;

import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import org.bukkit.Location;

public interface CompassTracker {

    void updateCompass();

    Location getLocation();

    GamePlayer getTrackingPlayer();

    Location getTrackingLocation();

    boolean isTracking();

    void setTracking(GamePlayer tracking);

    void setTracking(Location location);

}
