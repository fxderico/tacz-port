pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
    plugins {
        // NeoForge builds
        id("net.neoforged.moddev") version "2.0.148"
        // Forge builds
        id("net.minecraftforge.gradle") version "7.0.40"
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
        versions("1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2", "26.3")
        // NeoForge builds share the sources, `//? if neoforge` marks what differs
        for (mc in listOf("1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2", "26.2", "26.3")) {
            version("$mc-neoforge", mc).buildscript("build.neoforge.gradle.kts")
        }
        // Forge builds, `//? if forge`
        for (mc in listOf("1.21.5", "1.21.8", "1.21.10", "1.21.11")) {
            version("$mc-forge", mc).buildscript("build.forge.gradle.kts")
        }
        // the version the code in src/ is written for when committed
        vcsVersion = "1.21.11"
    }
}

rootProject.name = "tacz-fabric"
