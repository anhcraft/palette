package dev.anhcraft.palette;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public interface Refreshable {
    void refreshView(@NotNull HumanEntity humanEntity);
}
