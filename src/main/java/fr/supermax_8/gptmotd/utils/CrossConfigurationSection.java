package fr.supermax_8.gptmotd.utils;

import net.md_5.bungee.config.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

// A wrapper for the Bukkit and Bungee configuration classes
public abstract class CrossConfigurationSection {

    public abstract Object get(String path);

    public abstract void set(String path, Object value);

    public abstract boolean contains(String path);

    public abstract CrossConfigurationSection getConfigurationSection(String path);
    public abstract Collection<String> getKeys(boolean deep);

    public abstract String getName();
    public static boolean isConfigurationSection(Object o) {
        try {
            return o instanceof ConfigurationSection;
        } catch (Error error) {
            return o instanceof Configuration;
        }
    }

    public static CrossConfigurationSection from(Object section) {
        try {
            return new BukkitCrossConfigurationSection((ConfigurationSection) section);
        } catch (Error error) {
            return new BungeeCrossConfigurationSection((Configuration) section);
        }
    }

    private static class BukkitCrossConfigurationSection extends CrossConfigurationSection {
        private ConfigurationSection section;

        public BukkitCrossConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        @Override
        public Object get(String path) {
            return section.get(path);
        }

        @Override
        public void set(String path, Object value) {
            section.set(path, value);
        }

        @Override
        public boolean contains(String path) {
            return section.contains(path);
        }

        @Override
        public CrossConfigurationSection getConfigurationSection(String path) {
            return from(section.getConfigurationSection(path));
        }

        @Override
        public Collection<String> getKeys(boolean deep) {
            return section.getKeys(deep);
        }

        @Override
        public String getName() {
            return section.getName();
        }
    }

    private static class BungeeCrossConfigurationSection extends CrossConfigurationSection {
        private Configuration section;

        public BungeeCrossConfigurationSection(Configuration section) {
            this.section = section;
        }

        @Override
        public Object get(String path) {
            return section.get(path);
        }

        @Override
        public void set(String path, Object value) {
            section.set(path, value);
        }

        @Override
        public boolean contains(String path) {
            return section.contains(path);
        }

        @Override
        public Collection<String> getKeys(boolean deep) {
            return section.getKeys();
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public CrossConfigurationSection getConfigurationSection(String path) {
            return from(section.getSection(path));
        }

    }

}