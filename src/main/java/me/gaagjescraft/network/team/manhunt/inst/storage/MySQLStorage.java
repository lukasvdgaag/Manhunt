package me.gaagjescraft.network.team.manhunt.inst.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLStorage implements PlayerStorage {

    private HikariConfig config;
    private HikariDataSource ds;

    private Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public void setup() {
        connect();
    }

    @Override
    public void connect() {
        String hostname = Manhunt.get().getCfg().databaseHostname;
        int port = 3306;
        if (hostname.contains(":")) {
            String[] split = hostname.split(":");
            hostname = split[0];
            port = Integer.parseInt(split[1]);
        }
        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + Manhunt.get().getCfg().databaseDatabase;

        config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(Manhunt.get().getCfg().databaseUsername);
        config.setPassword(Manhunt.get().getCfg().databasePassword);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(4000);
        ds = new HikariDataSource(config);


        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + Manhunt.get().getCfg().databaseDatabase);
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `manhunt_playerdata` (" +
                    "  `uuid`   VARCHAR(255)  NOT NULL UNIQUE," +
                    "  `best_speedrun_time`  INT(6) DEFAULT 0," +
                    "  `hunter_games_played` INT(10) DEFAULT 0," +
                    "  `runner_games_played` INT(10) DEFAULT 0," +
                    "  `hunter_kills` INT(12) DEFAULT 0," +
                    "  `runner_kills` INT(12) DEFAULT 0," +
                    "  `hunter_wins` INT(10) DEFAULT 0," +
                    "  `runner_wins` INT(10) DEFAULT 0," +
                    "  KEY  (`uuid`)" +
                    ")");
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Manhunt failed to connect to the MySQL database with the following hostname: '" + hostname + ":" + port + "'. Do you have access?");
            Bukkit.getLogger().severe("Here's the mysql URI we used: " + url);
            Bukkit.getLogger().severe("Disabling the plugin to prevent further complications...");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(Manhunt.get());
        }
    }

    @Override
    public PlayerStat loadUser(UUID uuid) {
        PlayerStat ps = getUser(uuid);
        final boolean userFound = ps != null;
        if (!userFound) {
            ps = new PlayerStat(uuid, 0, 0, 0, 0, 0, 0, 0);
        }

        try (Connection conn = getConnection()) {
            ResultSet res = conn.createStatement().executeQuery("SELECT * FROM `manhunt_playerdata` WHERE `uuid`='" + uuid.toString() + "' LIMIT 1");
            if (res.next()) {
                ps.setBestSpeedRunTime(res.getLong("best_speedrun_time"));
                ps.setHunterKills(res.getInt("hunter_kills"));
                ps.setRunnerKills(res.getInt("runner_kills"));
                ps.setHunterWins(res.getInt("hunter_wins"));
                ps.setRunnerWins(res.getInt("runner_wins"));
                ps.setHunterGamesPlayed(res.getInt("hunter_games_played"));
                ps.setRunnerGamesPlayed(res.getInt("runner_games_played"));
            } else {
                if (!userFound) stats.add(ps);
                saveUser(uuid);
                return ps;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Manhunt failed to load the player data of user '" + uuid.toString() + "' from the MySQL database.");
            e.printStackTrace();
        }

        if (!userFound) stats.add(ps);
        return ps;
    }

    @Override
    public void saveUser(UUID uuid) {
        PlayerStat ps = getUser(uuid);

        try (Connection conn = getConnection()) {
            if (conn.prepareStatement("SELECT * FROM `manhunt_playerdata` WHERE `uuid`='" + uuid.toString() + "'").executeQuery().next()) {
                PreparedStatement sm = conn.prepareStatement("UPDATE `manhunt_playerdata` SET `best_speedrun_time`=?, `hunter_kills`=?, `runner_kills`=?, `hunter_wins`=?, `runner_wins`=?, `hunter_games_played`=?, `runner_games_played`=? WHERE `uuid`=?");
                sm.setLong(1, ps != null ? ps.getBestSpeedRunTime() : 0);
                sm.setInt(2, ps != null ? ps.getHunterKills() : 0);
                sm.setInt(3, ps != null ? ps.getRunnerKills() : 0);
                sm.setInt(4, ps != null ? ps.getHunterWins() : 0);
                sm.setInt(5, ps != null ? ps.getRunnerWins() : 0);
                sm.setInt(6, ps != null ? ps.getHunterGamesPlayed() : 0);
                sm.setInt(7, ps != null ? ps.getRunnerGamesPlayed() : 0);
                sm.setString(8, uuid.toString());
                sm.executeUpdate();
                sm.close();
            } else {
                PreparedStatement sm = conn.prepareStatement("INSERT INTO `manhunt_playerdata` (`uuid`, `best_speedrun_time`, `hunter_kills`, `runner_kills`, `hunter_wins`, `runner_wins`, `hunter_games_played`, `runner_games_played`) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
                sm.setString(1, uuid.toString());
                sm.setLong(2, ps != null ? ps.getBestSpeedRunTime() : 0);
                sm.setInt(3, ps != null ? ps.getHunterKills() : 0);
                sm.setInt(4, ps != null ? ps.getRunnerKills() : 0);
                sm.setInt(5, ps != null ? ps.getHunterWins() : 0);
                sm.setInt(6, ps != null ? ps.getRunnerWins() : 0);
                sm.setInt(7, ps != null ? ps.getHunterGamesPlayed() : 0);
                sm.setInt(8, ps != null ? ps.getRunnerGamesPlayed() : 0);
                sm.executeUpdate();
                sm.close();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Manhunt failed to save the player data of user '" + uuid.toString() + "' to the MySQL database.");
            e.printStackTrace();
        }
    }
}
