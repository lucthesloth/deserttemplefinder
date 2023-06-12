package me.lucthesloth.deserttemplefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class DesertTempleFinder extends JavaPlugin implements Listener {
    public FileConfiguration config;
    public static DesertTempleFinder instance;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        this.config = this.getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        if (!config.getBoolean("runCheck", false)) {
            config.set("runCheck", true);
            saveConfig();
        } else {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                    int total = findTemples(config.getInt("minX", -10000), config.getInt("minZ", -10000), config.getInt("maxX", 10000), config.getInt("maxZ", 10000));
                    config.set("runCheck", false);
                    saveConfig();
                    Bukkit.getLogger().info(String.format("Found %d temples", total));

            }, 20L);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private int findTemples(int minX, int minZ, int maxX, int maxZ) {
        AtomicInteger count = new AtomicInteger();
        LinkedHashSet<List<Integer>> temples = new LinkedHashSet<>();
        Bukkit.getWorlds().forEach(w -> {
            if (w.getEnvironment() != org.bukkit.World.Environment.NORMAL) return;
            Location temp;
            Bukkit.getLogger().info(String.format("Checking world %s %d %d %d %d", w.getName(), minX, minZ, maxX, maxZ));
            for (int x = minX; x < maxX; x+=16) {
                for (int z = minZ; z < maxZ; z += 16) {
                    if (w.getBiome(x, z) == Biome.DESERT) {
                        temp = w.locateNearestStructure(new Location(w, x, 65, z), StructureType.DESERT_PYRAMID, 64, false);
                        if (temp != null) {
                            if (temp.getBlockZ() > maxZ || temp.getBlockZ() < minZ || temp.getBlockX() > maxX || temp.getBlockX() < minX) continue;
                            temples.add(Arrays.asList(temp.getBlockX(), temp.getBlockZ()));
                        }
                        if (x % 1000 == 0 && z % 1000 == 0) {
                            Bukkit.getLogger().info(String.format("Checked %d, %d", x, z));
                        }
                    }
                }
            }
        });
        getConfig().set(String.format("pyramids%d", System.currentTimeMillis()), temples.toArray());
        saveConfig();
        return temples.size();
    }
}
