package dev.anhcraft.testplugin;

import dev.anhcraft.palette.GuiHandler;
import dev.anhcraft.palette.Refreshable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class UpgradeGuiHandler extends GuiHandler implements Refreshable {
    public UpgradeGuiHandler() {
        createModifiableComponent("item");
        createModifiableComponent("buff").maxStackSize(32);
    }

    @Override
    public void onRendered(@NotNull HumanEntity humanEntity) {
        refreshView(humanEntity);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull String component) {
        if (component.equals("executor")) {
            ItemStack item = collectPresentItem("item");
            if (item == null) {
                clickEvent.getWhoClicked().sendMessage(ChatColor.RED + "No item.");
                return;
            }

            ItemStack buff = collectPresentItem("buff");
            double chance = Math.min(1.0, 0.5 + (buff == null ? 0 : 0.05 * buff.getAmount()));

            if (ThreadLocalRandom.current().nextDouble() < chance) {
                item.addEnchantment(Enchantment.DAMAGE_ALL, 3);
                item.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                setItemOnce("item", item);
                clickEvent.getWhoClicked().sendMessage(ChatColor.GREEN + "Upgrade successful.");
            } else {
                clickEvent.getWhoClicked().sendMessage(ChatColor.RED + "Upgrade failed.");
            }

            resetItems("buff");
            refreshView(clickEvent.getWhoClicked());
        }
    }

    @Override
    public boolean canPut(@NotNull String component, @NotNull ItemStack item) {
        if (component.equals("item")) {
            return item.getType().name().endsWith("_SWORD");
        } else if (component.equals("buff")) {
            return item.getType() == Material.LAPIS_LAZULI;
        }
        return super.canPut(component, item);
    }

    @Override
    public void refreshView(@NotNull HumanEntity humanEntity) {
        ItemStack buff = collectPresentItem("buff");
        double chance = Math.min(1.0, 0.5 + (buff == null ? 0 : 0.05 * buff.getAmount()));
        replaceItems("executor", itemBuilder -> itemBuilder.replaceDisplay(s -> s.replace("{chance}", String.format("%.2f", chance))));
    }
}
