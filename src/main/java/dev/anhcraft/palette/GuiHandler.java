package dev.anhcraft.palette;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.config.Component;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class GuiHandler implements InventoryHolder {
    private final List<String> modifiableComponents = new ArrayList<>();
    private Inventory inventory;
    private Component[] backupLayer;
    private Multimap<String, Integer> slotByComponents = HashMultimap.create();

    @NotNull
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    public List<String> getModifiableComponents() {
        return this.modifiableComponents;
    }

    @NotNull
    public Component[] getBackupLayer() {
        return this.backupLayer;
    }

    public void setBackupLayer(@NotNull Component[] backupLayer) {
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

    public void renderBackupLayer() {
        for (int i = 0; i < this.backupLayer.length; ++i) {
            inventory.setItem(i, getBackupItem(i));
        }
    }

    @NotNull
    public String getComponentType(int slot) {
        return backupLayer[slot].getType();
    }

    @NotNull
    public ItemStack getActualItem(int slot) {
        ItemStack item = inventory.getItem(slot);
        ItemStack backupItem = getBackupItem(slot);
        if (ItemUtil.isEmpty(item) || item.isSimilar(backupItem)) {
            return ItemUtil.EMPTY_ITEM;
        }
        return item;
    }

    @Nullable
    public ItemStack collectItem(@NotNull String component) {
        Iterator<Integer> i = slotByComponents.get(component).iterator();
        return i.hasNext() ? getActualItem(i.next()) : null;
    }

    @Nullable
    public ItemStack collectPresentItem(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem).filter(ItemUtil::isPresent).findFirst().orElse(null);
    }

    @NotNull
    public Stream<ItemStack> streamItems(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem);
    }

    @NotNull
    public Stream<ItemStack> streamPresentItems(@NotNull String component) {
        return slotByComponents.get(component).stream().map(this::getActualItem).filter(ItemUtil::isPresent);
    }

    public void resetItems(@NotNull String component) {
        for (Integer i : slotByComponents.get(component)) {
            inventory.setItem(i, getBackupItem(i));
        }
    }

    public void replaceItems(@NotNull String component, @NotNull UnaryOperator<ItemBuilder> itemBuilderOperator) {
        for (Integer i : slotByComponents.get(component)) {
            ItemStack item = itemBuilderOperator.apply(backupLayer[i].duplicate()).build();
            inventory.setItem(i, item);
        }
    }

    public void setItemOnce(@NotNull String component, @Nullable ItemStack itemStack) {
        Iterator<Integer> i = slotByComponents.get(component).iterator();
        if (i.hasNext()) {
            inventory.setItem(i.next(), itemStack);
        }
    }

    public int findPlaceableSlot(@NotNull ItemStack item) {
        for (String s : modifiableComponents) {
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

    @NotNull
    public ItemStack[] getCollectableItems() {
        return modifiableComponents.stream().flatMap(this::streamPresentItems).toArray(ItemStack[]::new);
    }

    public abstract void onRendered(@NotNull HumanEntity humanEntity);

    public abstract void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull String component);

    public void onClose(@NotNull HumanEntity humanEntity) {
        ItemUtil.addToInventory(humanEntity, getCollectableItems());
        renderBackupLayer();
    }

    public boolean canPut(@NotNull String component, @NotNull ItemStack cursor) {
        return true;
    }
}
