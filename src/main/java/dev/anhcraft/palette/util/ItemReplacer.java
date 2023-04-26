package dev.anhcraft.palette.util;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import org.jetbrains.annotations.NotNull;

public interface ItemReplacer {
    @NotNull
    ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder);
}
