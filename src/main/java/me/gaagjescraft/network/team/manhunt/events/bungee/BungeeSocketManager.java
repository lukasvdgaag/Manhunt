package me.gaagjescraft.network.team.manhunt.events.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeSocketManager {

    private ServerSocket ss = null;
    private Socket s = null;
    private DataInputStream din = null;
    private DataOutputStream dout = null;
    private Thread thread = null;
    private int bukkitTaskId = -1;

    private List<Socket> socketsConnected = new ArrayList<>();

    /*
        This method sends a message to all the connected clients. (Only server side supported).
     */
    public boolean sendMessage(String... msg) {
        // (Manhunt.get().getCfg().isLobbyServer && (ss == null || ss.isClosed())) ||
        /*if ((!Manhunt.get().getCfg().isLobbyServer && dout==null||din==null||(s==null||s.isClosed()||!s.isConnected()))) {
            if (Manhunt.get().getCfg().isLobbyServer) enableServer();
            else connectClientToServer();

            if (dout == null) {
                // if still null after reconnecting...
                Bukkit.getLogger().severe("Failed to send a message to another server because the output stream was invalid (null).");
                return false;
            }
        }*/

        int count = 0;

        if (Manhunt.get().getCfg().isLobbyServer) {
            socketsConnected.removeIf((socket -> socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown()));
            for (Socket socket : socketsConnected) {
                if (!socket.isClosed() && socket.isConnected() && !socket.isOutputShutdown()) {
                    try {
                        DataOutputStream socketDout = new DataOutputStream(socket.getOutputStream());
                        for (String s : msg) {
                            socketDout.writeUTF(s);
                        }
                        socketDout.flush();
                        count++;
                    } catch (Exception ignored) {
                    } // todo maybe make something to remove the closed socket from the arraylist, but for now this is fine.
                }
            }

            if (count == 0 && !socketsConnected.isEmpty()) {
                Bukkit.getLogger().severe("Failed to send a message to another server because the output stream returned an error:");
                return false;
            }

        } else {
            try {
                DataOutputStream socketDout = new DataOutputStream(s.getOutputStream());
                for (String s : msg) {
                    socketDout.writeUTF(s);
                }
                socketDout.flush();
            } catch (Exception e) {
                Bukkit.getLogger().severe("Failed to send a message to another server because the output stream returned an error:");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /*
        This method starts the socket server. (Server)
     */
    public void enableServer() {
        close();
        thread = new Thread(() -> {
            try {
                ss = new ServerSocket(Manhunt.get().getCfg().socketPort);

                while (true) {
                    Bukkit.getLogger().info("Attempting to accept a connection to the socket.");
                    s = ss.accept();
                    s.setKeepAlive(true);
                    Bukkit.getLogger().warning("Manhunt successfully connected to the socket on port 8005 and is now ready to receive and send messages.");

                    socketsConnected.add(s);

                    if (bukkitTaskId != -1) Bukkit.getScheduler().cancelTask(bukkitTaskId);

                    bukkitTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Manhunt.get(), () -> {
                        try {
                            socketsConnected.removeIf((socket -> socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown() || socket.isInputShutdown()));
                            for (Socket s : socketsConnected) {
                                DataInputStream din = new DataInputStream(s.getInputStream());
                                if (din.available() != 0) {
                                    String subChannel = din.readUTF();
                                    String value = "";
                                    if (din.available() > 0) value = din.readUTF();
                                    Bukkit.getLogger().severe("Message from socket: " + subChannel + ", " + value);

                                    switch (subChannel) {
                                        case "createGameResponse":
                                            serverProcessCreateGameResponse(value);
                                            break;
                                        case "deleteGame":
                                            serverProcessDeleteGame(value);
                                            break;
                                        case "gameReady":
                                            serverProcessGameReady(value);
                                            break;
                                        case "gameEnded":
                                            serverProcessGameEnded(value);
                                            break;
                                        case "updateGame":
                                            serverProcessUpdateGame(value);
                                            break;
                                        case "endGame":
                                            serverProcessEndGame(value);
                                            break;
                                    }

                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, 0, 5);
                }


            } catch (Exception e) {
                Manhunt.get().getLogger().severe("Manhunt found an error while doing socket stuff:");
                e.printStackTrace();
                close();
            }
        });
        thread.start();
    }

    /*
        This method is being used to close the socket server and server?
     */

    public void close() {
        try {
            if (thread != null && thread.isAlive()) thread.interrupt();
            if (bukkitTaskId != -1) Bukkit.getScheduler().cancelTask(bukkitTaskId);
            if (din != null) din.close();
            if (dout != null) dout.close();
            if (s != null) s.close();
            if (ss != null) ss.close();
        } catch (Exception ignored) {
        }

        socketsConnected.clear();
    }

    /*
        This method is being used to connect from the client to the socketserver;
     */
    public void connectClientToServer() {
        close();
        thread = new Thread(() -> {
            try {
                s = new Socket(Manhunt.get().getCfg().socketHostname, Manhunt.get().getCfg().socketPort);
                din = new DataInputStream(s.getInputStream());
                dout = new DataOutputStream(s.getOutputStream());
                Bukkit.getLogger().warning("Manhunt successfully connected to the socket and is now ready to receive and send messages.");

                Bukkit.getScheduler().scheduleSyncRepeatingTask(Manhunt.get(), () -> {
                    try {
                        if (s == null || din == null || dout == null || s.isInputShutdown() || s.isOutputShutdown() || s.isClosed() || !s.isConnected()) {
                            close();
                            s = new Socket(Manhunt.get().getCfg().socketHostname, Manhunt.get().getCfg().socketPort);
                            din = new DataInputStream(s.getInputStream());
                            dout = new DataOutputStream(s.getOutputStream());
                            Bukkit.getLogger().warning("Manhunt successfully connected to the socket and is now ready to receive and send messages.");
                        }

                        if (din.available() != 0) {
                            String subChannel = din.readUTF();
                            String value = "";
                            if (din.available() > 0) value = din.readUTF();
                            Bukkit.getLogger().severe("Message from socket: " + subChannel + ", " + value);

                            switch (subChannel) {
                                case "createGame":
                                    clientProcessCreateGame(value);
                                    break;
                                case "endGame":
                                    clientProcessEndGame(value);
                                    break;
                                case "disconnect":
                                    clientProcessDisconnect();
                                    break;
                                case "addSpectator":
                                    clientProcessAddSpectator(value);
                                    break;
                            }
                        }
                    } catch (IOException ignored) {
                        Bukkit.getLogger().severe("Manhunt failed to connect to the socket. Trying again in 1/2 second.");
                    }
                }, 0, 5);
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        });
        thread.start();
    }


    /*
        Utils methods: (Processing input)
     */

    private void clientProcessDisconnect() {
        try {
            if (s != null && !s.isClosed()) s.close();
            if (din != null) din.close();
            if (dout != null) dout.close();
        } catch (Exception ignored) {
        }
    }

    private void serverProcessGameEnded(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game != null) {
            game.delete();
        }
    }

    private void serverProcessUpdateGame(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
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
            game = Game.createGame(allowTwists, gameName, UUID.fromString(hostUuid), maxPlayers);
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

    private void serverProcessEndGame(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
        String gameName = ob.getAsJsonPrimitive("game").getAsString();

        Game game = Game.getGame(gameName);
        if (game == null) return;

        game.setStatus(GameStatus.STOPPING);
    }

    private void clientProcessAddSpectator(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
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

    private void clientProcessEndGame(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
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

    private void clientProcessCreateGame(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
        String host = ob.getAsJsonPrimitive("host").getAsString();
        // request for creating a game to a game server.

        String targetServer = ob.getAsJsonPrimitive("game_server").getAsString();
        if (!Manhunt.get().getCfg().serverName.equals(targetServer)) return;

        boolean allowTwists = ob.getAsJsonPrimitive("allow_twists").getAsBoolean();
        int maxPlayers = ob.getAsJsonPrimitive("max_players").getAsInt();
        String headstart = ob.getAsJsonPrimitive("headstart").getAsString();
        boolean doDaylightCycle = ob.getAsJsonPrimitive("daylight_cycle").getAsBoolean();
        boolean friendlyFire = ob.getAsJsonPrimitive("friendly_fire").getAsBoolean();
        UUID hostUid = UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString());

        if (Game.getGames().isEmpty()) {
            // no running games on this server yet. Ready to create the game.

            Game game = Game.createGame(allowTwists, host, hostUid, maxPlayers);
            if (game == null) {
                sendMessage("createGameResponse", createGameResponse(host, "failed", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
                return;
            }
            sendMessage("createGameResponse", createGameResponse(host, "created", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));

            game.setHeadStart(HeadstartType.valueOf(headstart));
            game.setDoDaylightCycle(doDaylightCycle);
            game.setAllowFriendlyFire(friendlyFire);
            game.create();

        } else {
            // no room for this game on this server, letting the lobby know.
            sendMessage("createGameResponse", createGameResponse(host, "denied", hostUid, allowTwists, maxPlayers, headstart, doDaylightCycle, friendlyFire));
        }
    }

    private void serverProcessCreateGameResponse(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
        if (!ob.getAsJsonPrimitive("response").getAsString().equals("created")) {
            // failed to create the game.
            Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString()));
            if (p == null) return;
            GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
            if (setup == null) return;

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

            Game game = Game.createGame(allowTwists, ob.getAsJsonPrimitive("host").getAsString(), hostUid, maxPlayers);
            if (game != null) {
                GameSetup setup = Manhunt.get().getManhuntGameSetupMenu().gameSetups.get(p);
                if (setup != null) {
                    setup.getBungeeSetup().setServerMatched(true);
                    Bukkit.getScheduler().cancelTask(setup.getBungeeSetup().getRunnableTaskId());
                }
                if (p != null) p.sendTitle("§aServer Found!", "§7Wait here while we prepare the server.", 10, 50, 10);
                if (p != null) Manhunt.get().getManhuntGameSetupMenu().gameSetups.remove(p);
                game.setHeadStart(HeadstartType.valueOf(headstart));
                game.setDoDaylightCycle(doDaylightCycle);
                game.setAllowFriendlyFire(friendlyFire);
                if (p != null) {
                    p.sendMessage(" ");
                    p.sendMessage("§a§lManhunt host server found!");
                    p.sendMessage("§7We matched a server with your Manhunt game.");
                    p.sendMessage("§7You will automatically get moved once it's ready."); // todo send message here with matched server name.
                    p.sendMessage(" ");
                }
            } else {
                p.sendMessage(" ");
                p.sendMessage("§c§lSomething went wrong whilst creating your game.");
                p.sendMessage(" ");
            }
        }
    }

    private void serverProcessDeleteGame(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();
        Game game = Game.getGame(ob.getAsJsonPrimitive("game").getAsString());
        if (game != null) {
            game.delete();
        }
    }

    private void serverProcessGameReady(String json) {
        JsonObject ob = new JsonParser().parse(json).getAsJsonObject();

        Player p = Bukkit.getPlayer(UUID.fromString(ob.getAsJsonPrimitive("host_uuid").getAsString()));
        Game game = Game.getGame(ob.getAsJsonPrimitive("game").getAsString());

        if (p == null) return; // todo something here idk

        if (game == null) {
        } // todo create game if not yet created.
        else {
            String serverName = ob.getAsJsonPrimitive("server_name").getAsString();
            game.setBungeeServer(serverName);
            game.setReady(true);

            p.sendMessage("§aWe finished preparing your Manhunt host server. You will now be warped to that server.");

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            p.sendPluginMessage(Manhunt.get(), "BungeeCord", out.toByteArray());

            Bukkit.getScheduler().scheduleSyncDelayedTask(Manhunt.get(), game::sendGameAnnouncement, 20L);
        }


    }

    private String createGameResponse(String host, String response, UUID hostUUID, boolean allowTwists, int maxPlayers, String headstart, boolean daylight, boolean friendlyfire) {
        return "{'server':'" + Manhunt.get().getCfg().serverName + "', 'host':'" + host + "', 'response':'" + response + "', 'host_uuid':'" + hostUUID.toString() + "', 'allow_twists':" + allowTwists + ", 'max_players':" + maxPlayers + "," +
                " 'headstart':'" + headstart + "', 'daylight_cycle':" + daylight + ", 'friendly_fire':" + friendlyfire + "}";
    }


}