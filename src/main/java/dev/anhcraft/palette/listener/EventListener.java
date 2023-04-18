package dev.anhcraft.palette.listener;

import dev.anhcraft.palette.GuiHandler;
import dev.anhcraft.palette.Refreshable;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.palette.util.Pair;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {
    private final Plugin plugin;

    public EventListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handle(final InventoryClickEvent event) {
        final Inventory inv = event.getClickedInventory();
        if (inv == null) return;
        final HumanEntity who = event.getWhoClicked();

        // When clicking an item:
        if (inv.getHolder() instanceof GuiHandler) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            GuiHandler gh = (GuiHandler) inv.getHolder();
            String type = gh.getComponentType(event.getSlot());

            // When interacting with a modifiable component:
            if (gh.getModifiableComponents().contains(type)) {
                ItemStack cursor = event.getCursor();
                ItemStack target = event.getCurrentItem();
                boolean dirty = false;

                // When taking out an item:
                if (ItemUtil.isEmpty(cursor)) {
                    ItemStack bg = gh.getBackupItem(event.getSlot());
                    if (ItemUtil.isPresent(target) && !target.isSimilar(bg)) {
                        ((Player) who).playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
                        event.setCurrentItem(bg);
                        if (event.isShiftClick()) {
                            ItemUtil.addToInventory(who, target);
                        } else {
                            who.setItemOnCursor(target);
                        }
                        dirty = true;
                    }
                }

                // When putting an item:
                else if (gh.canPut(type, cursor)) {
                    ((Player) who).playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                    ItemStack bg = gh.getBackupItem(event.getSlot());
                    if (ItemUtil.isEmpty(target) || target.isSimilar(bg)) {
                        Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, 1);
                        event.setCurrentItem(p.getFirst());
                        who.setItemOnCursor(p.getSecond());
                        dirty = true;
                    } else {
                        Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, 1);
                        event.setCurrentItem(p.getFirst());
                        who.setItemOnCursor(target);
                        ItemUtil.addToInventory(who, p.getSecond());
                        dirty = true;
                    }
                } else {
                    ((Player) who).playSound(who.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
                }

                if (dirty && event.getInventory().getHolder() instanceof Refreshable) {
                    new BukkitRunnable() {

                        public void run() {
                            ((Refreshable) event.getInventory().getHolder()).refreshView(who);
                        }
                    }.runTask(plugin);
                }
            }

            else {
                gh.onClick(event, type);
            }
        }

        // When shift-click an item to quickly place:
        else if (
                event.getClick().isShiftClick() &&
                event.getView().getTopInventory().getHolder() instanceof GuiHandler &&
                ItemUtil.isPresent(event.getCurrentItem()) &&
                event.getView().convertSlot(event.getRawSlot()) != event.getRawSlot()
        ) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            final GuiHandler gh = (GuiHandler) event.getView().getTopInventory().getHolder();
            final int slot = gh.findPlaceableSlot(event.getCurrentItem());
            if (slot != -1) {
                final Pair<ItemStack, ItemStack> item = ItemUtil.splitItem(event.getCurrentItem(), 1);
                new BukkitRunnable() {
                    public void run() {
                        inv.setItem(event.getSlot(), item.getSecond());
                        gh.getInventory().setItem(slot, item.getFirst());
                        ((Player) who).playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                        if (gh.getInventory().getHolder() instanceof Refreshable) {
                            ((Refreshable) gh.getInventory().getHolder()).refreshView(who);
                        }
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler
    public void handle(final InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHandler) {
            if (event.getInventory().getHolder() instanceof Refreshable) {
                new BukkitRunnable() {

                    public void run() {
                        ((Refreshable) event.getInventory().getHolder()).refreshView(event.getWhoClicked());
                    }
                }.runTask(plugin);
            }
            if (event.getRawSlots().stream().anyMatch(r -> event.getView().convertSlot(r) == r)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void handle(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHandler) {
            GuiHandler gh = (GuiHandler) event.getInventory().getHolder();
            gh.onClose(event.getPlayer());
        }
    }
}
