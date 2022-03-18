package me.gaagjescraft.network.team.manhunt.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.GameStatus;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import me.gaagjescraft.network.team.manhunt.utils.centerText.DefaultFontInfo;
import net.gcnt.additionsplus.api.AdditionsPlugin;
import net.gcnt.additionsplus.api.actions.ActionSender;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    public static int CENTER_PX = 120;

    public static void sendCenteredMessage(Player player, String message) {
        if (message == null || message.equals("")) player.sendMessage("");
        message = c(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        String strippedMsg = ChatColor.stripColor(message);

        for (char c : strippedMsg.toCharArray()) {
            if (c == '&') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb + message);
    }

    public static List<String> r(List<String> items, String search, String replaceAll) {
        items = new ArrayList<>(items);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, items.get(i).replaceAll(search, replaceAll));
        }
        return items;
    }

    public static String c(String a) {
        return ChatColor.translateAlternateColorCodes('&', a);
    }

    public static void sendTitle(Player player, String cfgMsg, int in, int stay, int out) {
        String[] titles = cfgMsg.split("\\\\n");
        if (titles.length == 0) return;
        player.sendTitle(c(titles[0]), titles.length >= 2 ? c(titles[1]) : "", in, stay, out);
    }

    public static void playSound(Player player, String soundString, float volume, float pitch) {
        Sound sound = null;

        if (soundString.contains(";")) {
            String[] split = soundString.split(";");
            if (!split[0].contains(":")) {
                try {
                    sound = Sound.valueOf(split[0].toUpperCase());
                } catch (Exception ignored) {
                }
            }

            if (split.length >= 3) {
                // getting the volume (2nd in the array).
                try {
                    volume = Float.parseFloat(split[1]);
                } catch (Exception ex) {
                    Manhunt.get().getLogger().severe("You entered a wrongly formatted volume for the sound string: '" + soundString + "'.");
                }
            }

            try {
                // getting the pitch (last in the array).
                pitch = Float.parseFloat(split[split.length - 1]);
            } catch (Exception ex) {
                Manhunt.get().getLogger().severe("You entered a wrongly formatted pitch for the sound string: '" + soundString + "'.");
            }
            // changing value of soundString for the code below so it will execute correctly if sound is null.
            soundString = split[0];
        } else {
            try {
                sound = Sound.valueOf(soundString.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        try {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            } else {
                player.playSound(player.getLocation(), soundString, volume, pitch);
            }
        } catch (Exception e) {
            Manhunt.get().getLogger().severe("We failed to play the sound '" + soundString + " for player " + player.getName() + ". Does it exist?");
        }
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

    public String secondsToTimeString(long seconds, String format) {
        if (format == null) format = "simplified";

        long secs;
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
            mins = remainder / 60;
            remainder = remainder - mins * 60;
            secs = remainder;
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
                } else if (secs == 0) {
                    return hours + " hour" + (hours > 1 ? "s" : "") + " and " + mins + " minute" + (mins > 1 ? "s" : "");
                }
                return hours + " hours, " + mins + " minutes, and " + seconds + " seconds";
            } else if (mins > 0) {
                if (secs == 0) {
                    return mins + " minute" + (mins > 1 ? "s" : "");
                }
                return mins + " minute" + (mins > 1 ? "s" : "") + " and " + secs + " second" + (secs > 1 ? "s" : "");
            } else {
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
        Field profileField;
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

    public int getProtocol(Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(player.getUniqueId());
        }
        return -1;
    }

    public void updateHealth(Game game) {
        for (GamePlayer gp : game.getPlayers()) {

            if (!gp.isOnline()) continue;
            Player target = Bukkit.getPlayer(gp.getUuid());
            if (target == null) continue;

            AdditionsBoard board = gp.getScoreboard();
            if (board == null) continue;

            if ((game.getStatus() != GameStatus.PLAYING && game.getStatus() != GameStatus.STOPPING) || (game.getStatus() == GameStatus.PLAYING && gp.getPlayerType() == PlayerType.HUNTER)) {
                Objective ob = board.getScoreboard().getObjective("mh-health");
                if (ob != null) ob.unregister();
                board.getScoreboard().clearSlot(DisplaySlot.BELOW_NAME);
                return;
            }

            Objective ob = board.getScoreboard().getObjective("mh-health");
            if (ob == null) ob = board.getScoreboard().registerNewObjective("mh-health", "dummy", "§c❤");
            ob.setDisplaySlot(DisplaySlot.BELOW_NAME);
            ob.setRenderType(RenderType.HEARTS);
            for (GamePlayer gp1 : game.getPlayers()) {
                if (!gp1.isOnline()) continue;
                Player p1 = Bukkit.getPlayer(gp1.getUuid());
                if (p1 == null) continue;
                ob.getScore(p1.getName()).setScore((int) p1.getHealth());
            }

            //target.setScoreboard(board.getScoreboard());
        }

    }

    public List<String> replace(List<String> a, String search, String replace) {
        List<String> b = new ArrayList<>(a);
        b.replaceAll(s -> s.replaceAll(search, replace));
        return b;
    }

    public void performRewardActions(Player p, Game game, List<String> actions) {
        if (Bukkit.getPluginManager().isPluginEnabled("Additions")) {
            AdditionsPlugin additions = (AdditionsPlugin) Bukkit.getPluginManager().getPlugin("Additions");
            ActionSender sender = additions.getAPI().getActionSender(p);
            additions.getAPI().performActions(sender, actions);
        } else {
            for (String s : actions) {
                if (s.startsWith("[message]")) {
                    p.sendMessage(c(s.replace("[message]", "")).replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName()));
                } else if (s.startsWith("[actionbar]")) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(c(s.replace("[actionbar]", "").replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName()))));
                } else if (s.startsWith("[broadcast]")) {
                    Bukkit.broadcastMessage(c(s.replace("[broadcast]", "")).replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName()));
                } else if (s.startsWith("[command]") || s.startsWith("[player]")) {
                    Bukkit.dispatchCommand(p, s.replace("[command]", "").replace("[player]", "").replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName()));
                } else if (s.startsWith("[server]") || s.startsWith("[console]")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("[server]", "").replace("[console]", "").replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName()));
                } else if (s.startsWith("[sound]")) {
                    p.playSound(p.getLocation(), Sound.valueOf(s.replace("[sound]", "").toUpperCase()), 1, 1);
                } else if (s.startsWith("[title]")) {
                    sendTitle(p, c(s.replace("[title]", "").replaceAll("%game%", game.getIdentifier()).replaceAll("%player%", p.getName())), 20, 40, 20);
                }
            }
        }
    }

    public boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
