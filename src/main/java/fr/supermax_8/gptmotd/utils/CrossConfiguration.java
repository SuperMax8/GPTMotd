package fr.supermax_8.gptmotd.utils;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class CrossConfiguration {


    public CrossConfiguration() {

    }

    public abstract void set(String s, Object o);

    public abstract Object get(String s);

    public abstract String getString(String s);

    public abstract List<String> getKeys(boolean deep);

    public abstract boolean contains(String path);

    public abstract CrossConfigurationSection getConfigurationSection(String path);
    public abstract boolean isConfigurationSection(String path);

    public abstract String saveToString();

    public abstract void save();

    public abstract void save(File f);

    public static CrossConfiguration loadConfiguration(File f) {
        try {
            return new SpigotCrossConfiguration(f);
        } catch (Error error) {
            return new BungeeCrossConfiguration(f);
        }
    }

    public static CrossConfiguration loadConfiguration(Reader f) {
        try {
            return new SpigotCrossConfiguration(f);
        } catch (Error error) {
            return new BungeeCrossConfiguration(f);
        }
    }

    public static CrossConfiguration newConfig() {
        try {
            return new SpigotCrossConfiguration();
        } catch (Error error) {
            return new BungeeCrossConfiguration();
        }
    }

    private static class SpigotCrossConfiguration extends CrossConfiguration {

        private final FileConfiguration config;
        private final File configFile;

        private SpigotCrossConfiguration() {
            config = new YamlConfiguration();
            configFile = null;
        }

        private SpigotCrossConfiguration(File configFile) {
            config = YamlConfiguration.loadConfiguration(configFile);
            this.configFile = configFile;
        }

        private SpigotCrossConfiguration(Reader configFile) {
            config = YamlConfiguration.loadConfiguration(configFile);
            this.configFile = null;
        }

        @Override
        public void set(String s, Object o) {
            config.set(s, o);
        }

        @Override
        public Object get(String s) {
            return config.get(s);
        }

        @Override
        public String getString(String s) {
            return config.getString(s);
        }

        @Override
        public List<String> getKeys(boolean deep) {
            return new ArrayList<>(config.getKeys(deep));
        }

        @Override
        public boolean contains(String path) {
            return config.contains(path);
        }

        @Override
        public CrossConfigurationSection getConfigurationSection(String path) {
            return CrossConfigurationSection.from(config.getConfigurationSection(path));
        }

        @Override
        public boolean isConfigurationSection(String path) {
            return config.isConfigurationSection(path);
        }

        @Override
        public String saveToString() {
            return config.saveToString();
        }

        @Override
        public void save() {
            save(configFile);
        }

        @Override
        public void save(File f) {
            try {
                config.save(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static class BungeeCrossConfiguration extends CrossConfiguration {

        private Configuration config;
        private final File configFile;

        private BungeeCrossConfiguration() {
            this.configFile = null;
            try {
                config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private BungeeCrossConfiguration(File configFile) {
            this.configFile = configFile;
            try {
                config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private BungeeCrossConfiguration(Reader configFile) {
            try {
                config = net.md_5.bungee.config.YamlConfiguration.getProvider(net.md_5.bungee.config.YamlConfiguration.class).load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.configFile = null;
        }

        @Override
        public void set(String s, Object o) {
            config.set(s, o);
        }

        @Override
        public Object get(String s) {
            return config.get(s);
        }

        @Override
        public String getString(String s) {
            return config.getString(s);
        }

        @Override
        public List<String> getKeys(boolean deep) {
            return new ArrayList<>(config.getKeys());
        }

        @Override
        public boolean contains(String path) {
            return config.contains(path);
        }

        @Override
        public CrossConfigurationSection getConfigurationSection(String path) {
            return CrossConfigurationSection.from(config.getSection(path));
        }

        @Override
        public boolean isConfigurationSection(String path) {
            return config.getSection(path) != null;
        }

        @Override
        public String saveToString() {
            StringWriter sw = new StringWriter();
            ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).save(config, sw);
            return sw.toString();
        }

        @Override
        public void save() {
            save(configFile);
        }

        @Override
        public void save(File f) {
            try {
                ConfigurationProvider.getProvider(net.md_5.bungee.config.YamlConfiguration.class).save(config, f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}