plugins {
    id("java-library")
}

group = "org.popcraft"
version = "1.1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:v2.1.0")
}
