package dev.anhcraft.palette.util;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtil {
    public static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    public static boolean isEmpty(@Nullable ItemStack item) {
        return item == null || item.getType().isAir();
    }

    public static boolean isPresent(@Nullable ItemStack item) {
        return !isEmpty(item);
    }

    public static Pair<ItemStack, ItemStack> splitItem(@NotNull ItemStack item, int size) {
        if (isEmpty(item) || item.getAmount() == 0) {
            return new Pair<>(item, item);
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

    public static void addToInventory(@NotNull HumanEntity entity, @NotNull ItemStack ... items) {
        for (ItemStack i : entity.getInventory().addItem(items).values()) {
            entity.getWorld().dropItem(entity.getLocation(), i);
        }
    }
}
