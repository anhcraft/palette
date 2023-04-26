package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface TakeBlockedEvent extends Event {
    /**
     * Called when an item could not be taken out.
     * @param player the viewer
     * @param slot the slot
     * @param item the item to be taken
     */
    void onTakeBlocked(@NotNull Player player, int slot, @NotNull ItemStack item);
}
