package me.gaagjescraft.network.team.manhunt.events.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import me.gaagjescraft.network.team.manhunt.games.HeadstartType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessageHandler implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (channel.equals("exodus:manhunt")) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subChannel = in.readUTF();

            if (subChannel.equals("createGame") && !Manhunt.get().getConfig().getBoolean("lobby-server", false)) {
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                String host = ob.getAsJsonObject("host").getAsString();
                // request for creating a game to a game server.
                if (Game.getGames().size() == 0) {
                    // no running games on this server yet. Ready to create the game.
                    boolean allowTwists = ob.getAsJsonObject("allow_twists").getAsBoolean();
                    int maxPlayers = ob.getAsJsonObject("max_players").getAsInt();
                    String headstart = ob.getAsJsonObject("headstart").getAsString();
                    boolean doDaylightCycle = ob.getAsJsonObject("daylight_cycle").getAsBoolean();
                    boolean friendlyFire = ob.getAsJsonObject("friendly_fire").getAsBoolean();
                    UUID hostUid = UUID.fromString(ob.getAsJsonObject("host_uuid").getAsString());

                    Game game = Game.createGame(allowTwists, host, hostUid, maxPlayers);
                    if (game == null) {
                        player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "failed"));
                        return;
                    }
                    game.setHeadStart(HeadstartType.valueOf(headstart));
                    game.setDoDaylightCycle(doDaylightCycle);
                    game.setAllowFriendlyFire(friendlyFire);
                    game.create();
                    player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "created"));
                } else {
                    // no room for this game on this server, letting the lobby know.
                    player.sendPluginMessage(Manhunt.get(), "exodus:manhunt", createGameResponse(host, "denied"));
                }
            } else if (subChannel.equals("createGameResponse") && Manhunt.get().getConfig().getBoolean("lobby-server", false)) {
                // response to 'createGame', targeted to the lobby.
                String json = in.readUTF();
                JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
                if (!ob.getAsJsonObject("response").getAsString().equals("created")) {
                    // failed to create the game.
                    Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonObject("host").getAsString()));
                    if (p == null) return;
                    GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
                    if (setup == null) return;

                    setup.getBungeeSetup().requestNextGameCreation();
                } else {
                    // game is successfully created.
                    // todo update stuff or smt
                }
            }
        }
    }

    private byte[] createGameResponse(String host, String response) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createGameResponse");
        out.writeUTF("{'server':'" + Manhunt.get().getConfig().getString("server_name") + "', 'host':'" + host + "', 'response':'" + response + "'}");
        return out.toByteArray();
    }
}
