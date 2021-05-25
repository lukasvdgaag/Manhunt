package me.gaagjescraft.network.team.manhunt.inst.storage;

import com.google.common.collect.Lists;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;

import java.util.List;
import java.util.UUID;

public interface PlayerStorage {

    String type = "";
    List<PlayerStat> stats = Lists.newArrayList();

    void setup();

    void connect();

    PlayerStat loadUser(UUID uuid);

    default void unloadUser(UUID uuid) {
        stats.removeIf((e) -> e.getUuid().equals(uuid));
    }

    default PlayerStat getUser(UUID uuid) {
        for (PlayerStat stat : stats) {
            if (stat.getUuid().equals(uuid)) return stat;
        }
        return null;
    }

    void saveUser(UUID uuid);

}
