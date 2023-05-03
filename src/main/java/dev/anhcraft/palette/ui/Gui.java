package dev.anhcraft.palette.ui;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.palette.ui.element.Component;
import dev.anhcraft.palette.ui.element.Slot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configurable
public class Gui {
    @Description("The title of the GUI")
    @Validation(notNull = true, notEmpty = true)
    public String title;

    @Description("The layout of the GUI")
    @Validation(notNull = true, notEmpty = true)
    public List<String> layout;

    @Description("The components of the GUI")
    public Map<Character, Component> components;

    @Description("The sound to play when opening the GUI")
    @Optional
    public Sound openSound = Sound.UI_TOAST_IN;

    @PostHandler
    private void postHandle() {
        title = title.trim();
        layout = layout.stream().map(String::trim).collect(Collectors.toList());
        int len = layout.get(0).length();
        for (int i = 1; i < layout.size(); i++) {
            String s = layout.get(i);
            if (s.length() != len) {
                throw new RuntimeException(String.format("Invalid layout detected! Inconsistent length %s, %s", len, s.length()));
            }
        }
    }

    /**
     * Opens a GUI for the given entity.
     * @param player The entity to open the GUI for
     * @param guiHandler The GUI handler
     */
    public void open(@NotNull Player player, @NotNull GuiHandler guiHandler) {
        InventoryHolder h = player.getOpenInventory().getTopInventory().getHolder();
        if (h instanceof GuiHandler) {
            ((GuiHandler) h).onClose(player);
        }

        Inventory inv = Bukkit.createInventory(guiHandler, layout.size() * 9, ChatColor.translateAlternateColorCodes('&', title));
        Slot[] slots = new Slot[inv.getSize()];

        for (int y = 0; y < layout.size(); ++y) {
            String s = layout.get(y);
            for (int x = 0; x < s.length(); ++x) {
                int i = y * 9 + x;
                Component component = components.get(s.charAt(x));
                if (component == null) {
                    throw new RuntimeException(String.format("Component not found %s at slot x=%d, y=%d, i=%d", s.charAt(x), x, y, i));
                }
                slots[i] = new Slot(component);
            }
        }

        guiHandler.initialize(inv, slots);
        guiHandler.resetBulk(null);
        guiHandler.onPreOpen(player);
        player.openInventory(inv);
        player.playSound(player.getLocation(), openSound, 1.0f, 1.0f);
    }
}
