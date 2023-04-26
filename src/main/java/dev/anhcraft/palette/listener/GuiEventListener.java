package dev.anhcraft.palette.listener;

import dev.anhcraft.palette.event.*;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.ui.ItemPlaceResult;
import dev.anhcraft.palette.ui.element.Modifiability;
import dev.anhcraft.palette.ui.element.Slot;
import dev.anhcraft.palette.util.ItemUtil;
import dev.anhcraft.palette.util.Pair;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GuiEventListener implements Listener {

    @EventHandler
    public void handle(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null || !(event.getWhoClicked() instanceof Player)) return;
        Player who = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slotIndex = event.getRawSlot();

        // When clicking an item:
        if (inv.getHolder() instanceof GuiHandler) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);
            GuiHandler gh = (GuiHandler) inv.getHolder();
            Slot slot = gh.getSlot(slotIndex);
            Modifiability modifiability = slot.getModifiability();

            // When interacting with an unmodifiable component:
            if (modifiability == null) {
                slot.emitEvent(ClickEvent.class, e -> e.onClick(event, who, slotIndex));
                return;
            }

            ItemStack cursor = event.getCursor();

            // When taking out an item:
            if (ItemUtil.isEmpty(cursor)) {
                // If middle-clicked while in creative -> copy full stack
                if (event.getClick() == ClickType.MIDDLE &&
                        who.getGameMode() == GameMode.CREATIVE &&
                        ItemUtil.isPresent(gh.getActualItem(slotIndex))) {
                    ItemStack itemOnCursor = Objects.requireNonNull(clickedItem).clone();
                    itemOnCursor.setAmount(clickedItem.getMaxStackSize());
                    who.setItemOnCursor(itemOnCursor);
                    return;
                } else if (ItemUtil.isEmpty(clickedItem)) {
                    return;
                } else if (!modifiability.isAllowTaking()) {
                    slot.emitEvent(TakeBlockedEvent.class, e -> e.onTakeBlocked(who, slotIndex, clickedItem));
                    return;
                }

                ItemStack background = gh.getOriginalItem(slotIndex);
                if (clickedItem.isSimilar(background)) return;

                Action action;

                // shift+click: quick take and move straight to the inventory
                if (event.isShiftClick()) {
                    action = Action.QUICK_TAKE;
                    if (!slot.emitBoolEvent(PreTakeEvent.class, e -> e.onPreTake(action, who, slotIndex, clickedItem))) return;

                    event.setCurrentItem(background);
                    ItemUtil.addToInventory(who, clickedItem);
                } else {
                    ItemStack newCurrent = background;
                    ItemStack newCursor = clickedItem;

                    // If right-clicked and has more than 1 -> divide by 2
                    if (event.isRightClick() && clickedItem.getAmount() > 1) {
                        action = Action.HALF_TAKE;
                        if (!slot.emitBoolEvent(PreTakeEvent.class, e -> e.onPreTake(action, who, slotIndex, clickedItem))) return;

                        Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(clickedItem, Math.floorDiv(clickedItem.getAmount(), 2));
                        newCurrent = p.getFirst();
                        newCursor = p.getSecond();
                    } else {
                        action = Action.TAKE;
                        if (!slot.emitBoolEvent(PreTakeEvent.class, e -> e.onPreTake(action, who, slotIndex, clickedItem))) return;
                    }

                    event.setCurrentItem(newCurrent);
                    who.setItemOnCursor(newCursor);
                }

                slot.emitEvent(PostTakeEvent.class, e -> e.onPostTake(action, who, slotIndex, clickedItem));
            }

            // When putting an item:
            else if (modifiability.isAllowPlacing() && modifiability.canPlace(cursor)) {
                ItemStack newCurrent;
                ItemStack newCursor = cursor;

                Action action;

                // If the target is "empty":
                if (ItemUtil.isEmpty(clickedItem) || clickedItem.isSimilar(gh.getOriginalItem(slotIndex))) {
                    action = Action.PLACE;
                    if (!slot.emitBoolEvent(PrePlaceEvent.class, e -> e.onPrePlace(action, who, slotIndex, cursor))) return;

                    Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, event.isRightClick() ? 1 : modifiability.getMaxStackSize());
                    newCurrent = p.getFirst();
                    newCursor = p.getSecond();
                } else { // Otherwise:

                    // If the target is the same type as the cursor and not filled yet
                    if (clickedItem.isSimilar(cursor) && clickedItem.getAmount() < modifiability.getMaxStackSize()) {
                        // If right-clicked, merge by 1
                        if (event.isRightClick()) {
                            action = Action.MERGE_ONE;
                            if (!slot.emitBoolEvent(PrePlaceEvent.class, e -> e.onPrePlace(action, who, slotIndex, cursor))) return;

                            Pair<ItemStack, ItemStack> p = ItemUtil.mergeItem(clickedItem, 1, modifiability.getMaxStackSize());
                            newCurrent = p.getFirst();
                            if (p.getSecond() == ItemUtil.EMPTY_ITEM) {
                                ItemStack itemOnCursor = cursor.clone();
                                itemOnCursor.setAmount(itemOnCursor.getAmount() - 1);
                                newCursor = itemOnCursor;
                            }
                        } else { // Otherwise, merge by all
                            action = Action.MERGE_WHOLE;
                            if (!slot.emitBoolEvent(PrePlaceEvent.class, e -> e.onPrePlace(action, who, slotIndex, cursor))) return;

                            Pair<ItemStack, ItemStack> p = ItemUtil.mergeItem(clickedItem, cursor.getAmount(), modifiability.getMaxStackSize());
                            newCurrent = p.getFirst();
                            newCursor = p.getSecond();
                        }
                    } else {
                        action = Action.REPLACE;
                        if (!slot.emitBoolEvent(PrePlaceEvent.class, e -> e.onPrePlace(action, who, slotIndex, cursor))) return;

                        Pair<ItemStack, ItemStack> p = ItemUtil.splitItem(cursor, modifiability.getMaxStackSize());
                        newCurrent = p.getFirst();
                        newCursor = clickedItem;
                        ItemUtil.addToInventory(who, p.getSecond());
                    }
                }

                event.setCurrentItem(newCurrent);
                who.setItemOnCursor(newCursor);
                slot.emitEvent(PostPlaceEvent.class, e -> e.onPostPlace(action, who, slotIndex, cursor));
            } else {
                slot.emitEvent(PlaceBlockedEvent.class, e -> e.onPlaceBlocked(who, slotIndex, cursor));
            }
        }

        // When shift-click an item to quickly place:
        else if (
                event.getClick().isShiftClick() &&
                event.getView().getTopInventory().getHolder() instanceof GuiHandler &&
                ItemUtil.isPresent(clickedItem) &&
                event.getView().convertSlot(event.getSlot()) != slotIndex
        ) {
            event.setCancelled(true);
            event.setResult(org.bukkit.event.Event.Result.DENY);

            GuiHandler gh = (GuiHandler) event.getView().getTopInventory().getHolder();
            ItemPlaceResult result = gh.placeItem(clickedItem, null, true);
            if (result.getSlot() != -1) {
                boolean ok = gh.getSlot(result.getSlot()).emitBoolEvent(PrePlaceEvent.class, e -> e.onPrePlace(Action.QUICK_PLACE, who, slotIndex, clickedItem));
                if (ok) {
                    ItemStack target = clickedItem.clone();
                    target.setAmount(result.getTargetNewAmount());
                    gh.getInventory().setItem(result.getSlot(), target);

                    if (result.getRemainder() > 0) {
                        ItemStack curr = clickedItem.clone();
                        curr.setAmount(result.getRemainder());
                        event.setCurrentItem(curr);
                    } else {
                        event.setCurrentItem(ItemUtil.EMPTY_ITEM);
                    }

                    gh.getSlot(result.getSlot()).emitEvent(PostPlaceEvent.class, e -> e.onPostPlace(Action.QUICK_PLACE, who, slotIndex, clickedItem));
                }
            }
        }
    }

    @EventHandler
    public void handle(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHandler) {
            if (event.getRawSlots().stream().anyMatch(r -> event.getView().convertSlot(r) == r)) {
                event.setCancelled(true);
                event.setResult(org.bukkit.event.Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void handle(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHandler) {
            GuiHandler gh = (GuiHandler) event.getInventory().getHolder();
            gh.onClose((Player) event.getPlayer());
        }
    }
}
