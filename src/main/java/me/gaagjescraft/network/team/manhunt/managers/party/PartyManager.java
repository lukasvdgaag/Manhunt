package me.gaagjescraft.network.team.manhunt.managers.party;

import org.bukkit.entity.Player;

import java.util.List;

public interface PartyManager {

    /**
     * Check if a player is in a party.
     *
     * @param p Player to check.
     * @return true when in a party, false otherwise.
     */
    boolean hasParty(Player p);

    /**
     * Get the size of a party.
     *
     * @param p Player to get the party size of.
     * @return size of the party.
     */
    int partySize(Player p);

    /**
     * Check if the player is a party owner.
     *
     * @param p Player to check
     * @return true when player is party owner, false otherwise.
     */
    boolean isOwner(Player p);

    /**
     * Get the owner of the party that a player is in.
     *
     * @param p Player to get the owner of the party of.
     * @return Player that owns the party that the player is in.
     */
    Player getOwner(Player p);

    /**
     * Get a list of players that are in the same party as the owner of the given party.
     *
     * @param owner Player to get the party members of.
     * @return List of party members.
     */
    List<Player> getMembers(Player owner);
}
