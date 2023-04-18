package dev.anhcraft.palette.config;

import dev.anhcraft.config.ConfigDeserializer;
import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.utils.ObjectUtil;
import dev.anhcraft.palette.GuiHandler;
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
    public Map<Character, Component> components;

    @PostHandler
    private void postHandle(ConfigDeserializer deserializer) {
        layout = layout.stream().map(String::trim).collect(Collectors.toList());
        int max = layout.stream().mapToInt(String::length).max().orElse(0);
        for (String s : layout) {
            if (s.length() >= max) continue;
            throw new RuntimeException("Invalid layout detected! Max = " + max);
        }
    }

    @NotNull
    public Inventory render(@NotNull HumanEntity humanEntity, @NotNull Class<? extends GuiHandler> guiHandlerClass) throws InstantiationException {
        GuiHandler guiHandler = (GuiHandler) ObjectUtil.newInstance(guiHandlerClass);
        Inventory inv = Bukkit.createInventory(guiHandler, layout.size() * 9, ChatColor.translateAlternateColorCodes('&', title));
        guiHandler.setInventory(inv);
        Component[] backupLayer = new Component[inv.getSize()];
        for (int y = 0; y < layout.size(); ++y) {
            String s = layout.get(y);
            for (int x = 0; x < s.length(); ++x) {
                int i = y * 9 + x;
                Component c = components.get(s.charAt(x));
                if (c == null) {
                    throw new RuntimeException("Component not found: " + s.charAt(x));
                }
                backupLayer[i] = c;
            }
        }
        guiHandler.setBackupLayer(backupLayer);
        guiHandler.renderBackupLayer();
        guiHandler.onRendered(humanEntity);
        return inv;
    }

    public void open(@NotNull HumanEntity humanEntity, @NotNull Class<? extends GuiHandler> guiHandlerClass) {
        InventoryHolder h = humanEntity.getOpenInventory().getTopInventory().getHolder();
        if (h instanceof GuiHandler) {
            ((GuiHandler) h).onClose(humanEntity);
        }
        ((Player) humanEntity).playSound(humanEntity.getLocation(), Sound.UI_TOAST_IN, 1.0f, 1.0f);
        try {
            humanEntity.openInventory(render(humanEntity, guiHandlerClass));
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
