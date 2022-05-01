package me.gaagjescraft.network.team.manhunt.inst.storage;

import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.inst.PlayerStat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YamlStorage implements PlayerStorage {

    private File file;
    private FileConfiguration conf;

    private final Manhunt plugin;

    public YamlStorage(Manhunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup() {
        file = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!file.exists()) {
            try {
                boolean res = file.createNewFile();
                if (!res) {
                    Bukkit.getLogger().severe("Manhunt failed to create the playerdata.yml file");
                    Bukkit.getLogger().severe("Disabling the plugin to prevent further complications...");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("Manhunt failed to create the playerdata.yml file");
                Bukkit.getLogger().severe("Disabling the plugin to prevent further complications...");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }

        connect();
    }

    @Override
    public void connect() {
        conf = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public PlayerStat loadUser(UUID uuid) {
        PlayerStat ps = getUser(uuid);
        final boolean userFound = ps != null;
        if (!userFound) {
            ps = new PlayerStat(uuid, 0, 0, 0, 0, 0, 0, 0);
        }

        if (conf.contains(uuid.toString())) {
            ConfigurationSection res = conf.getConfigurationSection(uuid.toString());
            assert res != null;
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

        if (!userFound) stats.add(ps);
        return ps;
    }

    @Override
    public void saveUser(UUID uuid) {
        PlayerStat ps = getUser(uuid);

        conf.set(uuid + ".best_speedrun_time", ps != null ? ps.getBestSpeedRunTime() : 0);
        conf.set(uuid + ".hunter_kills", ps != null ? ps.getHunterKills() : 0);
        conf.set(uuid + ".runner_kills", ps != null ? ps.getRunnerKills() : 0);
        conf.set(uuid + ".hunter_wins", ps != null ? ps.getHunterWins() : 0);
        conf.set(uuid + ".runner_wins", ps != null ? ps.getRunnerWins() : 0);
        conf.set(uuid + ".hunter_games_played", ps != null ? ps.getHunterGamesPlayed() : 0);
        conf.set(uuid + ".runner_games_played", ps != null ? ps.getRunnerGamesPlayed() : 0);

        try {
            conf.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Manhunt failed to save the player data of user '" + uuid + "' to the YAML file.");
            e.printStackTrace();
        }
    }
}
