package dev.anhcraft.palette.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {
    public static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    /**
     * Checks if an item is empty.
     * @param item the item to be checked
     * @return {@code true} if the item is empty, {@code false} otherwise
     */
    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() == 0;
    }

    /**
     * Checks if an item is not empty.
     * @param item the item to be checked
     * @return {@code true} if the item is not empty, {@code false} otherwise
     */
    public static boolean isPresent(@Nullable ItemStack item) {
        return !isEmpty(item);
    }

    /**
     * Splits the given item by {@code size}.
     * @param item the item to be split
     * @param size the size of the split
     * @return a pair of the split item and the remainder
     */
    public static Pair<ItemStack, ItemStack> splitItem(@NotNull ItemStack item, int size) {
        Preconditions.checkArgument(size > 0, "size must be positive");

        if (isEmpty(item) || item.getAmount() == 0) {
            return new Pair<>(item, EMPTY_ITEM);
        }

        size = Math.min(size, item.getAmount());
        ItemStack sub = item.clone();
        sub.setAmount(size);

        int remain = Math.max(0, item.getAmount() - size);
        if (remain == 0) {
            return new Pair<>(sub, EMPTY_ITEM);
        }

        ItemStack rest = item.clone();
        rest.setAmount(remain);
        return new Pair<>(sub, rest);
    }

    /**
     * Adds the given amount to the given item.
     * @param item the item to be modified
     * @param addition the amount to be added
     * @return a pair of the modified item and the remainder (which exceeded the maximum stack size)
     */
    public static Pair<ItemStack, ItemStack> mergeItem(@NotNull ItemStack item, int addition) {
        return mergeItem(item, addition, item.getMaxStackSize());
    }

    /**
     * Adds the given amount to the given item.
     * @param item the item to be modified
     * @param addition the amount to be added
     * @param maxStackSize the maximum stack size
     * @return a pair of the modified item and the remainder (which exceeded the maximum stack size)
     */
    public static Pair<ItemStack, ItemStack> mergeItem(@NotNull ItemStack item, int addition, int maxStackSize) {
        Preconditions.checkArgument(addition > 0, "addition must be positive");
        Preconditions.checkArgument(maxStackSize > 0, "maxStackSize must be positive");

        maxStackSize = Math.min(maxStackSize, item.getMaxStackSize());
        int total = item.getAmount() + addition;
        ItemStack current = item.clone();
        current.setAmount(Math.min(maxStackSize, total));
        int remain = total - current.getAmount();

        if (remain > 0) {
            ItemStack rest = item.clone();
            rest.setAmount(Math.min(remain, maxStackSize));
            return new Pair<>(current, rest);
        } else {
            return new Pair<>(current, EMPTY_ITEM);
        }
    }

    /**
     * Adds items to the given entity, and drops if the inventory is full
     * @param entity the entity
     * @param items the items to be added
     */
    public static void addToInventory(@NotNull HumanEntity entity, @NotNull ItemStack ... items) {
        for (ItemStack i : entity.getInventory().addItem(items).values()) {
            entity.getWorld().dropItem(entity.getLocation(), i);
        }
    }
}
