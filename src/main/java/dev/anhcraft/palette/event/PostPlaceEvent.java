package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PostPlaceEvent extends Event {
    /**
     * Called when someone successfully placed item on a modifiable component.
     * @param action the action
     * @param player the viewer
     * @param slot the slot
     * @param item the item placed
     */
    void onPostPlace(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item);
}
