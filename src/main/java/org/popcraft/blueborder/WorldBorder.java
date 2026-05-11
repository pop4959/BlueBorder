package org.popcraft.blueborder;

import de.bluecolored.bluenbt.NBTName;

public class WorldBorder {
    @NBTName({"data", "Data"})
    private WorldBorderData data;

    public double getX() {
        return data.centerX;
    }

    public double getZ() {
        return data.centerZ;
    }

    public double getSize() {
        return data.size;
    }

    private static class WorldBorderData {
        @NBTName({"center_x", "BorderCenterX"})
        private double centerX;

        @NBTName({"center_z", "BorderCenterZ"})
        private double centerZ;

        @NBTName({"size", "BorderSize"})
        private double size;
    }
}
