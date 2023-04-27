package dev.anhcraft.testplugin;

import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class UpgradeGuiHandler extends GuiHandler {

    @Override
    public void onPreOpen(@NotNull Player player) {
        visitComponent("item", slot -> {
            slot.makeModifiable().filter(itemStack -> itemStack.getType().name().endsWith("_SWORD"));
        });
        visitComponent("buff", slot -> {
            slot.makeModifiable().maxStackSize(32).filter(itemStack -> itemStack.getType() == Material.LAPIS_LAZULI);
        });

        listen("executor", (ClickEvent) (clickEvent, player1, slot) -> {
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
                setBulk("item", item);
                clickEvent.getWhoClicked().sendMessage(ChatColor.GREEN + "Upgrade successful.");
            } else {
                clickEvent.getWhoClicked().sendMessage(ChatColor.RED + "Upgrade failed.");
            }

            resetBulk("buff");
            refreshView();
        });

        refreshView();
    }

    public void refreshView() {
        ItemStack buff = collectPresentItem("buff");
        double chance = Math.min(1.0, 0.5 + (buff == null ? 0 : 0.05 * buff.getAmount()));
        replaceItem("executor", (slot, itemBuilder) -> {
            return itemBuilder.replaceDisplay(s -> s.replace("{chance}", String.format("%.2f", chance)));
        });
    }
}
