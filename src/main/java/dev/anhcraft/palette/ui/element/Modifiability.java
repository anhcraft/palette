package dev.anhcraft.palette.ui.element;

import dev.anhcraft.palette.util.ItemFilter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Modifiability {
    private int maxStackSize = 64;
    private boolean allowPlacing = true;
    private boolean allowTaking = true;
    private ItemFilter itemFilter;

    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Sets the maximum stack size of this component.
     * @param maxStackSize the maximum stack size
     * @return this
     */
    public Modifiability maxStackSize(int maxStackSize) {
        this.maxStackSize = Math.max(1, Math.min(64, maxStackSize));
        return this;
    }

    public boolean isAllowPlacing() {
        return allowPlacing;
    }

    /**
     * Disables placing items on this component.
     * @return this
     */
    public Modifiability disallowPlacing() {
        allowPlacing = false;
        return this;
    }

    public boolean isAllowTaking() {
        return allowTaking;
    }

    /**
     * Disables taking items from this component.
     * @return this
     */
    public Modifiability disallowTaking() {
        allowTaking = false;
        return this;
    }

    public boolean canPlace(@NotNull ItemStack itemStack) {
        return allowPlacing && (itemFilter == null || itemFilter.accept(itemStack));
    }

    /**
     * Sets the item filter for this component
     * @param itemFilter the item filter
     * @return this
     */
    public Modifiability filter(@NotNull ItemFilter itemFilter) {
        this.itemFilter = itemFilter;
        return this;
    }
}
