package org.popcraft.blueborder;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class BlueBorder extends JavaPlugin {
    private static final String MARKER_SET_ID = "worldborder";
    private static final String LABEL = "World border";
    private Color borderColor = new Color(0xFF0000, 1f);
    private FileConfiguration config;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Create config.yml if it doesn't exist
        if (!configFile.exists()) {
            config.set("borderColor.red", borderColor.getRed());
            config.set("borderColor.green", borderColor.getGreen());
            config.set("borderColor.blue", borderColor.getBlue());
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //checks config for border color if exists
        else if (config.contains("borderColor.red") && config.contains("borderColor.green") && config.contains("borderColor.blue")){
            int red = config.getInt("borderColor.red");
            int green = config.getInt("borderColor.green");
            int blue = config.getInt("borderColor.blue");
            if(red >= 0 && red <= 255 && green >= 0 && green <= 255 && blue >= 0 && blue <= 255) {
                borderColor = new Color(red, green, blue, 1f);
            }
            else{
                getServer().getConsoleSender().sendMessage("[" + this.getName() + "] Invalid color values in config. Using default color.");
                //assume config invalid, save new file
                saveBorderColor(borderColor);
            }
        }
        else{
            //assume config invalid, save new file
            saveBorderColor(borderColor);
        }

        //refreshes map
        BlueMapAPI.onEnable(this::addWorldBorders);
        BlueMapAPI.onDisable(this::removeWorldBorders);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase(this.getName())) {
            if(args.length == 0) {
                sender.sendMessage("Usage: /blueborder refresh, /blueborder color <red> <green> <blue>");
                return false;
            }
            else if(args.length > 0){
                if(args[0].equalsIgnoreCase("refresh")) {
                    BlueMapAPI.getInstance().ifPresent(this::removeWorldBorders);
                    BlueMapAPI.getInstance().ifPresent(this::addWorldBorders);
                    sender.sendMessage("World borders refreshed.");
                    return true;
                }
                else if(args[0].equalsIgnoreCase("color")) {
                    if(args.length != 4) {
                        sender.sendMessage("Usage: /blueborder color <red> <green> <blue>");
                        return false;
                    }
                    try {
                        int red = Integer.parseInt(args[1]);
                        int green = Integer.parseInt(args[2]);
                        int blue = Integer.parseInt(args[3]);
                        if(red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
                            sender.sendMessage("Invalid color values. Must be between 0 and 255.");
                            return false;
                        }
                        borderColor = new Color(red, green, blue, 1f);
                        BlueMapAPI.getInstance().ifPresent(this::removeWorldBorders);
                        BlueMapAPI.getInstance().ifPresent(this::addWorldBorders);
                        sender.sendMessage("World border color changed.");
                        saveBorderColor(borderColor);
                        return true;
                    }
                    catch(NumberFormatException e) {
                        sender.sendMessage("Invalid color values. Must be integers.");
                        return false;
                    }
                } 
            }
            else {
                sender.sendMessage("Usage: /blueborder refresh, /blueborder color <red> <green> <blue>");
                return false;
            }
        }
        return true;
    }

    private void saveBorderColor(Color color) {
        config.set("borderColor.red", color.getRed());
        config.set("borderColor.green", color.getGreen());
        config.set("borderColor.blue", color.getBlue());
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private void addWorldBorders(BlueMapAPI blueMapAPI) {
        for (final World world : getServer().getWorlds()) {
            final MarkerSet markerSet = MarkerSet.builder().label(LABEL).build();
            final WorldBorder worldBorder = world.getWorldBorder();
            final double centerX = worldBorder.getCenter().getX();
            final double centerZ = worldBorder.getCenter().getZ();
            final double radius = worldBorder.getSize() / 2d;
            final Vector2d pos1 = new Vector2d(centerX - radius, centerZ - radius);
            final Vector2d pos2 = new Vector2d(centerX + radius, centerZ + radius);
            final Shape border = Shape.createRect(pos1, pos2);
            final ShapeMarker marker = ShapeMarker.builder()
                    .label(LABEL)
                    .shape(border, world.getSeaLevel())
                    .lineColor(borderColor)
                    .fillColor(new Color(0))
                    .lineWidth(3)
                    .depthTestEnabled(false)
                    .build();
            markerSet.getMarkers().put(world.getName(), marker);
            blueMapAPI.getWorld(world.getName())
                    .map(BlueMapWorld::getMaps)
                    .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet)));
        }
    }

    private void removeWorldBorders(BlueMapAPI blueMapAPI) {
        blueMapAPI.getMaps().forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID));
    }
}
