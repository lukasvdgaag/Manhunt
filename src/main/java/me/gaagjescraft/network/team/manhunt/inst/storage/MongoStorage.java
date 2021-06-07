package me.gaagjescraft.network.team.manhunt.inst.storage;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import joptsimple.internal.Strings;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class MongoStorage implements PlayerStorage {

    private MongoCollection<Document> collection;

    @Override
    public void setup() {
        connect();
    }

    private String enc(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF-8");
    }

    @Override
    public void connect() {
        Bukkit.getScheduler().runTaskAsynchronously(Manhunt.get(), () -> {
            String hostname = Manhunt.get().getCfg().databaseHostname;
            int port = 27017;
            if (hostname.contains(":")) {
                String[] split = hostname.split(":");
                hostname = split[0];
                port = Integer.parseInt(split[1]);
            }

            String url = "null";
            try {
                MongoCredential credential = null;
                if (!Strings.isNullOrEmpty(Manhunt.get().getCfg().databaseUsername)) {
                    credential = MongoCredential.createCredential(Manhunt.get().getCfg().databaseUsername, Manhunt.get().getCfg().databaseDatabase, Manhunt.get().getCfg().databasePassword.toCharArray());
                }


                MongoClient client;
                if (credential == null) {
                    url = "mongodb://" + enc(hostname) + ":" + port;
                } else {
                    url = "mongodb://" + enc(Manhunt.get().getCfg().databaseUsername) + ":" + enc(Manhunt.get().getCfg().databasePassword) + "@" + enc(hostname) + ":" + port + "/?authSource=" + Manhunt.get().getCfg().databaseDatabase;
                }
                client = new MongoClient(new MongoClientURI(url)); //MongoClients.create(url);

                MongoDatabase database = client.getDatabase(Manhunt.get().getCfg().databaseDatabase);
                collection = database.getCollection("manhunt_stats");
            } catch (Exception | Error e) {
                Bukkit.getLogger().severe("Manhunt failed to connect to the MongoDB database with the following hostname: '" + hostname + ":" + port + "'. Do you have access?");
                Bukkit.getLogger().severe("Here's the mongodb URI we used: " + url);
                Bukkit.getLogger().severe("Disabling the plugin to prevent further complications...");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(Manhunt.get());
                return;
            }
            Bukkit.getLogger().info("Manhunt has successfully connected to the MongoDB database!");

            for (Player p : Bukkit.getOnlinePlayers()) {
                loadUser(p.getUniqueId());
            }
        });
    }

    @Override
    public PlayerStat loadUser(UUID uuid) {
        Document filter = new Document("uuid", uuid.toString());
        Document found = collection.find(filter).first();
        PlayerStat ps = getUser(uuid);
        final boolean userFound = ps != null;
        if (!userFound) ps = new PlayerStat(uuid, 0, 0, 0, 0, 0, 0, 0);

        if (found == null) {
            if (!userFound) stats.add(ps);
            saveUser(uuid);
            return ps;
        }

        ps.setBestSpeedRunTime(found.getLong("best_speedrun_time"));
        ps.setHunterKills(found.getInteger("hunter_kills", 0));
        ps.setRunnerKills(found.getInteger("runner_kills", 0));
        ps.setRunnerWins(found.getInteger("hunter_wins", 0));
        ps.setHunterWins(found.getInteger("runner_wins", 0));
        ps.setHunterGamesPlayed(found.getInteger("hunter_games_played", 0));
        ps.setRunnerGamesPlayed(found.getInteger("runner_games_played", 0));

        if (!userFound) stats.add(ps);
        return ps;
    }

    @Override
    public void saveUser(UUID uuid) {
        PlayerStat ps = getUser(uuid);

        Document filter = new Document("uuid", uuid.toString());
        Document doc = new Document();
        doc.append("best_speedrun_time", ps != null ? ps.getBestSpeedRunTime() : 0);
        doc.append("hunter_kills", ps != null ? ps.getHunterKills() : 0);
        doc.append("runner_kills", ps != null ? ps.getRunnerKills() : 0);
        doc.append("hunter_wins", ps != null ? ps.getHunterWins() : 0);
        doc.append("runner_wins", ps != null ? ps.getRunnerWins() : 0);
        doc.append("hunter_games_played", ps != null ? ps.getHunterGamesPlayed() : 0);
        doc.append("runner_games_played", ps != null ? ps.getRunnerGamesPlayed() : 0);

        if (collection.count(filter) == 0) {
            collection.insertOne(new Document("$set", doc));
        } else {
            collection.updateOne(filter, new Document("$set", doc), new UpdateOptions().upsert(true));
        }
    }
}
