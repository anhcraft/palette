# Palette

[![](https://jitpack.io/v/anhcraft/palette.svg)](https://jitpack.io/#anhcraft/palette)

Lightweight GUI library for Bukkit plugins.

## Feature
Palette allows creating custom GUIs with following features:
- Customizable using configuration
- Quick place/take support
- Custom background
- Custom stack size
- Sounds on opening and clicking
- Multiple components
- Programmatically update the GUI

However, Palette does not mean to:
- Create complex, animated GUIs
- Replace existing menu plugins

## Dependency
Palette contains [Config](https://github.com/anhcraft/config) library to assist in serializing and deserializing configuration.

## Getting Started
First, every GUI must have its own configuration file:

Take a quick example from the test plugin: Assume we want to create a simple item upgrade GUI
- First slot is the input/output - must be a sword
- Second slot is the buff item to increase the chance - must be lapis lazuli
- Third slot is a button to execute

```yaml
title: "&e&lSword Upgrade GUI"
layout:
  - +++++++++
  - ++I+B+E++
  - +++++++++
components:
  "+":
    material: BLACK_STAINED_GLASS_PANE
    name: "&a"
  "I":
    type: item
    material: LIME_STAINED_GLASS_PANE
    name: "&aItem"
    lore:
      - "&7» Put the sword to be upgraded"
  "B":
    type: buff
    material: YELLOW_STAINED_GLASS_PANE
    name: "&aBuff"
    lore:
      - "&7» Put the bonus item (Lapis) here"
  "E":
    type: executor
    material: ANVIL
    name: "&bUpgrade"
    lore:
      - "&7» Click to upgrade the item"
      - "&7» Chance: &a{chance}%"
```

Palette works with GUI using pattern - similar to how recipes work. The user defines the layout in which each letter representing a component. Then we configure the item for each of them.

Components are distinct using "type". If "type" is not specified, it defaults to "unknown" component.

This is the full configuration for an item stack:
```yaml
material: grass_block
amount: 1
name: "Hello"
lore:
  - ">> Custom grass block"
enchant:
  sharpness: 3 # Allows Minecraft enchantment names
  damage_all: 3 # Allows Bukkit enchantment names
flag:
  - hide_potion_effects
unbreakable: true
```

## Terminology

- Background item: Since Palette allows customizable background item, we say that an item is background if it is either empty or be the background defined in the configuration
- Backup layer: The backup layer contains all original items defined in the configuration
- Backed item: To improve the performance, Palette creates a cached version of the original item (equivalent to the backup layer), that version is said to be baked
- Actual item: Is any item that is existing in the current GUI which may be different from the configuration
- Modifiable component: A modifiable component means it can be changed either by placing item or taking out item. With Palette, you can define the component to be placing-only or taking-out-only
- Refreshable: Refreshable GUI requires the GUi to be refreshed / updated for every successful interaction. This is the essential interface to create dynamic GUIs with Palette.

## How does it work?
On initialization:
1. GUI configuration is loaded
2. Colorize item name and lore
3. Create baked items for the backup layer
<br>

Whenever a GUI instance is created:
1. Create a `Inventory` with items from the backup layer
2. Fire `onRendered` event. During this time, the plugin can replace placeholders in name and lore.
3. Show the inventory
<br>

Whenever the player interacts with GUI (place item, take out item, drag item, etc)
1. Palette automatically handles common interactions on **modifiable components** such as: cloning a stack in creative mode, splitting item, merging item, etc
- In this case, Palette will refresh the GUI if it is refreshable
2. If the component is unmodifiable, it fallbacks the responsibility to the plugin by firing `onClick` event
- Palette won't refresh the GUI no matter if it is refreshable
<br>

Whenever the GUI is closed:
1. Palette tries to move items in modifiable components to the main inventory. If it is full, items will be dropped.
2. Fire `onClose` event

## Create GUI Handler
A GUI handler is one that controls a GUI instance / view.

Take a look at the test plugin:
```java
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
```

Let's explain how the code above works!
First, we define modifiable components, there are two: "item" and "buff". With "buff", we configure a custom maximum stack size of 32 (normally, it should be 64).<br>
**Note: If we don't define the maximum stack size, it defaults to 64**

There are four methods:
- `onRendered`: call after the GUI is rendered and to be displayed to player
- `onClick`: when the player clicks on a slot (and Palette could not handle the case)
- `canPut`: check if the item can be put to a specific component
- `refreshView`: call whenever Palette handles an interaction

`onClose` is not specified since we want to preserve the default behaviour.

With the idea from Getting Started section, we can implement GUI Handler step-by-step:

1. When opened, replace the placeholder to its default value
   - This can be done using `onRendered` event
2. Update the chance automatically
   - This can be done using `refreshView` event
   - So whenever the sword or the buff item is put or taken out, the chance is re-updated
   - #1 and #2 can be combined to a single method as shown in the code
3. Validate items
   - This can be done via `canPut`
4. Listen to upgrade button click
   - This can be done using `onClick` event
   - First, we collect necessary ingredients: the sword and the buff item
   - Then we calculate the chance, randomize and upgrade the item
   - Next, we update ingredient slots
   - Finally, it's important to re-update the chance

## Load configuration
Load from file or resource (inside .jar)
```java
try {
   Gui upgradeGui = BukkitConfigProvider.YAML.createDeserializer().transformConfig(
           Objects.requireNonNull(SchemaScanner.scanConfig(Gui.class)),
           new YamlConfigSection(YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(TestPlugin.class.getResourceAsStream("/gui.yml")))))
   );
} catch (Exception e) {
   throw new RuntimeException(e);
}
```

```java
try {
   Gui upgradeGui = BukkitConfigProvider.YAML.createDeserializer().transformConfig(
           Objects.requireNonNull(SchemaScanner.scanConfig(Gui.class)),
           new YamlConfigSection(YamlConfiguration.loadConfiguration(new File("gui.yml")))
   );
} catch (Exception e) {
   throw new RuntimeException(e);
}
```

## Open the GUI!
```java
upgradeGui.open(player1, UpgradeGuiHandler.class);
upgradeGui.open(player2, UpgradeGuiHandler.class); // etc...
```
