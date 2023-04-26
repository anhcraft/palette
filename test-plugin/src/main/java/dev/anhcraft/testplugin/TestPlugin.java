package dev.anhcraft.testplugin;

import dev.anhcraft.config.bukkit.BukkitConfigProvider;
import dev.anhcraft.config.bukkit.struct.YamlConfigSection;
import dev.anhcraft.config.schema.SchemaScanner;
import dev.anhcraft.palette.ui.Gui;
import dev.anhcraft.palette.listener.GuiEventListener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.util.Objects;

public final class TestPlugin extends JavaPlugin {
    private Gui upgradeGui;

    @Override
    public void onEnable() {
        try {
            upgradeGui = BukkitConfigProvider.YAML.createDeserializer().transformConfig(
                    Objects.requireNonNull(SchemaScanner.scanConfig(Gui.class)),
                    new YamlConfigSection(YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(TestPlugin.class.getResourceAsStream("/gui.yml")))))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new GuiEventListener(), this);

        Objects.requireNonNull(getCommand("upgrade")).setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player && sender.hasPermission("testplugin.upgrade")) {
                upgradeGui.open((Player) sender, new UpgradeGuiHandler());
                return true;
            }
            return false;
        });
    }
}
