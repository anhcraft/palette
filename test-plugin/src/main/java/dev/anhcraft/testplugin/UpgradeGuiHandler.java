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
        // Visits all slots belonging to the component "item", then marks them as modifiable. Sword is the only item
        // allowed to be put in
        visitComponent("item", slot -> {
            slot.makeModifiable().filter(itemStack -> itemStack.getType().name().endsWith("_SWORD"));
        });

        // Visits all slots belonging to the component "buff", then marks them as modifiable. Lapis is the only item
        // allowed to be put in. The maximum stack size is reduced to 32.
        visitComponent("buff", slot -> {
            slot.makeModifiable().maxStackSize(32).filter(itemStack -> itemStack.getType() == Material.LAPIS_LAZULI);
        });

        // Listens to click event on all slots belonging to component "executor"
        listen("executor", (ClickEvent) (clickEvent, player1, slot) -> {
            ItemStack item = collectPresentItem("item");
            if (item == null) {
                clickEvent.getWhoClicked().sendMessage(ChatColor.RED + "No item.");
                return;
            }

            // Collect the first present item from component "buff"
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

            // Reset the component "buff"
            resetBulk("buff");

            // Update the GUI
            refreshView();
        });

        // Initial update
        refreshView();
    }

    public void refreshView() {
        ItemStack buff = collectPresentItem("buff");
        double chance = Math.min(1.0, 0.5 + (buff == null ? 0 : 0.05 * buff.getAmount()));
        // Replace the placeholders
        replaceItem("executor", (slot, itemBuilder) -> {
            return itemBuilder.replaceDisplay(s -> s.replace("{chance}", String.format("%.2f", chance)));
        });
    }
}
