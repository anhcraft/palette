package dev.anhcraft.palette.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemFilter {
    boolean accept(@NotNull ItemStack itemStack);
}
