package org.popcraft.blueborder;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import de.bluecolored.bluemap.common.api.BlueMapWorldImpl;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluenbt.BlueNBT;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public final class BlueBorder implements Runnable {
    private static final String MARKER_SET_ID = "worldborder";

    private static final BlueNBT nbt = new BlueNBT();

    private Config config;

    @Override
    public void run() {
        BlueMapAPI.onEnable(this::onEnable);
        // No need to remove anything onDisable, because all markers are removed when BlueMap disables, anyway.
        // The onEnable simply puts it back.
    }

    public void onEnable(BlueMapAPI blueMapAPI) {
        // Allow config reloads through `/bluemap reload`
        try {
            config = Config.load(blueMapAPI);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        addWorldBorders(blueMapAPI);
    }

    private void addWorldBorders(BlueMapAPI blueMapAPI) {
        for (final BlueMapWorld world : blueMapAPI.getWorlds()) {
            final MarkerSet markerSet = MarkerSet.builder().label(config.getLabel()).build();
            final WorldBorder worldBorder = getWorldBorder(world);
            final double centerX = worldBorder.getX();
            final double centerZ = worldBorder.getZ();
            final double radius = worldBorder.getSize() / 2d;
            final Vector2d pos1 = new Vector2d(centerX - radius, centerZ - radius);
            final Vector2d pos2 = new Vector2d(centerX + radius, centerZ + radius);
            final Shape border = Shape.createRect(pos1, pos2);
            final ShapeMarker marker = ShapeMarker.builder()
                    .label(config.getLabel())
                    .shape(border, config.getHeight())
                    .lineColor(config.getColor())
                    .fillColor(new Color(0))
                    .lineWidth(3)
                    .depthTestEnabled(false)
                    .build();
            markerSet.getMarkers().put(world.getId(), marker);
            world.getMaps().forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet));
        }
    }

    private static WorldBorder getWorldBorder(BlueMapWorld world) {
        final Path worldBorderFile = getWorldBorderFile(world);

        try (
                final InputStream in = Files.newInputStream(worldBorderFile);
                final InputStream compressedIn = new BufferedInputStream(new GZIPInputStream(in))
        ) {
            return nbt.read(compressedIn, WorldBorder.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read world border data from " + worldBorderFile, e);
        }
    }

    private static Path getWorldBorderFile(BlueMapWorld world) {
        final Path saveFolder = getSaveFolder(world);
        final Path worldBorderFile = saveFolder.resolve("data").resolve("minecraft").resolve("world_border.dat");
        if (Files.exists(worldBorderFile)) {
            return worldBorderFile;
        }

        final Path levelDatFile = saveFolder.resolve("level.dat");
        if (Files.exists(levelDatFile)) {
            return levelDatFile;
        }

        // on Bukkit, saveFolder() returns paths like `world_nether/DIM-1` for alternate dimensions, so we try the parent as well
        final Path levelDatFileBukkitDimension = saveFolder.getParent().resolve("level.dat");
        if (Files.exists(levelDatFileBukkitDimension)) {
            return levelDatFileBukkitDimension;
        }

        throw new RuntimeException("World border file not found for world " + saveFolder);
    }

    // Inspired by https://github.com/BlueMap-Minecraft/BlueMap/blob/3092de2e2320fef2081ddb5e5f1040846a3103f9/common/src/main/java/de/bluecolored/bluemap/common/api/BlueMapWorldImpl.java#L60-L69
    // But that method is deprecated, so we reimplement it here
    private static Path getSaveFolder(BlueMapWorld apiWorld) {
        BlueMapWorldImpl worldImpl = (BlueMapWorldImpl) apiWorld;
        World world = worldImpl.world();
        if (world instanceof MCAWorld mcaWorld) {
            return mcaWorld.getDimensionFolder();
        } else {
            throw new UnsupportedOperationException("Unsupported world type: " + world.getClass().getName());
        }
    }
}
