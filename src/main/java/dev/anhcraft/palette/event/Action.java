package dev.anhcraft.palette.event;

public enum Action {
    /**
     * Left click to take the whole item and put it on the cursor.
     */
    TAKE,

    /**
     * Shift + click to take out the whole item and move it straight to the inventory.
     */
    QUICK_TAKE,

    /**
     * Right click to take a half of the item stack and put it on the cursor.
     */
    HALF_TAKE,

    /**
     * Click to put the item on cursor into the slot.
     */
    PLACE,

    /**
     * Shift + click an item in the bottom inventory into the slot of the top GUI.
     */
    QUICK_PLACE,

    /**
     * Right-click on an existing, same-typed item to merge it by one from cursor.
     */
    MERGE_ONE,

    /**
     * Left-click on an existing, same-typed item to merge the whole item from cursor.
     */
    MERGE_WHOLE,

    /**
     * Click on an existing item (different type) to replace it with the one from cursor.
     */
    REPLACE,
}
