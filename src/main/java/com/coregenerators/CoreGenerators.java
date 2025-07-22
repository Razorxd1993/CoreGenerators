// Hinweis: Der alte Code wird ersetzt, da der neue eine deutlich erweiterte Struktur hat.

// Wir entfernen die alte minimalistische Version und fügen eine erweiterte Pluginstruktur schrittweise ein.
// Beginnen wir mit der neuen Plugin-Hauptklasse.

package com.coregenerators;

import com.coregenerators.util.GeneratorStorage;
import dev.lone.itemsadder.api.CustomBlock;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public final class CoreGenerators extends JavaPlugin implements Listener {

    public static CoreGenerators instance;
    public static Economy economy;

    // Generator-Datenbank
    public static final Map<String, Generator> generators = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().log(Level.SEVERE, "Vault wurde nicht gefunden oder Economy nicht initialisiert!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Events registrieren
        Bukkit.getPluginManager().registerEvents(new GeneratorListener(), this);

        // Konfigs laden
        saveDefaultConfig();
        createConfig("generators.yml");
        createConfig("upgrades.yml");
        createConfig("messages.yml");
        createConfig("gui.yml");

        loadGenerators();

        // Storage laden
        GeneratorStorage.load();

        // Command registrieren
        getCommand("coregen").setExecutor(new com.coregenerators.commands.CoreCommand());

        getLogger().info("CoreGenerators erfolgreich geladen!");

        // Test-Datei zum Debuggen
        File testFile = new File(getDataFolder(), "debug.txt");
        try {
            getDataFolder().mkdirs();
            testFile.createNewFile();
            getLogger().info("Testdatei wurde erstellt.");
        } catch (IOException e) {
            getLogger().severe("Konnte Testdatei nicht erstellen: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        // Storage speichern
        GeneratorStorage.save();

        getLogger().info("CoreGenerators deaktiviert.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private void createConfig(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    public static FileConfiguration getConfigFile(String name) {
        File file = new File(instance.getDataFolder(), name);
        return YamlConfiguration.loadConfiguration(file);
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static CoreGenerators getInstance() {
        return instance;
    }

    private void loadGenerators() {
        FileConfiguration generatorConfig = getConfigFile("generators.yml");
        ConfigurationSection generatorSection = generatorConfig.getConfigurationSection("generators");

        if (generatorSection == null) return;

        for (String id : generatorSection.getKeys(false)) {
            ConfigurationSection section = generatorSection.getConfigurationSection(id);

            String itemsAdderId = section.getString("itemsadder");
            CustomBlock customBlock = null;
            if (itemsAdderId != null && CustomBlock.isInRegistry(itemsAdderId)) {
                customBlock = CustomBlock.getInstance(itemsAdderId);
            } else {
                getLogger().warning("CustomBlock '" + itemsAdderId + "' nicht gefunden oder nicht registriert! Fallback zu STONE.");
            }

            Material fallbackMaterial = Material.STONE;

            Material fuel = Material.valueOf(section.getString("fuel").toUpperCase());
            int interval = section.getInt("interval");
            int customData = section.getInt("customdata", 0);

            List<Map<?, ?>> dropList = section.getMapList("drop");
            List<GeneratorDrop> drops = new ArrayList<>();

            for (Map<?, ?> dropMap : dropList) {
                if (dropMap.get("material") instanceof String materialStr &&
                        dropMap.get("amount") instanceof Integer amount &&
                        dropMap.get("chance") instanceof Number chanceNum) {

                    try {
                        Material material = Material.valueOf(materialStr.toUpperCase());
                        double chance = chanceNum.doubleValue();
                        drops.add(new GeneratorDrop(material, amount, chance));
                    } catch (IllegalArgumentException ex) {
                        getLogger().warning("Ungültiges Material '" + materialStr + "' bei Generator '" + id + "'.");
                    }

                } else {
                    getLogger().warning("Fehlerhafte Drop-Angaben bei Generator '" + id + "'.");
                }

            }

            Generator generator = new Generator(id, customBlock, fallbackMaterial, interval, customData, drops);
            generators.put(id, generator);
        }

        getLogger().info("Es wurden " + generators.size() + " Generator(en) geladen.");
    }
}
