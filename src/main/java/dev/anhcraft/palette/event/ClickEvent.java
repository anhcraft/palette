package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public interface ClickEvent extends Event {
    /**
     * Called when someone clicks on an unmodifiable component.
     * @param clickEvent the event
     * @param player the viewer
     * @param slot the slot
     */
    void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot);
}
