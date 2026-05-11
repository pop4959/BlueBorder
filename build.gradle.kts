plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "org.popcraft"
version = "2.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filesMatching("bluemap.addon.json") {
            expand(
                "version" to project.version,
                "id" to rootProject.name
            )
        }
    }
    shadowJar {
        relocate("com.technicjelle.BMUtils", "org.popcraft.blueborder.shadow.BMUtils")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.bluecolored.de/releases")
}

dependencies {
    compileOnly("de.bluecolored:bluemap-common:5.13")
    compileOnly("de.bluecolored:bluenbt:3.5.0") //bluemap provides it
    implementation("com.technicjelle:BMUtils:5.0.3")
    implementation("org.spongepowered:configurate-yaml:4.2.0")
}
