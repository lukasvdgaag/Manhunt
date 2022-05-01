package me.gaagjescraft.network.team.manhunt.managers.party;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AlessioDPPartiesManager implements PartyManager {

    //Support for Parties by AlessioDP Support by JT122406
    private final PartiesAPI api;

    public AlessioDPPartiesManager() {
        this.api = Parties.getApi();
    }

    @Override
    public boolean hasParty(Player p) {
        PartyPlayer pp = api.getPartyPlayer(p.getUniqueId());
        return pp != null && pp.isInParty();
    }

    @Override
    public int partySize(Player p) {
        Party party = api.getPartyOfPlayer(p.getUniqueId());
        if (party == null) return 0;

        return party.getOnlineMembers().size();
    }

    @Override
    public boolean isOwner(Player p) {
        Party pp = api.getPartyOfPlayer(p.getUniqueId());
        return pp != null && pp.getLeader() != null && pp.getLeader().equals(p.getUniqueId());
    }

    @Override
    public Player getOwner(Player p) {
        final Party party = api.getPartyOfPlayer(p.getUniqueId());
        if (party == null || party.getLeader() == null) return null;

        return Bukkit.getPlayer(party.getLeader());
    }

    @Override
    public List<Player> getMembers(Player p) {
        ArrayList<Player> players = new ArrayList<>();

        final PartyPlayer partyPlayer = api.getPartyPlayer(p.getUniqueId());
        if (partyPlayer == null || partyPlayer.getPartyId() == null) return players;

        com.alessiodp.parties.api.interfaces.Party party = api.getParty(partyPlayer.getPartyId());
        if (party == null) return players; // very unlikely

        for (PartyPlayer member : party.getOnlineMembers()) {
            players.add(Bukkit.getPlayer(member.getPlayerUUID()));
        }
        return players;
    }
}
