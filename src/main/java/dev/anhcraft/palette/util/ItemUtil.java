package dev.anhcraft.palette.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {
    public static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() == 0;
    }

    public static boolean isPresent(@Nullable ItemStack item) {
        return !isEmpty(item);
    }

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

    public static Pair<ItemStack, ItemStack> mergeItem(@NotNull ItemStack item, int addition) {
        return mergeItem(item, addition, item.getMaxStackSize());
    }

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

    public static void addToInventory(@NotNull HumanEntity entity, @NotNull ItemStack ... items) {
        for (ItemStack i : entity.getInventory().addItem(items).values()) {
            entity.getWorld().dropItem(entity.getLocation(), i);
        }
    }
}
