package me.gaagjescraft.network.team.manhunt.menus.twist;

import me.gaagjescraft.network.team.manhunt.games.TwistVote;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class TwistItem {
    private TwistVote vote;
    private String icon;
    private String displayName;
    private List<String> lore;
    private Consumer<ItemStack> customizer;

    public TwistItem(TwistVote vote, String icon, String displayName, List<String> lore, Consumer<ItemStack> customizer) {
        this.vote = vote;
        this.icon = icon;
        this.displayName = displayName;
        this.lore = lore;
        this.customizer = customizer;
    }

    public TwistItem(TwistVote vote, String icon, String displayName, List<String> lore) {
        this.vote = vote;
        this.icon = icon;
        this.displayName = displayName;
        this.lore = lore;
        this.customizer = (ignored) -> {};
    }

    public TwistVote getVote() {
        return vote;
    }

    public void setVote(TwistVote vote) {
        this.vote = vote;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public Consumer<ItemStack> getCustomizer() {
        return customizer;
    }

    public void setCustomizer(Consumer<ItemStack> customizer) {
        this.customizer = customizer;
    }
}
