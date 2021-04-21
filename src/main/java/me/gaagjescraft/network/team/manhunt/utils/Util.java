package me.gaagjescraft.network.team.manhunt.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.GameSetup;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class Util {

    public void createGameServer(GameSetup setup, String targetGameServer) {
        Player host = setup.getHost();
        String json = "{'server_name': '" + Manhunt.get().getConfig().getString("server_name") + "', 'game_server':'" + targetGameServer + "', 'host':'" + host.getUniqueId() + "', 'host_uuid':'" + setup.getHost().getUniqueId().toString() + "', " +
                "'max_players':" + setup.getMaxPlayers() + ", 'headstart:'" + setup.getHeadstart().name() + "', " +
                "'allow_twists':" + setup.isAllowTwists() + ", 'daylight_cycle':" + setup.isDoDaylightCycle() + ", 'friendly_fire':" + setup.isAllowFriendlyFire() + "}";
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("createGame");
        out.writeUTF(json);

        host.sendPluginMessage(Manhunt.get(), "exodus:manhunt", out.toByteArray());
    }

    public void spawnAcidParticles(Location loc, boolean hit) {
        for (int i1 = 0; i1 < 50; i1++) {
            if (hit) {
                loc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, loc.add(0, 0.2, 0), 0, 181 / 255D, 5 / 255D, 38 / 255D, 1);
            } else {
                if (i1 % 2 == 0) {
                    loc.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, loc.add(0, 0.2, 0), 0, 26 / 255D, 102 / 255D, 14 / 255D, 1);
                } else {
                    loc.getWorld().spawnParticle(Particle.SPELL_MOB, loc.add(0, 0.2, 0), 0, 66 / 255D, 161 / 255D, 51 / 255D, 1);
                }
            }
        }
    }

    public String secondsToTimeString(int seconds, String format) {
        if (format == null) format = "simplified";

        int secs;
        int mins = 0;
        int hours = 0;

        if (seconds < 60) {
            secs = seconds;
        } else if (seconds < 3600) {
            mins = (int) seconds / 60;
            secs = (int) seconds % 60;
        } else {
            hours = (int) seconds / 3600;
            int remainder = (int) seconds - hours * 3600;
            mins = (int) remainder / 60;
            remainder = remainder - mins * 60;
            secs = (int) remainder;
        }

        if (format.equals("simplified")) {
            if (hours > 0) {
                return hours + ":" + (mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs);
            } else if (mins > 0) {
                return mins + ":" + (secs < 10 ? "0" + secs : secs);
            } else {
                return secs + "";
            }
        }
        if (format.equals("simplified-zeros")) {
            if (hours > 0) {
                return (hours < 10 ? "0" + hours : hours) + ":" + (mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs);
            } else if (mins > 0) {
                return (mins < 10 ? "0" + mins : mins) + ":" + (secs < 10 ? "0" + secs : secs);
            } else {
                return secs + "";
            }
        }
        if (format.equals("string")) {
            if (hours > 0) {
                if (mins == 0 && secs == 0) {
                    return hours + " hour" + (hours > 1 ? "s" : "");
                }
                else if (secs == 0) {
                    return hours + " hour"  + (hours > 1 ? "s" : "") + " and " + mins + " minute" + (mins > 1 ? "s" : "");
                }
                return hours + " hours, " + mins + " minutes, and " + seconds + " seconds";
            }
            else if (mins > 0) {
                if (secs == 0) {
                    return mins + " minute" + (mins > 1 ? "s" : "");
                }
                return mins + " minute" + (mins > 1 ? "s" : "") + " and " + secs + " second" + (secs > 1 ? "s" : "");
            }
            else {
                return secs + " second" + (secs > 1 ? "s" : "");
            }
        }

        format = format.replaceAll("(?<!\\\\)mm", mins < 10 ? "0" + mins : mins + "");
        format = format.replaceAll("(?<!\\\\)m", mins + "");
        format = format.replaceAll("(?<!\\\\)ss", secs < 10 ? "0" + secs : secs + "");
        format = format.replaceAll("(?<!\\\\)s", secs + "");

        return format;
    }

    public ItemStack getCustomTextureHead(String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

}
