package dev.anhcraft.palette;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

public interface Refreshable {
    /**
     * Refresh the view.
     * @param humanEntity the viewer
     */
    void refreshView(@NotNull HumanEntity humanEntity);
}
