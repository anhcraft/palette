package dev.anhcraft.palette.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Configurable
public class Component extends ItemBuilder {
    private String type;
    private ItemStack bakedItem;

    @PostHandler
    private void postHandle() {
        if (type == null || (type = type.trim()).isEmpty()) {
            type = "unknown";
        }
        replaceDisplay(s -> ChatColor.translateAlternateColorCodes('&', s));
        bakedItem = build();
    }

    @NotNull
    public String getType() {
        return this.type;
    }


    @NotNull
    public ItemStack getBakedItem() {
        return bakedItem;
    }
}
