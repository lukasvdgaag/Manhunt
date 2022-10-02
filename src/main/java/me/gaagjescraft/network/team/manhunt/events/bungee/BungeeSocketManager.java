package me.gaagjescraft.network.team.manhunt.events.bungee;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.events.custom.ManhuntBungeeMessageReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BungeeSocketManager {

    public final ConcurrentLinkedQueue<Socket> socketsConnected = new ConcurrentLinkedQueue<>();

    private final Object socketLock = new Object();
    private ServerSocket ss = null;
    public Socket s = null;
    private DataInputStream din = null;
    private DataOutputStream dout = null;
    private Thread incomingConnectionThread = null;
    private BukkitTask bukkitTask = null;

    private final Manhunt plugin;

    public BungeeSocketManager(Manhunt plugin) {
        this.plugin = plugin;
    }

    /**
     * This method sends a message to all the connected clients. (Only server side supported).
     */
    public boolean sendMessage(String... msg) {
        int count = 0;

        if (plugin.getCfg().debug) {
            Bukkit.getLogger().info("Sending a message: ");
            for (String s : msg) {
                Bukkit.getLogger().info("value = " + s);
            }
        }

        if (plugin.getCfg().isLobbyServer) {
            socketsConnected.removeIf((socket -> socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown()));
            for (Socket socket : socketsConnected) {
                if (!socket.isClosed() && socket.isConnected() && !socket.isOutputShutdown()) {
                    try {
                        DataOutputStream socketDout = new DataOutputStream(socket.getOutputStream());
                        for (String s : msg) {
                            socketDout.writeUTF(s);
                        }
                        socketDout.flush();
                        if (plugin.getCfg().debug) Bukkit.getLogger().info("message sent.");
                        count++;
                    } catch (Exception ignored) {
                    }
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
                if (plugin.getCfg().debug) Bukkit.getLogger().info("message sent.");
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
        // Make sure that the plugin behaves fine after a reload
        cleanup();

        // Setup handler for open connections
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        bukkitTask = scheduler.runTaskTimerAsynchronously(plugin, () -> {
            try {
                socketsConnected.removeIf((socket -> socket.isClosed() || !socket.isConnected() || socket.isOutputShutdown() || socket.isInputShutdown()));
                for (Socket s : socketsConnected) {
                    DataInputStream din = new DataInputStream(s.getInputStream());
                    if (din.available() >= 2) {
                        String subChannel = din.readUTF();
                        String value = din.readUTF();
                        if (plugin.getCfg().debug)
                            Bukkit.getLogger().severe("Message from socket: " + subChannel + ", " + value);

                        // Perform event call and trigger message handlers on the tick
                        scheduler.runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(new ManhuntBungeeMessageReceiveEvent(subChannel, value));

                            switch (subChannel) {
                                case "createGameResponse" -> plugin.getBungeeMessenger().serverProcessCreateGameResponse(value);
                                case "deleteGame" -> plugin.getBungeeMessenger().serverProcessDeleteGame(value);
                                case "gameReady" -> plugin.getBungeeMessenger().serverProcessGameReady(value);
                                case "gameEnded" -> plugin.getBungeeMessenger().serverProcessGameEnded(value);
                                case "updateGame" -> plugin.getBungeeMessenger().serverProcessUpdateGame(value);
                                case "endGame" -> plugin.getBungeeMessenger().serverProcessEndGame(value);
                            }
                        });

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 5);

        // Setup listener for new connections
        incomingConnectionThread = new Thread(() -> {
            try {
                ss = new ServerSocket(plugin.getCfg().socketPort);

                while (true) {
                    Bukkit.getLogger().info("Attempting to accept a connection to the socket.");
                    Socket incomingSocketConnection = ss.accept(); // Will hang here until a new connection comes in
                    incomingSocketConnection.setKeepAlive(true);
                    Bukkit.getLogger().warning("Manhunt successfully connected to the socket on port " + plugin.getCfg().socketPort + " and is now ready to receive and send messages.");

                    socketsConnected.add(incomingSocketConnection);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Manhunt found an error while doing socket stuff:");
                e.printStackTrace();
                cleanup();
            }
        });
        incomingConnectionThread.start();
    }

    /*
        This method is being used to close the socket server and server?
     */

    public void cleanup() {
        try {
            if (incomingConnectionThread != null && incomingConnectionThread.isAlive()) incomingConnectionThread.interrupt();
            if (bukkitTask != null && !bukkitTask.isCancelled()) bukkitTask.cancel();
            synchronized (socketLock) {
                if (din != null) din.close();
                if (dout != null) dout.close();
                if (s != null) s.close();
                if (ss != null) ss.close();
            }
        } catch (Exception ex) {
            if (plugin.getCfg().debug) ex.printStackTrace();
        }

        socketsConnected.clear();
    }

    /*
        This method is being used to connect from the client to the socketserver;
     */
    public void connectClientToServer() {
        // Make sure nothing is still connected
        cleanup();

        // Setup data listener (from: lobby server, to: this client server)
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                synchronized (socketLock) {
                    if (s == null || din == null || dout == null || s.isInputShutdown() || s.isOutputShutdown() || s.isClosed() || !s.isConnected()) {
                        cleanup();
                        s = new Socket(plugin.getCfg().socketHostname, plugin.getCfg().socketPort);
                        din = new DataInputStream(s.getInputStream());
                        dout = new DataOutputStream(s.getOutputStream());
                        Bukkit.getLogger().warning("Manhunt successfully connected to the socket and is now ready to receive and send messages.");
                    }
                }

                // Get if we are ready to read data
                int availableData;
                synchronized (socketLock) {
                    availableData = din.available();
                }

                if (availableData >= 2) {
                    // Read data
                    String subChannel;
                    String value;
                    synchronized (socketLock) {
                        subChannel = din.readUTF();
                        value = din.readUTF();
                    }

                    if (plugin.getCfg().debug) Bukkit.getLogger().severe("Message from socket: " + subChannel + ", " + value);

                    // Perform event call and trigger message handlers on the tick
                    scheduler.runTask(plugin, () -> {
                        Bukkit.getPluginManager().callEvent(new ManhuntBungeeMessageReceiveEvent(subChannel, value));

                        switch (subChannel) {
                            case "createGame" -> plugin.getBungeeMessenger().clientProcessCreateGame(value);
                            case "endGame" -> plugin.getBungeeMessenger().clientProcessEndGame(value);
                            case "disconnect" -> clientProcessDisconnect();
                            case "addSpectator" -> plugin.getBungeeMessenger().clientProcessAddSpectator(value);
                        }
                    });
                }
            } catch (IOException ignored) {
                if (plugin.getCfg().debug)
                    Bukkit.getLogger().severe("Manhunt failed to connect to the socket. Trying again in 1/2 second.");
            }
        }, 0, 5);
    }

    private void clientProcessDisconnect() {
        try {
            synchronized (socketLock) {
                if (s != null && !s.isClosed()) s.close();
                if (din != null) din.close();
                if (dout != null) dout.close();
            }
        } catch (Exception ex) {
            if (plugin.getCfg().debug) ex.printStackTrace();
        }
    }


}