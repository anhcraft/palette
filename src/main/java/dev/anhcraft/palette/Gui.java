package dev.anhcraft.palette;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.annotations.Validation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configurable
public class Gui {
    public String title;

    public List<String> layout;

    public Map<Character, ComponentItem> components;

    @Validation(notNull = true, silent = true)
    public Sound openSound = Sound.UI_TOAST_IN;

    @PostHandler
    private void postHandle() {
        layout = layout.stream().map(String::trim).collect(Collectors.toList());
        int max = layout.stream().mapToInt(String::length).max().orElse(0);
        for (String s : layout) {
            if (s.length() >= max) continue;
            throw new RuntimeException("Invalid layout detected! Max = " + max);
        }
    }

    /**
     * Opens a GUI for the given entity.
     * @param humanEntity The entity to open the GUI for
     * @param guiHandler The GUI handler
     */
    public void open(@NotNull HumanEntity humanEntity, @NotNull GuiHandler guiHandler) {
        InventoryHolder h = humanEntity.getOpenInventory().getTopInventory().getHolder();
        if (h instanceof GuiHandler) {
            ((GuiHandler) h).onClose(humanEntity);
        }

        Inventory inv = Bukkit.createInventory(guiHandler, layout.size() * 9, ChatColor.translateAlternateColorCodes('&', title));
        guiHandler.setInventory(inv);

        ComponentItem[] backupLayer = new ComponentItem[inv.getSize()];

        for (int y = 0; y < layout.size(); ++y) {
            String s = layout.get(y);
            for (int x = 0; x < s.length(); ++x) {
                int i = y * 9 + x;
                ComponentItem c = components.get(s.charAt(x));
                if (c == null) {
                    throw new RuntimeException("Component not found: " + s.charAt(x));
                }
                backupLayer[i] = c;
            }
        }

        guiHandler.setBackupLayer(backupLayer);
        guiHandler.renderBackupLayer();
        guiHandler.onPreOpen(humanEntity);
        humanEntity.openInventory(inv);
        ((Player) humanEntity).playSound(humanEntity.getLocation(), openSound, 1.0f, 1.0f);
    }
}
