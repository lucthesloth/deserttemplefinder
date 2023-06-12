package me.lucthesloth.witchhutfinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class WitchHutFinder extends JavaPlugin implements Listener {
    public FileConfiguration config;
    public static WitchHutFinder instance;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        this.config = this.getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            int total = findTemples(config.getInt("minX", -10000), config.getInt("minZ", -10000), config.getInt("maxX", 10000), config.getInt("maxZ", 10000));
            config.set("runCheck", false);
            saveConfig();
            Bukkit.getLogger().info(String.format("Found %d Witch Huts", total));
        }, 20L);
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
                        temp = w.locateNearestStructure(new Location(w, x, 65, z), StructureType.SWAMP_HUT, 64, false);
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
        getConfig().set(String.format("huts%d", System.currentTimeMillis()), temples.toArray());
        saveConfig();
        return temples.size();
    }
}
