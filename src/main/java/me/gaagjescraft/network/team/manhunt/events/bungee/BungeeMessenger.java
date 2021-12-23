package me.gaagjescraft.network.team.manhunt.events.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.*;
import me.gaagjescraft.network.team.manhunt.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * BungeeCord Socket Messenger class.
 * Send and process messages sent over the connected sockets.
 */
public class BungeeMessenger {

    public void serverProcessGameEnded(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game != null) {
            game.delete();
        }
    }

    public void serverProcessUpdateGame(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("id").getAsString();
        String serverName = ob.getAsJsonPrimitive("server").getAsString();
        String hostUuid = ob.getAsJsonPrimitive("host_uuid").getAsString();
        boolean allowTwists = ob.getAsJsonPrimitive("allow_twists").getAsBoolean();
        int maxPlayers = ob.getAsJsonPrimitive("max_players").getAsInt();
        String headstart = ob.getAsJsonPrimitive("headstart").getAsString();
        boolean doDaylightCycle = ob.getAsJsonPrimitive("daylight_cycle").getAsBoolean();
        boolean friendlyFire = ob.getAsJsonPrimitive("friendly_fire").getAsBoolean();
        String status = ob.getAsJsonPrimitive("status").getAsString();
        int runnerCount = ob.getAsJsonPrimitive("runner_count").getAsInt();
        int hunterCount = ob.getAsJsonPrimitive("hunter_count").getAsInt();

        Game game = Game.getGame(gameName);
        if (game == null) {
            game = Manhunt.get().getPlatformUtils().initGame(allowTwists, gameName, UUID.fromString(hostUuid), maxPlayers);
            if (game == null) return;
        }

        game.setBungeeServer(serverName);
        game.setHostUUID(UUID.fromString(hostUuid));
        game.setTwistsAllowed(allowTwists);
        game.setMaxPlayers(maxPlayers);
        game.setHeadStart(HeadstartType.valueOf(headstart));
        game.setDoDaylightCycle(doDaylightCycle);
        game.setAllowFriendlyFire(friendlyFire);
        game.setStatus(GameStatus.valueOf(status));
        game.setBungeeRunnerCount(runnerCount);
        game.setBungeeHunterCount(hunterCount);
    }

    public void serverProcessEndGame(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game == null) return;

        game.setStatus(GameStatus.STOPPING);
    }

    public void clientProcessAddSpectator(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game == null) return;

        UUID player = UUID.fromString(ob.getAsJsonPrimitive("player").getAsString());
        if (!game.getSpectators().contains(player)) {
            game.addSpectator(player);
        }
        GamePlayer gp = game.getPlayer(player);
        if (gp.isOnline() && !gp.isSpectating()) {
            Player p = Bukkit.getPlayer(player);
            if (p == null) return;
            gp.setSpectating(true);
            gp.prepareForSpectate();
        }
    }

    public void clientProcessEndGame(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game == null) return;

        boolean forceStop = ob.getAsJsonPrimitive("force_stop").getAsBoolean();
        if (forceStop) {
            game.delete();
        } else {
            game.stopGame(true);
        }
    }

    public Game clientProcessCreateGame(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        String host = ob.getAsJsonPrimitive("host").getAsString();
        // request for creating a game to a game server.

        String targetServer = ob.getAsJsonPrimitive("game_server").getAsString();
        if (!Manhunt.get().getCfg().serverName.equals(targetServer)) return null;

        boolean allowTwists = ob.getAsJsonPrimitive("allow_twists").getAsBoolean();
        int maxPlayers = ob.getAsJsonPrimitive("max_players").getAsInt();
        String headstart = ob.getAsJsonPrimitive("headstart").getAsString();
        boolean doDaylightCycle = ob.getAsJsonPrimitive("daylight_cycle").getAsBoolean();
        boolean friendlyFire = ob.getAsJsonPrimitive("friendly_fire").getAsBoolean();
        UUID hostUid = UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString());

        if (Game.getGames().isEmpty()) {
            // no running games on this server yet. Ready to create the game.
            if (Manhunt.get().getCfg().lobby == null) {
                Bukkit.getLogger().severe("Manhunt denied a game creation from the lobby because the lobby spawnpoint has not been set yet.");
                Bukkit.getLogger().severe("Please set it with '/manhunt setspawn'.");
                Manhunt.get().getBungeeSocketManager().sendMessage("createGameResponse", createGameResponse(host, "denied", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                return null;
            }

            Game game = Manhunt.get().getPlatformUtils().initGame(allowTwists, host, hostUid, maxPlayers);
            if (game == null) {
                Manhunt.get().getBungeeSocketManager().sendMessage("createGameResponse", createGameResponse(host, "failed", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                return null;
            }
            Manhunt.get().getBungeeSocketManager().sendMessage("createGameResponse", createGameResponse(host, "created", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));

            game.setHeadStart(HeadstartType.valueOf(headstart));
            game.setDoDaylightCycle(doDaylightCycle);
            game.setAllowFriendlyFire(friendlyFire);
            game.create();
            return game;
        } else {
            // no room for this game on this server, letting the lobby know.
            Manhunt.get().getBungeeSocketManager().sendMessage("createGameResponse", createGameResponse(host, "denied", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
        }
        return null;
    }

    public Game serverProcessCreateGameResponse(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        if (!ob.getAsJsonPrimitive("response").getAsString().equals("created")) {
            // failed to create the game.
            Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString()));
            if (p == null) return null;
            GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
            if (setup == null) return null;

            if (setup.getBungeeSetup().isLastServer()) {
                setup.getBungeeSetup().setNoGameAvailable(true);
                Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(p);
            } else {
                setup.getBungeeSetup().requestNextGameCreation();
            }
        } else {
            // game is successfully created.
            boolean allowTwists = ob.getAsJsonPrimitive("allow_twists").getAsBoolean();
            int maxPlayers = ob.getAsJsonPrimitive("max_players").getAsInt();
            String headstart = ob.getAsJsonPrimitive("headstart").getAsString();
            boolean doDaylightCycle = ob.getAsJsonPrimitive("daylight_cycle").getAsBoolean();
            boolean friendlyFire = ob.getAsJsonPrimitive("friendly_fire").getAsBoolean();
            UUID hostUid = UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString());

            Player p = Bukkit.getPlayer(hostUid);

            Game game = Manhunt.get().getPlatformUtils().initGame(allowTwists, ob.getAsJsonPrimitive("host").getAsString(), hostUid, maxPlayers);
            if (game != null) {
                GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
                if (setup != null) {
                    setup.getBungeeSetup().setServerMatched(true);
                    Bukkit.getScheduler().cancelTask(setup.getBungeeSetup().getRunnableTaskId());
                }
                if (p != null) Util.sendTitle(p, Util.c(Manhunt.get().getCfg().serverFoundTitle), 10, 50, 10);
                if (p != null) Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(p);
                game.setHeadStart(HeadstartType.valueOf(headstart));
                game.setDoDaylightCycle(doDaylightCycle);
                game.setAllowFriendlyFire(friendlyFire);
                if (p != null) {
                    for (String s : Manhunt.get().getCfg().serverFoundMessage) {
                        p.sendMessage(Util.c(s));
                    }
                }
                return game;
            } else {
                if (p == null) return null;
                for (String s : Manhunt.get().getCfg().creatingGameErrorMessage) {
                    p.sendMessage(Util.c(s));
                }
            }
        }
        return null;
    }

    public void serverProcessDeleteGame(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();
        Game game = Game.getGame(ob.getAsJsonPrimitive("game").getAsString());
        if (game != null) {
            game.delete();
        }
    }

    public void serverProcessGameReady(String json) {
        JsonObject ob = JsonParser.parseString(json).getAsJsonObject();

        Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString()));
        Game game = Game.getGame(ob.getAsJsonPrimitive("game").getAsString());

        if (p == null) return; // todo something here idk

        // todo create game if not yet created. (game == null)
        if (game != null) {
            String serverName = ob.getAsJsonPrimitive("server_name").getAsString();
            game.setBungeeServer(serverName);
            game.setReady(true);

            p.sendMessage(Util.c(Manhunt.get().getCfg().finishedPreparingServerMessage));

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            p.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());

            Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), game::sendGameAnnouncement, 20L);
        }
    }

    public String createGameResponse(String host, String response, UUID hostUUID, boolean allowTwists, int maxPlayers, String headstart, boolean daylight, boolean friendlyfire) {
        return String.format("{'server': '%s', 'host': '%s', 'response': '%s', 'host_uuid': '%s', 'allow_twists': %b, 'max_players': %d, 'headstart': %s, 'daylight_cycle': %b, 'friendly_fire': %b}",
                Manhunt.get().getCfg().serverName, host, response, hostUUID.toString(), allowTwists, maxPlayers, headstart, daylight, friendlyfire);
    }

    public void createGameServer(GameSetup setup, String targetGameServer) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        Player host = setup.getHost();
        String json = "{'server_name': '" + Manhunt.get().getCfg().serverName + "', 'game_server':'" + targetGameServer + "', 'host':'" + host.getName() + "', 'host_uuid':'" + setup.getHost().getUniqueId() + "', " +
                "'max_players':" + setup.getMaxPlayers() + ", 'headstart':'" + setup.getHeadstart().name() + "', " +
                "'allow_twists':" + setup.isAllowTwists() + ", 'daylight_cycle':" + setup.isDoDaylightCycle() + ", 'friendly_fire':" + setup.isAllowFriendlyFire() + "}";

        if (!Manhunt.get().getBungeeSocketManager().sendMessage("createGame", json)) {
            setup.getHost().sendMessage(ChatColor.RED + "We had some trouble connecting to the other servers. Please inform a staff member.");
        }
    }

    public void createAddSpectatorMessage(Game game, UUID player) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        Manhunt.get().getBungeeSocketManager().sendMessage("addSpectator", "{'game':'" + game.getIdentifier() + "', 'player':'" + player.toString() + "'}");
    }

    public void createDisconnectClientMessage() {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        Manhunt.get().getBungeeSocketManager().sendMessage("disconnect", "");
    }

    public void createGameReadyMessage(Game game) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        String json = "{'server_name': '" + Manhunt.get().getCfg().serverName + "', 'game':'" + game.getIdentifier() + "', 'host_uuid':'" + game.getHostUUID().toString() + "'}";
        Manhunt.get().getBungeeSocketManager().sendMessage("gameReady", json);
    }

    public void createGameEndedMessage(Game game) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        String json = "{'game':'" + game.getIdentifier() + "'}";
        Manhunt.get().getBungeeSocketManager().sendMessage("gameEnded", json);
    }

    public void createEndGameMessage(Game game, boolean forceStop) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        String json = "{'game':'" + game.getIdentifier() + "', 'force_stop':" + forceStop + "}";
        Manhunt.get().getBungeeSocketManager().sendMessage("endGame", json);
    }

    public void createUpdateGameMessage(Game game) {
        if (Manhunt.get().getBungeeSocketManager() == null) return;
        JsonObject object = new JsonObject();
        object.addProperty("server", Manhunt.get().getCfg().serverName);
        object.addProperty("id", game.getIdentifier());
        object.addProperty("host_uuid", game.getHostUUID().toString());
        object.addProperty("allow_twists", game.isTwistsAllowed());
        object.addProperty("max_players", game.getMaxPlayers());
        object.addProperty("headstart", game.getHeadStart().name());
        object.addProperty("daylight_cycle", game.isDoDaylightCycle());
        object.addProperty("friendly_fire", game.isAllowFriendlyFire());
        object.addProperty("status", game.getStatus().name());
        object.addProperty("runner_count", game.getOnlinePlayers(PlayerType.RUNNER).size());
        object.addProperty("hunter_count", game.getOnlinePlayers(PlayerType.HUNTER).size());

        Manhunt.get().getBungeeSocketManager().sendMessage("updateGame", object.toString());
    }

}
