package org.popcraft.blueborder;

import com.technicjelle.BMUtils.BMNative.BMNConfigDirectory;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.math.Color;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

@ConfigSerializable
public class Config {
    private static final String fileName = "config.yml";

    private static final String DEFAULT_LABEL = "World border";
    private static final String DEFAULT_COLOR = "FF0000";
    private static final int DEFAULT_HEIGHT = 63;

    private @Nullable String color;
    private @Nullable String label;
    private @Nullable Integer height;

    public static Config load(BlueMapAPI api) throws IOException {
        BMNConfigDirectory.BMNCopy.fromJarResource(api, Config.class.getClassLoader(), fileName, fileName, false);
        Path configDirectory = BMNConfigDirectory.getAllocatedDirectory(api, Config.class.getClassLoader());
        Path configFile = configDirectory.resolve(fileName);

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .defaultOptions(options -> options.implicitInitialization(false))
                .path(configFile).build();

        Config config = loader.load().get(Config.class);
        if (config == null) {
            throw new IOException("Failed to load config");
        }
        return config;
    }

    public Color getColor() {
        return new Color(Integer.parseInt((color != null ? color : DEFAULT_COLOR).toLowerCase(), 16), 1f);
    }

    public String getLabel() {
        return label != null ? label : DEFAULT_LABEL;
    }

    public int getHeight() {
        return height != null ? height : DEFAULT_HEIGHT;
    }
}
