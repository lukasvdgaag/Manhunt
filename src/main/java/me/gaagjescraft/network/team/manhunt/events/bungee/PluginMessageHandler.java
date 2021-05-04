package me.gaagjescraft.network.team.manhunt.events.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessageHandler implements PluginMessageListener {

    /**
     * This is not done and will most likely not be handled through the BungeeMessagingChannels because it requires players to send messages.
     * Please ignore this for the time being.
     *
     * @param channel
     * @param player
     * @param bytes
     */

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!Manhunt.get().getCfg().bungeeMode) return;
        if (channel.equals("exodus:manhunt")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();

            if (subChannel.equals("createGame") && !Manhunt.get().getCfg().isLobbyServer) {
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                String host = ob.getAsJsonObject("host").getAsString();
                // request for creating a game to a game server.

                boolean allowTwists = ob.getAsJsonObject("allow_twists").getAsBoolean();
                int maxPlayers = ob.getAsJsonObject("max_players").getAsInt();
                String headstart = ob.getAsJsonObject("headstart").getAsString();
                boolean doDaylightCycle = ob.getAsJsonObject("daylight_cycle").getAsBoolean();
                boolean friendlyFire = ob.getAsJsonObject("friendly_fire").getAsBoolean();
                UUID hostUid = UUID.fromString(ob.getAsJsonObject("host_uuid").getAsString());

                if (Game.getGames().size() == 0) {
                    // no running games on this server yet. Ready to create the game.

                    Game game = Game.createGame(allowTwists, host, hostUid, maxPlayers);
                    if (game == null) {
                        player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "failed", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                        return;
                    }
                    game.setHeadStart(HeadstartType.valueOf(headstart));
                    game.setDoDaylightCycle(doDaylightCycle);
                    game.setAllowFriendlyFire(friendlyFire);
                    game.create();
                    player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "created", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                } else {
                    // no room for this game on this server, letting the lobby know.
                    player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "denied", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                }
            } else if (subChannel.equals("createGameResponse") && Manhunt.get().getCfg().isLobbyServer) {
                // response to 'createGame', targeted to the lobby.
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                if (!ob.getAsJsonObject("response").getAsString().equals("created")) {
                    // failed to create the game.
                    Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonObject("host").getAsString()));
                    if (p == null) return;
                    GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
                    if (setup == null) return;

                    if (setup.getBungeeSetup().isNoGameAvailable()) {
                        p.sendMessage(" ");
                        p.sendMessage("§c§lThere weren't any free servers available to host your game.");
                        p.sendMessage(" ");
                    } else {
                        setup.getBungeeSetup().requestNextGameCreation();
                    }
                } else {
                    // game is successfully created.
                    boolean allowTwists = ob.getAsJsonObject("allow_twists").getAsBoolean();
                    int maxPlayers = ob.getAsJsonObject("max_players").getAsInt();
                    String headstart = ob.getAsJsonObject("headstart").getAsString();
                    boolean doDaylightCycle = ob.getAsJsonObject("daylight_cycle").getAsBoolean();
                    boolean friendlyFire = ob.getAsJsonObject("friendly_fire").getAsBoolean();
                    UUID hostUid = UUID.fromString(ob.getAsJsonObject("host_uuid").getAsString());

                    Player p = Bukkit.getPlayer(hostUid);

                    Game game = Game.createGame(allowTwists, ob.getAsJsonObject("host").getAsString(), hostUid, maxPlayers);
                    if (p != null) Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(p);
                    if (game != null) {
                        game.setHeadStart(HeadstartType.valueOf(headstart));
                        game.setDoDaylightCycle(doDaylightCycle);
                        game.setAllowFriendlyFire(friendlyFire);
                        if (p != null) {
                            p.sendMessage(" ");
                            p.sendMessage("§a§lSuccessfully found a server to host your manhunt event on.");
                            p.sendMessage("§7Please wait in this server until the server is ready.");
                            p.sendMessage(" ");
                        }
                    } else {
                        p.sendMessage(" ");
                        p.sendMessage("§c§lSomething went wrong whilst creating your game.");
                        p.sendMessage(" ");
                    }
                }
            } else if (subChannel.equals("updateGame") && Manhunt.get().getCfg().isLobbyServer) {
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                boolean allowTwists = ob.getAsJsonObject("allow_twists").getAsBoolean();
                int maxPlayers = ob.getAsJsonObject("max_players").getAsInt();
                String headstart = ob.getAsJsonObject("headstart").getAsString();
                String status = ob.getAsJsonObject("status").getAsString();

                int ingameRunners = ob.getAsJsonObject("runner_count").getAsInt();
                int ingameHunters = ob.getAsJsonObject("hunter_count").getAsInt();

                boolean doDaylightCycle = ob.getAsJsonObject("daylight_cycle").getAsBoolean();
                boolean friendlyFire = ob.getAsJsonObject("friendly_fire").getAsBoolean();
                UUID hostUid = UUID.fromString(ob.getAsJsonObject("host_uuid").getAsString());
                String host = ob.getAsJsonObject("host").getAsString();

                Game game = Game.getGame(host);
                if (game == null) {
                    game = Game.createGame(allowTwists, host, hostUid, maxPlayers);
                    if (game == null) return;
                }
                game.setBungeeServer(ob.getAsJsonObject("server").getAsString());
                game.setHeadStart(HeadstartType.valueOf(headstart));
                game.setBungeeHunterCount(ingameHunters);
                game.setBungeeRunnerCount(ingameRunners);
                game.setDoDaylightCycle(doDaylightCycle);
                game.setAllowFriendlyFire(friendlyFire);
                game.setStatus(GameStatus.valueOf(status));
            } else if (subChannel.equals("deleteGame") && Manhunt.get().getCfg().isLobbyServer) {
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                Game game = Game.getGame(ob.getAsJsonObject("game").getAsString());
                if (game != null) {
                    game.delete();
                }
            }
        }
    }

    public byte[] createDeleteGameMessage(String gameId) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("deleteGame");
        out.writeUTF("{'game':'" + gameId + "'}");
        return out.toByteArray();
    }

    // todo create updateGame method
    public byte[] createUpdateGameMessage(String host, UUID hostUUID, boolean allowTwists, int maxPlayers, String headstart, boolean daylight, boolean friendlyfire, String status, int ingameRunners, int ingameHunters) {
        JsonObject object = new JsonObject();
        object.addProperty("server", Manhunt.get().getCfg().serverName);
        object.addProperty("host", host);
        object.addProperty("host_uuid", hostUUID.toString());
        object.addProperty("allow_twists", allowTwists);
        object.addProperty("max_players", maxPlayers);
        object.addProperty("headstart", headstart);
        object.addProperty("daylight_cycle", daylight);
        object.addProperty("friendly_fire", friendlyfire);
        object.addProperty("status", status);
        object.addProperty("runner_count", ingameRunners);
        object.addProperty("hunter_count", ingameHunters);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("updateGame");
        out.writeUTF(object.toString());
        return out.toByteArray();
    }

    private byte[] createGameResponse(String host, String response, UUID hostUUID, boolean allowTwists, int maxPlayers, String headstart, boolean daylight, boolean friendlyfire) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createGameResponse");
        out.writeUTF("{'server':'" + Manhunt.get().getCfg().serverName + "', 'host':'" + host + "', 'response':'" + response + "', 'host_uuid':'" + hostUUID.toString() + "', 'allow_twists':" + allowTwists + ", 'max_players':" + maxPlayers + "," +
                " 'headstart':'" + headstart + "', 'daylight_cycle':" + daylight + ", 'friendly_fire':" + friendlyfire + "}");
        return out.toByteArray();
    }
}
