package me.lucthesloth.deserttemplefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
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
                    int total = findTemples(config.getInt("minX", -10000), -config.getInt("minZ", -10000), config.getInt("mxnX", 10000), config.getInt("manZ", 10000));
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

    private boolean addIfNotPresent(Location loc){
        if (config.getList("temples") == null) {
            config.set("temples", new ArrayList<String>(){{
                add(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
            }});
        } else {
            @SuppressWarnings("unchecked") ArrayList<String> temples = (ArrayList<String>) config.getList("temples", Collections.emptyList());
            assert temples != null;
            boolean found = temples.contains(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
            if (!found) {
                temples.add(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
                config.set("temples", temples);
                saveConfig();
                return true;
            }
        }
        return false;
    }
    private int findTemples(int minX, int minZ, int maxX, int maxZ) {
        AtomicInteger count = new AtomicInteger();
        Bukkit.getWorlds().forEach(w -> {
            Location temp;
            for (int x = minX; x < maxX; x+=16) {
                for (int z = minZ; z < maxZ; z += 16) {
                    if (w.getBiome(x, z) == Biome.DESERT) {
                        temp = w.locateNearestStructure(new Location(w, x, 65, z), StructureType.DESERT_PYRAMID, 64, false);
                        if (temp != null) {
                            if (addIfNotPresent(temp)) {
                                count.getAndIncrement();
                            }
                        }
                    }
                }
            }
        });
        return count.get();
    }
}
