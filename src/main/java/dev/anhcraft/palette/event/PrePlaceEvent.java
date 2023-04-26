package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PrePlaceEvent extends Event {
    /**
     * Called when someone going to place item on a modifiable component.
     * @param action the action
     * @param player the viewer
     * @param slot the slot
     * @param item the item to be placed
     * @return true if it is allowed, false otherwise
     */
    boolean onPrePlace(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item);
}
