# Palette

[![](https://jitpack.io/v/anhcraft/palette.svg)](https://jitpack.io/#anhcraft/palette)

Lightweight GUI library for Bukkit plugins.

## Feature
Palette allows creating custom GUIs with following features:
- Customization using configuration
- Quick place/take support
- Custom background
- Custom stack size
- Playing sounds on opening and clicking
- Multiple components
- Programmatically update the GUI

However, Palette is not mean to:
- Create complex, animated GUIs
- Replace existing menu plugins

## Dependency
Palette **requires** [Config](https://github.com/anhcraft/config) library to assist in serializing and deserializing configuration.

## Add Palette as shadow dependency

Example Maven configuration:
```xml
<repository>
   <id>jitpack.io</id>
   <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
   <groupId>com.github.anhcraft</groupId>
   <artifactId>palette</artifactId>
   <version>v1.0.2</version>
   <scope>compile</scope>
</dependency>
<dependency>
   <groupId>com.github.anhcraft.config</groupId>
   <artifactId>config.bukkit</artifactId>
   <version>v1.1.5</version>
   <scope>compile</scope>
</dependency>
```

Also, relocate Palette as well to prevent any conflicts. Example Maven configuration in pom.xml:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>VERSION</version>
  <executions>
    <execution>
      <phase>package</phase>
        <goals>
          <goal>shade</goal>
        </goals>
        <configuration>
          <relocations>
             <relocation>
                <pattern>dev.anhcraft.palette</pattern>
                <shadedPattern>path.to.your.plugin.palette</shadedPattern>
             </relocation>
             <relocation>
                <pattern>dev.anhcraft.config</pattern>
                <shadedPattern>path.to.your.plugin.config</shadedPattern>
             </relocation>
          </relocations>
        </configuration>
      </execution>
    </executions>
  </plugin>
```

## Getting Started
With Palette, every GUI must have its own configuration file.

Take a quick example from the test plugin: Assume we want to create a simple item upgrade GUI
- First component is the input/output - must be a sword
- Second component is the buff item to increase the chance - must be lapis lazuli
- Third component is a button to execute

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
- Original layer: The layer contains all original items defined in the configuration
- Baked item: To improve the performance, Palette creates a cached version of the original item (equivalent to the baked layer), that version is said to be baked
- Actual item: Is any item that is existing in the current GUI (which may be different from the configuration). If the actual item is background, it is treated as empty item.
- Modifiable component: A modifiable component means it can be changed either by placing item or taking out item. With Palette, you can define the component to be placing-only or taking-out-only

## Register event listener
As your plugin is going to bundle Palette (shadow dependency), ensure the event listener is registered
```java
getServer().getPluginManager().registerEvents(new GuiEventListener(plugin), plugin);
```
(GuiEventListener is a class from Palette)

## Create GUI Handler
A GUI handler is one that controls a GUI instance / view.
Have a look at the test plugin: [UpgradeGuiHandler](https://github.com/anhcraft/palette/blob/main/test-plugin/src/main/java/dev/anhcraft/testplugin/UpgradeGuiHandler.java)

## Load configuration
Load a resource (inside .jar):
```java
try {
   Gui upgradeGui = new BukkitConfigDeserializer(BukkitConfigProvider.YAML).transformConfig(
        Objects.requireNonNull(SchemaScanner.scanConfig(Gui.class)),
        new YamlConfigSection(YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(TestPlugin.class.getResourceAsStream("/gui.yml")))))
   );
} catch (Exception e) {
   throw new RuntimeException(e);
}
```

Load from a file:
```java
try {
   Gui upgradeGui = new BukkitConfigDeserializer(BukkitConfigProvider.YAML).transformConfig(
        Objects.requireNonNull(SchemaScanner.scanConfig(Gui.class)),
        new YamlConfigSection(YamlConfiguration.loadConfiguration(new File("gui.yml")))
   );
} catch (Exception e) {
   throw new RuntimeException(e);
}
```

## Open the GUI!
```java
upgradeGui.open(player1, new UpgradeGuiHandler());
upgradeGui.open(player2, new UpgradeGuiHandler()); // etc...
```
