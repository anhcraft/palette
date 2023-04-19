package dev.anhcraft.palette;

public class ModifiableComponent {
    private int maxStackSize = 64;
    private boolean allowPlacing = true;
    private boolean allowTaking = true;

    public ModifiableComponent maxStackSize(int maxStackSize) {
        this.maxStackSize = Math.max(1, Math.min(64, maxStackSize));
        return this;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public ModifiableComponent disallowPlacing() {
        allowPlacing = false;
        return this;
    }

    public boolean isAllowPlacing() {
        return allowPlacing;
    }

    public ModifiableComponent disallowTaking() {
        allowTaking = false;
        return this;
    }

    public boolean isAllowTaking() {
        return allowTaking;
    }
}
