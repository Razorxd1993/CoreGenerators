package com.coregenerators.main;

import com.coregenerators.generatorconfigs.GeneratorGuiListener;
import com.coregenerators.generatorconfigs.GeneratorTickTask;
import com.coregenerators.generatorconfigs.PlacedGenerator;
import com.coregenerators.listener.*;
import com.coregenerators.util.CoreCommand;
import com.coregenerators.util.GeneratorStorage;
import com.coregenerators.util.Messages;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CoreGenerators extends JavaPlugin {

    private static CoreGenerators instance;
    private GeneratorStorage storage;
    private LuckPerms luckPerms;
    private Economy economy;

    public static final Map<String, Generator> generators = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        storage = new GeneratorStorage();

        // LuckPerms initialisieren
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            getLogger().severe("§cLuckPerms wurde nicht gefunden. Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Vault & Economy initialisieren
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        }
        if (economy == null) {
            getLogger().severe("§cVault oder Economy-Plugin wurde nicht gefunden. Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Config-Dateien erstellen
        saveDefaultConfig();
        saveResourceIfNotExists("generators.yml");
        saveResourceIfNotExists("gui.yml");
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("upgrades.yml");

        // Messages laden
        Messages.load();

        // Speicherordner sicherstellen
        File storageFolder = new File(getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        // Generatoren aus ItemsAdder etc. werden im IAListener geladen (nicht hier direkt!)

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new FurniturePlaceListener(), this);
        getServer().getPluginManager().registerEvents(new IAListener(), this);
        getServer().getPluginManager().registerEvents(new GeneratorGuiListener(), this);
        getServer().getPluginManager().registerEvents(new FurnitureRemoveListener(), this);
        // Generatoren laden (jetzt sicher nach Messages & Configs)
        storage.loadAllGenerators();

        // Commands registrieren
        CoreCommand coreCommand = new CoreCommand(this);
        getCommand("coregen").setExecutor(coreCommand);
        getCommand("coregen").setTabCompleter(coreCommand);

        // Ticker starten
        new GeneratorTickTask().runTaskTimer(this, 20L, 20L); // alle 1 Sekunde

        getLogger().info("CoreGenerators erfolgreich geladen!");
    }

    private void saveResourceIfNotExists(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    @Override
    public void onDisable() {
        storage.saveAll();
        getLogger().info("CoreGenerators wurde deaktiviert.");
    }

    public static CoreGenerators getInstance() {
        return instance;
    }

    public GeneratorStorage getStorage() {
        return storage;
    }

    public Map<UUID, List<PlacedGenerator>> getPlayerGenerators() {
        return storage.getPlayerGeneratorMap();
    }

    public Map<Location, PlacedGenerator> getPlacedGenerators() {
        return storage.getPlacedGeneratorMap();
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public static Economy getEconomy() {
        return getInstance().economy;
    }

    public static YamlConfiguration getMessages() {
        return Messages.getConfig();
    }
}
