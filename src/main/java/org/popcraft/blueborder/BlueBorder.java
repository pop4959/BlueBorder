package org.popcraft.blueborder;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class BlueBorder extends JavaPlugin {
    private static final String MARKERSET_ID = "worldborder";
    private static final String LABEL = "World border";

    @Override
    public void onEnable() {
        BlueMapAPI.onEnable(this::addWorldBorders);
        BlueMapAPI.onDisable(this::removeWorldBorders);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BlueMapAPI.getInstance().ifPresent(this::addWorldBorders);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private void addWorldBorders(BlueMapAPI blueMapAPI) {
        try {
            final MarkerAPI markerAPI = blueMapAPI.getMarkerAPI();
            final MarkerSet markerSet = markerAPI.createMarkerSet(MARKERSET_ID);
            markerSet.setLabel(LABEL);
            blueMapAPI.getMaps().forEach(blueMapMap -> {
                final World world = getServer().getWorld(blueMapMap.getWorld().getUuid());
                if (world != null) {
                    final WorldBorder worldBorder = world.getWorldBorder();
                    final double centerX = worldBorder.getCenter().getX();
                    final double centerZ = worldBorder.getCenter().getZ();
                    final double radius = worldBorder.getSize() / 2d;
                    final Vector2d pos1 = new Vector2d(centerX - radius, centerZ - radius);
                    final Vector2d pos2 = new Vector2d(centerX + radius, centerZ + radius);
                    final Shape border = Shape.createRect(pos1, pos2);
                    final ShapeMarker marker = markerSet.createShapeMarker(world.getName(), blueMapMap, border, world.getSeaLevel());
                    marker.setColors(Color.RED, new Color(0, true));
                    marker.setLabel(LABEL);
                }
            });
            markerAPI.save();
        } catch (IOException e) {
            getLogger().warning("Failed to add world borders");
        }
    }

    private void removeWorldBorders(BlueMapAPI blueMapAPI) {
        try {
            final MarkerAPI markerAPI = blueMapAPI.getMarkerAPI();
            if (markerAPI.removeMarkerSet(MARKERSET_ID)) {
                markerAPI.save();
            }
        } catch (IOException e) {
            getLogger().warning("Failed to remove world borders");
        }
    }
}
