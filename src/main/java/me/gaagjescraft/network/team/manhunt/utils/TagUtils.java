package me.gaagjescraft.network.team.manhunt.utils;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.api.data.Nametag;
import com.nametagedit.plugin.api.events.NametagEvent;
import me.gaagjescraft.network.team.manhunt.Manhunt;
import me.gaagjescraft.network.team.manhunt.games.Game;
import me.gaagjescraft.network.team.manhunt.games.GamePlayer;
import me.gaagjescraft.network.team.manhunt.games.PlayerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TagUtils implements Listener {

    public TagUtils() {
        /*GroupData hgd = new GroupData();
        hgd.setGroupName("manhunt-hunters");
        hgd.setPrefix(getNametag(PlayerType.HUNTER).replaceAll("§", "&"));
        hgd.setSortPriority(0);
        hgd.setPermission("whateverpermission.people.should.not.get");

        GroupData rgd = new GroupData();
        rgd.setGroupName("manhunt-runners");
        rgd.setPrefix(getNametag(PlayerType.RUNNER).replaceAll("§", "&"));
        rgd.setSortPriority(-1);
        rgd.setPermission("whateverpermission.people.should.not.get");

        NametagEdit.getApi().saveGroupData(hgd, rgd);*/
    }

    public void updateTag(Player player) {
        Bukkit.getScheduler().runTask(Manhunt.get(), () -> {
            if (player == null) return;
            String nametag = getNametag(player);
            if (nametag == null) {
                NametagEdit.getApi().clearNametag(player);
                NametagEdit.getApi().applyTagToPlayer(player, false);
                return;
            }

            NametagEdit.getApi().setNametag(player, nametag, "");
            NametagEdit.getApi().applyTagToPlayer(player, false);
        });
    }

    public String getNametag(PlayerType type) {
        String nametag = type == PlayerType.HUNTER ? Manhunt.get().getCfg().hunterNametagPrefix : Manhunt.get().getCfg().runnerNametagPrefix;
        nametag = Util.c(nametag);
        nametag = nametag.replaceAll("%prefix%", Util.c(type == PlayerType.HUNTER ? Manhunt.get().getCfg().hunterChatPrefix : Manhunt.get().getCfg().runnerChatPrefix));
        nametag = nametag.replaceAll("%color%", Util.c(type == PlayerType.HUNTER ? Manhunt.get().getCfg().hunterColor : Manhunt.get().getCfg().runnerColor));
        return nametag;
    }

    public String getNametag(Player player) {
        Game game = Game.getGame(player);
        if (game == null || !game.getPlayer(player).isOnline()) return null;
        GamePlayer gp = game.getPlayer(player);
        return getNametag(gp.getPlayerType());
    }

    @EventHandler
    public void onTagApply(NametagEvent e) {
        String tag = getNametag(Bukkit.getPlayer(e.getPlayer()));
        if (tag == null) {
            e.setNametag(new Nametag("§7", ""));
        } else {
            e.setNametag(new Nametag(tag, ""));
        }
    }

}
