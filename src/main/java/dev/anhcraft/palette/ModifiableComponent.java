package dev.anhcraft.palette;

public class ModifiableComponent {
    private int maxStackSize = 64;
    private boolean allowPlacing = true;
    private boolean allowTaking = true;

    /**
     * Sets the maximum stack size of this component.
     * @param maxStackSize the maximum stack size
     * @return this
     */
    public ModifiableComponent maxStackSize(int maxStackSize) {
        this.maxStackSize = Math.max(1, Math.min(64, maxStackSize));
        return this;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Disables placing items on this component.
     * @return this
     */
    public ModifiableComponent disallowPlacing() {
        allowPlacing = false;
        return this;
    }

    public boolean isAllowPlacing() {
        return allowPlacing;
    }

    /**
     * Disables taking items from this component.
     * @return this
     */
    public ModifiableComponent disallowTaking() {
        allowTaking = false;
        return this;
    }

    public boolean isAllowTaking() {
        return allowTaking;
    }
}
