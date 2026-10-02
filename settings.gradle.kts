pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    // picks the remapping loom for obfuscated versions and the plain one for 26.1+
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

stonecutter {
    create(rootProject) {
        // every Minecraft version range gets its own jar, see stonecutter.properties.toml
        versions("1.21.11", "26.1.2", "26.2")
        // the version the code in src/ is written for when committed
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "tacz-fabric"
