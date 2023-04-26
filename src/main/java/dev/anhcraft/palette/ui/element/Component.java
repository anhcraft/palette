package dev.anhcraft.palette.ui.element;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Configurable
public class Component extends ItemBuilder {
    @Description("The type of the component")
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
