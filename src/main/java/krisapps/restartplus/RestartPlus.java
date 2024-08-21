package krisapps.restartplus;

import krisapps.restartplus.commands.Language;
import krisapps.restartplus.commands.ScheduleActionCommand;
import krisapps.restartplus.commands.tab.LanguageTab;
import krisapps.restartplus.commands.tab.ScheduleActionTab;
import krisapps.restartplus.events.listeners.OnPlayerJoin;
import krisapps.restartplus.util.FormatUtility;
import krisapps.restartplus.util.ScheduleManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.krisapps.PluginEssentials.LocalizationUtility;
import org.krisapps.PluginEssentials.LoggingUtility;
import org.krisapps.PluginEssentials.MessageUtility;
import org.krisapps.PluginEssentials.PluginEssentials;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class RestartPlus extends JavaPlugin {

    public FileConfiguration pluginConfig;
    public File configFile = new File(getDataFolder(), "config.yml");
    public FileConfiguration pluginData;
    public File dataFile = new File(getDataFolder(), "data.yml");

    private final PluginEssentials essentials = new PluginEssentials(this,  "en-US", new String[] {"en-US", "ru-RU"});
    public FormatUtility formatUtility = new FormatUtility(this);
    public MessageUtility messageUtility = essentials.messages;
    public LocalizationUtility localizationUtility = essentials.localization;
    public LoggingUtility logging = essentials.logging;

    public static RestartPlus instance;

    @Override
    public void onEnable() {
        instance = this;

        registerCommands();
        registerEvents();
        loadFiles();
        localizationUtility.initialize();

        ScheduleManager.initialize();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadFiles(){
        if (!configFile.getParentFile().exists() || !configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", true);
        }

        if (!dataFile.getParentFile().exists() || !dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        pluginConfig = new YamlConfiguration();
        pluginData = new YamlConfiguration();

        try {
            pluginConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Failed to load the config file: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            pluginData.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load the data file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerCommands(){
        getCommand("scheduleaction").setExecutor(new ScheduleActionCommand());
        getCommand("scheduleaction").setTabCompleter(new ScheduleActionTab());

        getCommand("lang").setExecutor(new Language());
        getCommand("lang").setTabCompleter(new LanguageTab());
    }

    private void registerEvents(){
        // Events here
        Bukkit.getPluginManager().registerEvents(new OnPlayerJoin(), this);
    }

    public boolean resetDefaultLanguageFile() {
        try {
            localizationUtility.resetDefaultLanguageFile();
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public void reloadCurrentLanguageFile() {
        localizationUtility.loadCurrentLanguage();
    }

    public void saveConfig(){
        try {
            pluginConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("An error occurred while trying to save the configuration file.\nReason: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            pluginData.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("An error occurred while trying to save the data file.\nReason: " + e.getMessage());
        }
    }
}
