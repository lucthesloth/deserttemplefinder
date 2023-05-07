package me.lucthesloth.deserttemplefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;

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
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    findTemples(config.getInt("minX", -10000), -config.getInt("minZ", -10000), config.getInt("mxnX", 10000), config.getInt("manZ", 10000));
                    config.set("runCheck", false);
                    saveConfig();
                    Bukkit.getLogger().info(String.format("Found %d temples", (config.getList("temples", Collections.emptyList())).size()));

                }
            }, 20L);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private void addIfNotPresent(Location loc){
        if (config.getList("temples") == null) {
            config.set("temples", new ArrayList<String>(){{
                add(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
            }});
        } else {
            ArrayList<String> temples = (ArrayList<String>) config.getList("temples");
            boolean found = temples.contains(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
            if (!found) {
                temples.add(String.format("%d,%d", loc.getBlockX(), loc.getBlockZ()));
                config.set("temples", temples);
                saveConfig();
            }
        }
    }
    private void findTemples(int minX, int minZ, int maxX, int maxZ) {
        Bukkit.getWorlds().forEach(w -> {
            Location temp;
            for (int x = minX; x < maxX; x+=16) {
                for (int z = minZ; z < maxZ; z += 16) {
                    if (w.getBiome(x, z) == Biome.DESERT) {
                        temp = w.locateNearestStructure(new Location(w, x, 65, z), StructureType.DESERT_PYRAMID, 64, false);
                        if (temp != null) {
                            addIfNotPresent(temp);
                        }
                    }
                }
            }
        });
    }
}
