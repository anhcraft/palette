package dev.anhcraft.palette.ui;

public class ItemPlaceResult {
    private final int targetOldAmount;
    private final int targetNewAmount;
    private final int remainder;
    private final int slot;
    private final boolean changed;

    public ItemPlaceResult(int targetOldAmount, int targetNewAmount, int remainder, int slot, boolean changed) {
        this.targetOldAmount = targetOldAmount;
        this.targetNewAmount = targetNewAmount;
        this.remainder = remainder;
        this.slot = slot;
        this.changed = changed;
    }

    public int getTargetOldAmount() {
        return targetOldAmount;
    }

    public int getTargetNewAmount() {
        return targetNewAmount;
    }

    public int getRemainder() {
        return remainder;
    }

    public int getSlot() {
        return slot;
    }

    public boolean wasChanged() {
        return changed;
    }
}
