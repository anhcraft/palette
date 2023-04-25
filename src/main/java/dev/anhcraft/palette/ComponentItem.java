package dev.anhcraft.palette;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Configurable
public class ComponentItem extends ItemBuilder {
    private String type;

    @Exclude
    private ItemStack bakedItem;

    @PostHandler
    private void postHandle() {
        if (type == null || (type = type.trim()).isEmpty()) {
            type = "unknown";
        }
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
