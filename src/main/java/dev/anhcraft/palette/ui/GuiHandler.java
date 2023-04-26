package dev.anhcraft.palette.ui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.palette.event.Event;
import dev.anhcraft.palette.ui.element.Component;
import dev.anhcraft.palette.ui.element.Modifiability;
import dev.anhcraft.palette.ui.element.Slot;
import dev.anhcraft.palette.util.ItemReplacer;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class GuiHandler implements InventoryHolder {
    private Inventory inventory;
    private Slot[] slots;
    private Multimap<String, Integer> slotByComponents;

    void initialize(Inventory inventory, @NotNull Slot[] slots) {
        this.inventory = inventory;
        this.slots = slots;
        HashMultimap<String, Integer> map = HashMultimap.create();
        for (int i = 0; i < slots.length; i++) {
            map.put(slots[i].getComponent().getType(), i);
        }
        slotByComponents = map;
    }

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Gets the slots.
     * @return the slots
     */
    @NotNull
    public Slot[] getSlots() {
        return slots;
    }

    /**
     * Gets a specific slot
     * @param slot the slot index
     * @return slot
     */
    @NotNull
    public Slot getSlot(int slot) {
        return slots[slot];
    }

    /**
     * Gets the components.
     * @return the components
     */
    @NotNull
    public Set<String> getComponents() {
        return slotByComponents.keySet();
    }

    /**
     * Locates the given component.
     * @param component component
     * @return a collection of slots
     */
    @NotNull
    public Collection<Integer> locateComponent(String component) {
        return slotByComponents.get(component);
    }

    /**
     * Visits every slot of the given component.
     * @param component the component
     * @param consumer the slot consumer
     */
    public void visitComponent(String component, @NotNull Consumer<Slot> consumer) {
        for (int i : slotByComponents.get(component)) {
            consumer.accept(slots[i]);
        }
    }

    /**
     * Gets the component at a specific slot.
     * @param slot the slot
     * @return the component
     */
    @NotNull
    public Component getComponent(int slot) {
        return slots[slot].getComponent();
    }

    /**
     * Gets the component type at a specific slot.
     * @param slot the slot
     * @return the component type
     */
    @NotNull
    public String getComponentType(int slot) {
        return slots[slot].getComponent().getType();
    }

    /**
     * Gets the original item at a specific slot.
     * @param slot the slot
     * @return the original item
     */
    @NotNull
    public ItemStack getOriginalItem(int slot) {
        return slots[slot].getComponent().getBakedItem();
    }

    /**
     * Resets the item at a specific slot.
     * @param slot the slot
     */
    public void resetItem(int slot) {
        inventory.setItem(slot, getOriginalItem(slot));
    }

    /**
     * Resets all items in the inventory.
     * @param component the component (or null for all)
     */
    public void resetBulk(@Nullable String component) {
        if (component == null) {
            for (int i = 0; i < slots.length; ++i) {
                inventory.setItem(i, getOriginalItem(i));
            }
        } else {
            for (int i : slotByComponents.get(component)) {
                inventory.setItem(i, getOriginalItem(i));
            }
        }
    }

    /**
     * Sets the given item stack.
     * @param component the component (or null for all)
     * @param itemStack the item stack
     */
    public void setBulk(@Nullable String component, ItemStack itemStack) {
        if (component == null) {
            for (int i = 0; i < slots.length; ++i) {
                inventory.setItem(i, itemStack);
            }
        } else {
            for (int i : slotByComponents.get(component)) {
                inventory.setItem(i, itemStack);
            }
        }
    }

    /**
     * Listens to event at a specific slot.
     * @param slot the slot
     * @param event the event
     */
    public void listen(int slot, @NotNull Event event) {
        getSlot(slot).listen(event);
    }

    /**
     * Listens to event at a specific component.
     * @param component the component
     * @param event the event
     */
    public void listen(String component, @NotNull Event event) {
        for (int i : locateComponent(component)) {
            listen(i, event);
        }
    }

    /**
     * Gets the actual item at the given slot.<br>
     * For unmodifiable slot, the actual item is the current item shown in the inventory.<br>
     * For modifiable slot, the actual item is the current item if it differs from the original item, otherwise, empty.
     * @param index the slot index
     * @return the actual item
     */
    @NotNull
    public ItemStack getActualItem(int index) {
        ItemStack current = inventory.getItem(index);
        Slot slot = slots[index];
        if (slot.getModifiability() != null && (ItemUtil.isEmpty(current) || current.isSimilar(getOriginalItem(index)))) {
            return ItemUtil.EMPTY_ITEM;
        }
        return current == null ? ItemUtil.EMPTY_ITEM : current;
    }

    /**
     * Collects the first item belonging to the given component.
     * @param component the component
     * @return the collected item
     */
    @Nullable
    public ItemStack collectItem(@NotNull String component) {
        Iterator<Integer> i = slotByComponents.get(component).iterator();
        return i.hasNext() ? getActualItem(i.next()) : null;
    }

    /**
     * Collects the first existing item belonging to the given component.
     * @param component the component
     * @return the collected item
     */
    @Nullable
    public ItemStack collectPresentItem(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem).filter(ItemUtil::isPresent).findFirst().orElse(null);
    }

    /**
     * Streams the items belonging to the given component.
     * @param component the component
     * @return the collected item
     */
    @NotNull
    public Stream<ItemStack> streamItems(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem);
    }

    /**
     * Streams the existing items belonging to the given component.
     * @param component the component
     * @return the collected item
     */
    @NotNull
    public Stream<ItemStack> streamPresentItems(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem).filter(ItemUtil::isPresent);
    }

    /**
     * Replaces the item at given slot.
     * @param slot the slot
     * @param itemReplacer the item replacer
     */
    public void replaceItem(int slot, @NotNull ItemReplacer itemReplacer) {
        ItemStack item = itemReplacer.apply(slot, slots[slot].getComponent().duplicate()).build();
        inventory.setItem(slot, item);
    }

    /**
     * Replaces the items belonging to the given component.
     * @param component the component
     * @param itemReplacer the item replacer
     */
    public void replaceItem(@NotNull String component, @NotNull ItemReplacer itemReplacer) {
        for (int i : slotByComponents.get(component)) {
            replaceItem(i, itemReplacer);
        }
    }

    /**
     * Places the given item to modifiable slots.
     * @param item the item to be placed
     * @param component the component (optional)
     * @param test enables test mode (which does not alter the inventory)
     * @return the result
     */
    @NotNull
    public ItemPlaceResult placeItem(@NotNull ItemStack item, @Nullable String component, boolean test) {
        for (int i = 0; i < slots.length; i++) {
            if (component != null && !slots[i].getComponent().getType().equals(component)) {
                continue;
            }
            Modifiability modifiability = slots[i].getModifiability();
            if (modifiability != null && modifiability.isAllowPlacing()) {
                ItemStack current = getActualItem(i);
                int oldAmount = 0;
                int expectedNewAmount = item.getAmount();

                if (ItemUtil.isPresent(current)) {
                    if (current.isSimilar(item)) {
                        oldAmount = current.getAmount();
                        expectedNewAmount += oldAmount;
                    } else {
                        continue;
                    }
                }

                int stackSize = Math.min(item.getMaxStackSize(), modifiability.getMaxStackSize());
                int actualNewAmount = Math.min(stackSize, expectedNewAmount);
                if (!test) {
                    ItemStack toBePlaced = item.clone();
                    toBePlaced.setAmount(actualNewAmount);
                    inventory.setItem(i, toBePlaced);
                }
                return new ItemPlaceResult(oldAmount, actualNewAmount, expectedNewAmount - actualNewAmount, i, !test);
            }
        }
        return new ItemPlaceResult(item.getAmount(), item.getAmount(), 0, -1, false);
    }

    /**
     * Gets all collectable items from modifiable components which allow taking out items.
     * @param component the component (optional)
     * @return all collectable items
     */
    @NotNull
    public List<ItemStack> getCollectableItems(@Nullable String component) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) {
            if (component != null && !slots[i].getComponent().getType().equals(component)) {
                continue;
            }
            Modifiability modifiability = slots[i].getModifiability();
            if (modifiability != null && modifiability.isAllowTaking()) {
                ItemStack item = getActualItem(i);
                if (ItemUtil.isPresent(item)) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * Called when an {@link Inventory} is created, items are put and the inventory is going to be shown to viewer.
     * @param player the viewer
     */
    public abstract void onPreOpen(@NotNull Player player);

    /**
     * Called when the inventory is closed.
     * @param player the viewer
     */
    public void onClose(@NotNull Player player) {
        ItemUtil.addToInventory(player, getCollectableItems(null));
        resetBulk(null);
    }
}
