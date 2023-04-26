package dev.anhcraft.palette.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PostTakeEvent extends Event {
    /**
     * Called when someone successfully taken out an item.
     * @param action the action
     * @param player the viewer
     * @param slot the slot
     * @param item the item taken
     */
    void onPostTake(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item);
}
