package dev.anhcraft.palette.ui.element;

import dev.anhcraft.palette.event.*;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Slot {
    private final Component component;
    private List<Event> events = Collections.synchronizedList(new ArrayList<>());
    private Modifiability modifiability;

    public Slot(@NotNull Component component) {
        this.component = component;

        listen(new PostPlaceEvent() {
            @Override
            public void onPostPlace(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
            }
        });

        listen(new PostTakeEvent() {
            @Override
            public void onPostTake(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0f, 1.0f);
            }
        });

        listen(new PlaceBlockedEvent() {
            @Override
            public void onPlaceBlocked(@NotNull Player player, int slot, @NotNull ItemStack item) {
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
            }
        });

        listen(new TakeBlockedEvent() {
            @Override
            public void onTakeBlocked(@NotNull Player player, int slot, @NotNull ItemStack item) {
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.0f);
            }
        });
    }

    @NotNull
    public Component getComponent() {
        return component;
    }

    @NotNull
    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void setEvents(@NotNull List<Event> events) {
        this.events = Collections.synchronizedList(events);
    }

    public void setEvents(@NotNull Event... events) {
        this.events = Collections.synchronizedList(Arrays.asList(events));
    }

    public void listen(@NotNull Event event) {
        events.add(event);
    }

    @Nullable
    public Modifiability getModifiability() {
        return modifiability;
    }

    public void setModifiability(@Nullable Modifiability modifiability) {
        this.modifiability = modifiability;
    }

    @NotNull
    public Modifiability makeModifiable() {
        Modifiability modifiability = new Modifiability();
        setModifiability(modifiability);
        return modifiability;
    }

    public <T extends Event> void emitEvent(Class<T> clazz, Consumer<T> consumer) {
        for (Event event : events) {
            if (clazz.isInstance(event)) {
                //noinspection unchecked
                consumer.accept((T) event);
            }
        }
    }

    public <T extends Event> boolean emitBoolEvent(Class<T> clazz, Function<T, Boolean> consumer) {
        for (Event event : events) {
            if (clazz.isInstance(event)) {
                //noinspection unchecked
                if (!consumer.apply((T) event)) {
                    return false;
                }
            }
        }
        return true;
    }
}
