package dev.anhcraft.palette.listener;

import dev.anhcraft.palette.GuiHandler;
import dev.anhcraft.palette.ModifiableComponent;
import dev.anhcraft.palette.Refreshable;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.palette.util.Pair;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
    public void handle(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || !(event.getWhoClicked() instanceof Player)) return;
        Player who = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // When clicking an item:
        if (inv.getHolder() instanceof GuiHandler) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            GuiHandler gh = (GuiHandler) inv.getHolder();
            String type = gh.getComponentType(event.getSlot());

            // When interacting with a modifiable component:
            ModifiableComponent mc = gh.getModifiableComponents().get(type);
            if (mc != null) {
                ItemStack cursor = event.getCursor();
                boolean dirty = false;

                // When taking out an item:et
                if (ItemUtil.isEmpty(cursor)) {
                    // If middle-clicked while in creative -> copy full stack
                    if (event.getClick() == ClickType.MIDDLE &&
                            who.getGameMode() == GameMode.CREATIVE &&
                            ItemUtil.isPresent(clickedItem) &&
                            !clickedItem.isSimilar(gh.getBackupItem(event.getSlot()))) {
                        ItemStack itemOnCursor = clickedItem.clone();
                        itemOnCursor.setAmount(clickedItem.getMaxStackSize());
                        who.setItemOnCursor(itemOnCursor);
                    }
                    else if (mc.isAllowTaking()) {
                        ItemStack bg = gh.getBackupItem(event.getSlot());
                        // Only if the target is "empty":
                        if (ItemUtil.isPresent(clickedItem) && !clickedItem.isSimilar(bg)) {
                            if (event.isShiftClick()) {
                                event.setCurrentItem(bg);
                                ItemUtil.addToInventory(who, clickedItem);
                            } else {
                                // If right-clicked and has more than 1 -> divide by 2
                                if (event.isRightClick() && clickedItem.getAmount() > 1) {
                                    Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(clickedItem, Math.floorDiv(clickedItem.getAmount(), 2));
                                    event.setCurrentItem(p.getFirst());
                                    who.setItemOnCursor(p.getSecond());
                                }
                                // Otherwise, take the whole item
                                else {
                                    event.setCurrentItem(bg);
                                    who.setItemOnCursor(clickedItem);
                                }
                            }
                            dirty = true;
                            who.playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
                        }
                    } else {
                        who.playSound(who.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
                    }
                }

                // When putting an item:
                else if (mc.isAllowPlacing() && gh.canPut(type, cursor)) {
                    ItemStack bg = gh.getBackupItem(event.getSlot());

                    // If the target is "empty":
                    if (ItemUtil.isEmpty(clickedItem) || clickedItem.isSimilar(bg)) {
                        Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, event.isRightClick() ? 1 : mc.getMaxStackSize());
                        event.setCurrentItem(p.getFirst());
                        who.setItemOnCursor(p.getSecond());
                    }

                    // If the target is not "empty":
                    else {
                        // If the target is the same type as the cursor and not filled yet
                        if (clickedItem.isSimilar(cursor) && clickedItem.getAmount() < mc.getMaxStackSize()) {
                            // If right-clicked, merge by 1
                            if (event.isRightClick()) {
                                Pair<ItemStack, ItemStack> p = ItemUtil.mergeItem(clickedItem, 1, mc.getMaxStackSize());
                                event.setCurrentItem(p.getFirst());
                                if (p.getSecond() == ItemUtil.EMPTY_ITEM) {
                                    ItemStack itemOnCursor = who.getItemOnCursor().clone();
                                    itemOnCursor.setAmount(itemOnCursor.getAmount() - 1);
                                    who.setItemOnCursor(itemOnCursor);
                                }
                            }
                            // Otherwise, merge by all
                            else {
                                Pair<ItemStack, ItemStack> p = ItemUtil.mergeItem(clickedItem, cursor.getAmount(), mc.getMaxStackSize());
                                event.setCurrentItem(p.getFirst());
                                who.setItemOnCursor(p.getSecond());
                            }
                        }
                        else {
                            Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, mc.getMaxStackSize());
                            event.setCurrentItem(p.getFirst());
                            who.setItemOnCursor(clickedItem);
                            ItemUtil.addToInventory(who, p.getSecond());
                        }
                    }

                    who.playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                    dirty = true;
                } else {
                    who.playSound(who.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
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
                ItemUtil.isPresent(clickedItem) &&
                event.getView().convertSlot(event.getRawSlot()) != event.getRawSlot()
        ) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            GuiHandler gh = (GuiHandler) event.getView().getTopInventory().getHolder();
            ItemStack remain = gh.tryPlace(clickedItem);
            if (!remain.equals(clickedItem)) {
                inv.setItem(event.getSlot(), remain);
                who.playSound(who.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
                new BukkitRunnable() {
                    public void run() {
                        if (gh.getInventory().getHolder() instanceof Refreshable) {
                            ((Refreshable) gh.getInventory().getHolder()).refreshView(who);
                        }
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler
    public void handle(InventoryDragEvent event) {
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
