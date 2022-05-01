package me.gaagjescraft.network.team.manhunt.managers.party;

import org.bukkit.entity.Player;

import java.util.List;

public class NoPartyManager implements PartyManager {

    @Override
    public boolean hasParty(Player p) {
        return false;
    }

    @Override
    public int partySize(Player p) {
        return 0;
    }

    @Override
    public boolean isOwner(Player p) {
        return false;
    }

    @Override
    public Player getOwner(Player p) {
        return null;
    }

    @Override
    public List<Player> getMembers(Player owner) {
        return null;
    }
}
