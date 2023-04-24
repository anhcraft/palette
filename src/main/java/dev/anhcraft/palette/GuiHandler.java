package dev.anhcraft.palette;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.palette.util.Pair;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class GuiHandler implements InventoryHolder {
    private final Map<String, ModifiableComponent> modifiableComponents = new HashMap<>();
    private Inventory inventory;
    private ComponentItem[] backupLayer;
    private Multimap<String, Integer> slotByComponents = HashMultimap.create();

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    public Map<String, ModifiableComponent> getModifiableComponents() {
        return this.modifiableComponents;
    }

    /**
     * Creates a new modifiable component and adds it to the GUI handler.
     * @param type the type of the modifiable component
     * @return the modifiable component
     */
    @NotNull
    public ModifiableComponent createModifiableComponent(@NotNull String type) {
        ModifiableComponent component = new ModifiableComponent();
        this.modifiableComponents.put(type, component);
        return component;
    }

    @NotNull
    public ComponentItem[] getBackupLayer() {
        return this.backupLayer;
    }

    void setBackupLayer(@NotNull ComponentItem[] backupLayer) {
        this.backupLayer = backupLayer;
        HashMultimap<String, Integer> map = HashMultimap.create();
        for (int i = 0; i < backupLayer.length; ++i) {
            map.put(backupLayer[i].getType(), i);
        }
        slotByComponents = map;
    }

    @NotNull
    public ItemStack getBackupItem(int slot) {
        return backupLayer[slot].getBakedItem();
    }

    /**
     * Renders the backup layer.
     */
    public void renderBackupLayer() {
        for (int i = 0; i < this.backupLayer.length; ++i) {
            inventory.setItem(i, getBackupItem(i));
        }
    }

    @NotNull
    public String getComponentType(int slot) {
        return backupLayer[slot].getType();
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
     * Gets the actual item at the given slot.<br>
     * An actual item must be present and not be the background item.
     * @param slot the slot
     * @return the actual item
     */
    @NotNull
    public ItemStack getActualItem(int slot) {
        ItemStack item = inventory.getItem(slot);
        ItemStack backupItem = getBackupItem(slot);
        if (ItemUtil.isEmpty(item) || item.isSimilar(backupItem)) {
            return ItemUtil.EMPTY_ITEM;
        }
        return item;
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
     * Resets the items belonging to the given component.
     * @param component the component
     */
    public void resetItems(@NotNull String component) {
        for (Integer i : slotByComponents.get(component)) {
            inventory.setItem(i, getBackupItem(i));
        }
    }

    /**
     * Replaces the items belonging to the given component.
     * @param component the component
     * @param itemBuilderOperator an operator to handle item builder
     */
    public void replaceItems(@NotNull String component, @NotNull UnaryOperator<ItemBuilder> itemBuilderOperator) {
        for (Integer i : slotByComponents.get(component)) {
            ItemStack item = itemBuilderOperator.apply(backupLayer[i].duplicate()).build();
            inventory.setItem(i, item);
        }
    }

    /**
     * Sets the given item to the first empty slot belonging to the given component.<br>
     * This method does not try to merge the item even if the current amount does not exceed the stack size; in this case,
     * {@link #tryPlace(ItemStack)} is preferred.
     * @param component the component
     * @param itemStack the item
     */
    public void setItemOnce(@NotNull String component, @Nullable ItemStack itemStack) {
        Iterator<Integer> i = slotByComponents.get(component).iterator();
        if (i.hasNext()) {
            int slot = i.next();
            if (ItemUtil.isEmpty(getActualItem(slot))) {
                inventory.setItem(slot, itemStack);
            }
        }
    }

    /**
     * Finds the first empty slot which allows the given item to be put in.
     * @param item the item
     * @return the first empty slot
     */
    public int findEmptySlot(@NotNull ItemStack item) {
        for (String s : modifiableComponents.keySet()) {
            if (canPut(s, item)) {
                for (int i : slotByComponents.get(s)) {
                    if (ItemUtil.isEmpty(getActualItem(i))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Finds the first placeable slot which allows the given item to be put in / merged.
     * @param item the item
     * @return the first placeable slot
     */
    public int findPlaceableSlot(@NotNull ItemStack item) {
        for (Map.Entry<String, ModifiableComponent> e : modifiableComponents.entrySet()) {
            if (canPut(e.getKey(), item)) {
                for (int i : slotByComponents.get(e.getKey())) {
                    ItemStack target = getActualItem(i);
                    if (ItemUtil.isEmpty(target) || (target.getAmount() < e.getValue().getMaxStackSize() && target.isSimilar(item))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Tries to place the item in any modifiable component that allows it to be placed or merged to existing one.
     * @param item the item
     * @return the remainder
     */
    @NotNull
    public ItemStack tryPlace(@NotNull ItemStack item) {
        for (Map.Entry<String, ModifiableComponent> e : modifiableComponents.entrySet()) {
            if (canPut(e.getKey(), item) && e.getValue().isAllowPlacing()) {
                for (int i : slotByComponents.get(e.getKey())) {
                    ItemStack target = getActualItem(i);
                    if (ItemUtil.isEmpty(target)) {
                        inventory.setItem(i, item);
                        return ItemUtil.EMPTY_ITEM;
                    } else if (target.getAmount() < e.getValue().getMaxStackSize() && target.isSimilar(item)) {
                        Pair<ItemStack, ItemStack> p = ItemUtil.mergeItem(target, item.getAmount(), e.getValue().getMaxStackSize());
                        inventory.setItem(i, p.getFirst());
                        return p.getSecond();
                    }
                }
            }
        }
        return item;
    }

    /**
     * Gets all collectable items from modifiable components which allow taking out items.
     * @return all collectable items
     */
    @NotNull
    public ItemStack[] getCollectableItems() {
        return modifiableComponents.entrySet().stream()
                .filter(e -> e.getValue().isAllowTaking())
                .map(Map.Entry::getKey)
                .flatMap(this::streamPresentItems)
                .toArray(ItemStack[]::new);
    }

    /**
     * Called when an {@link Inventory} is created, items are put and the inventory is going to be shown to viewer.
     * @param humanEntity the viewer
     */
    public abstract void onRendered(@NotNull HumanEntity humanEntity);

    /**
     * Called when someone clicks on a component.
     * The component will never be modifiable since any cases related to it is handled by Palette automatically.
     * @param clickEvent the event
     * @param component the component
     */
    public abstract void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull String component);

    /**
     * Called when the inventory is closed.
     * @param humanEntity the viewer
     */
    public void onClose(@NotNull HumanEntity humanEntity) {
        ItemUtil.addToInventory(humanEntity, getCollectableItems());
        renderBackupLayer();
    }

    /**
     * Called to check if the given item can be put in the given component.
     * @param component the component
     * @param item the item
     * @return true if the item can be put
     */
    public boolean canPut(@NotNull String component, @NotNull ItemStack item) {
        return true;
    }
}
