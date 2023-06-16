package me.lucthesloth.structurefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class StructureFinder extends JavaPlugin {
    public static enum Structures  {
        DesertTemple, WitchHut
    }
    public FileConfiguration config;
    public static StructureFinder instance;
    public BukkitTask FindTask;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        this.config = this.getConfig();
        Check();
    }

    @Override
    public void onDisable() {

    }

    public void Check(){
        if (!config.getBoolean("runCheck", false)) return;
        List<String> search = (List<String>) config.getList("structures", Collections.emptyList());
        if (search.isEmpty()) return;
        if (FindTask != null && !FindTask.isCancelled()) return;
        FindTask = Bukkit.getScheduler().runTaskTimer(this, new Task(config.getInt("minX", -10000), config.getInt("minZ", -10000), config.getInt("maxX", 10000), config.getInt("maxZ", 10000), search), 10L, 10L);
    }

}
