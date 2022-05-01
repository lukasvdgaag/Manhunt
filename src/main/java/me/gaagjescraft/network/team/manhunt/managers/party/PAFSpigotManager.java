package me.gaagjescraft.network.team.manhunt.managers.party;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.api.party.PartyManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PAFSpigotManager implements me.gaagjescraft.network.team.manhunt.managers.party.PartyManager {

    //Party and Friends for Spigot Support by JT122406
    private PlayerParty getPAFParty(Player p) {
        OnlinePAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(p);
        return PartyManager.getInstance().getParty(pafPlayer);
    }

    @Override
    public boolean hasParty(Player p) {
        return getPAFParty(p) != null;
    }

    @Override
    public int partySize(Player p) {
        PlayerParty party = getPAFParty(p);
        if (party == null) return 0;

        return party.getAllPlayers().size();
    }

    @Override
    public Player getOwner(Player p) {
        PlayerParty party = getPAFParty(p);
        if (party == null) return null;

        return Bukkit.getPlayer(party.getLeader().getUniqueId());
    }

    @Override
    public boolean isOwner(Player p) {
        OnlinePAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(p);
        PlayerParty party = PartyManager.getInstance().getParty(pafPlayer);
        if (party == null)
            return false;
        return party.isLeader(pafPlayer);
    }

    @Override
    public List<Player> getMembers(Player owner) {
        ArrayList<Player> playerList = new ArrayList<>();

        PlayerParty party = getPAFParty(owner);
        if (party == null) return playerList;

        for (OnlinePAFPlayer players : party.getAllPlayers()) {
            playerList.add(players.getPlayer());
        }
        return playerList;
    }
}
