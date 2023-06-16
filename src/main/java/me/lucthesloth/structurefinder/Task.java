package me.lucthesloth.structurefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;

import java.util.*;
import java.util.stream.Collectors;

public class Task implements Runnable{
    private Set<List<Integer>> _temples = new HashSet<>();
    private Set<List<Integer>> _huts = new HashSet<>();
    private int _minX, _minZ, _maxX, _maxZ, _curX, _curZ, xSize, zSize;
    double pivotX = 0, pivotZ = 0;
    private List<StructureFinder.Structures> _structures;
    private boolean debug;
    public Task(int minX, int minZ, int maxX, int maxZ, List<String> strc){
        super();
        _minX = minX;
        _minZ = minZ;
        _maxX = maxX;
        _maxZ = maxZ;
        _curX = minX;
        _curZ = minZ;
        xSize = maxX - minX;
        zSize = maxZ - minZ;
        _structures = strc.stream().map(t -> {
            switch (t.toLowerCase()){
                case "deserttemple":
                    return StructureFinder.Structures.DesertTemple;
                case "witchhut":
                    return StructureFinder.Structures.WitchHut;
                default:
                    return null;
            }
        }).collect(Collectors.toList());
        debug = StructureFinder.instance.config.getBoolean("displayProgress", true);
    }
    @Override
    public void run() {
        if (debug) Bukkit.getLogger().info(String.format("Currently at %d, %d", _curX, _curZ));
        if (pivotX >= 1) {
            pivotX = -0.1;
            pivotZ+=0.1;
        }
        for (_curX = (int) (_minX + xSize * pivotX); _curX < _minX + xSize * (pivotX + 0.1); _curX+=16) {
            for (_curZ = (int) (_minZ + zSize * pivotZ); _curZ < _minZ + zSize * (pivotZ + 0.1); _curZ+=16) {
                _structures.forEach(t -> structureLocation(t, _curX, _curZ));
            }
        }
        if (debug) Bukkit.getLogger().info(String.format("Checked until at %d, %d", _curX, _curZ));
        pivotX+=0.1;
        if (_curX >= _maxX && _curZ >= _maxZ){
            if (_structures.contains(StructureFinder.Structures.DesertTemple)){
                Bukkit.getLogger().info(String.format("Found %d temples", _temples.size()));
                StructureFinder.instance.config.set(String.format("temples%d", System.currentTimeMillis()), _temples.toArray());

            }
            if (_structures.contains(StructureFinder.Structures.WitchHut)) {
                Bukkit.getLogger().info(String.format("Found %d huts", _huts.size()));
                StructureFinder.instance.config.set(String.format("huts%d", System.currentTimeMillis()), _huts.toArray());
            }
            StructureFinder.instance.config.set("runCheck", false);
            StructureFinder.instance.saveConfig();
            StructureFinder.instance.FindTask.cancel();
        }
    }

    private void structureLocation(StructureFinder.Structures structure, double posX, double posZ){
        StructureType type = null;
        switch (structure){
            case DesertTemple:
                type = StructureType.DESERT_PYRAMID;
                break;
            case WitchHut:
                type = StructureType.SWAMP_HUT;
                break;
            default:
                return;
        }
        StructureType finalType = type;
        Bukkit.getWorlds().forEach(k -> {
            if (k.getEnvironment() == World.Environment.NORMAL) {
                Location temp = k.locateNearestStructure(new Location(k, posX, 65D, posZ), finalType, 64, false);
                if (temp != null) {
                    if (temp.getBlockZ() > _maxZ || temp.getBlockZ() < _minZ || temp.getBlockX() > _maxX || temp.getBlockX() < _minX) return;
                    switch (structure){
                        case DesertTemple:
                            _temples.add(Arrays.asList(temp.getBlockX(), temp.getBlockZ()));
                            break;
                        case WitchHut:
                            _huts.add(Arrays.asList(temp.getBlockX(), temp.getBlockZ()));
                            break;
                    }
                }
            }
        });
    }
}
