package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PreTakeEvent extends Event {
    /**
     * Called when someone is going to take out an item.
     * @param action the action
     * @param player the viewer
     * @param slot the slot
     * @param item the item to be taken
     * @return true if it is allowed, false otherwise
     */
    boolean onPreTake(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item);
}
