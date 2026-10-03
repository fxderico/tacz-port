plugins {
    // applies the remapping loom on obfuscated versions and the plain one on 26.1+
    id("dev.kikugie.loom-back-compat")
}

val mcVersion = sc.current.version
version = "${property("mod.version")}"
group = property("mod.group") as String

// jar name: tacz-<loader>-<first>-to-<last>.jar over the Minecraft releases it supports, or tacz-<loader>-<version>.jar
val releaseRange = property("mod.jar_range") as String
base.archivesName = "tacz-fabric-$releaseRange"
tasks.withType<AbstractArchiveTask>().configureEach { archiveVersion.set("") }

val requiredJava: JavaVersion = if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

repositories {
    maven("https://jitpack.io")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${property("deps.forge_config_api_port")}")

    // commons-math3: interpolation
    implementation("org.apache.commons:commons-math3:3.6.1")
    include("org.apache.commons:commons-math3:3.6.1")

    // LuaJ: gun logic + animation state machines
    implementation("com.github.FiguraMC.luaj:luaj-core:3.0.8-figura") { exclude(group = "org.apache.commons", module = "commons-lang3") }
    include("com.github.FiguraMC.luaj:luaj-core:3.0.8-figura")
    implementation("com.github.FiguraMC.luaj:luaj-jse:3.0.8-figura") { exclude(group = "org.apache.commons", module = "commons-lang3") }
    include("com.github.FiguraMC.luaj:luaj-jse:3.0.8-figura")
    implementation("org.apache.bcel:bcel:6.6.1") { exclude(group = "org.apache.commons", module = "commons-lang3") }
    include("org.apache.bcel:bcel:6.6.1")

    // mae: animation math used by the first-person renderer
    implementation("com.maydaymemory:mae:1.1.2") {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "it.unimi.dsi", module = "fastutil")
        exclude(group = "org.joml", module = "joml")
    }
    include("com.maydaymemory:mae:1.1.2")

    // gun pack version ranges
    implementation("org.apache.maven:maven-artifact:3.9.6")
    include("org.apache.maven:maven-artifact:3.9.6")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

loom {
    // 26.1+ is not obfuscated, its access wideners use the official names
    accessWidenerPath = sc.process(rootProject.file("src/main/resources/tacz.accesswidener"), "build/processed.accesswidener")
    mixin {
        defaultRefmapName = "tacz.refmap.json"
    }
}

fabricApi {
    // ./gradlew :<version>:runClientGameTest boots the client, makes a world and shoots a gun (src/gametest)
    configureTests {
        createSourceSet = true
        modId = "tacz-gametest"
        enableGameTests = false
        enableClientGameTests = true
        eula = true
        clearRunDirectory = true
    }
}

loom {
    // ./gradlew :<version>:runSmokeTest plays the scripted session of src/gametest (TaczSmokeTest), screenshots in
    // build/run/smokeTest/screenshots. Works on every version, unlike Fabric's client gametest (1.21.4+).
    runs {
        register("smokeTest") {
            client()
            source(sourceSets.getByName("gametest"))
            vmArg("-Dtacz.smoketest=true")
            vmArg("-Xmx3G")
            runDir("build/run/smokeTest")
        }
    }
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = requiredJava.majorVersion.toInt()
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "100000"))
    }

    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "minecraft" to project.property("mod.mc_compat").toString(),
            "java" to requiredJava.majorVersion,
            "mc_version" to mcVersion,
            "forge_config_api_port" to project.property("deps.forge_config_api_port").toString(),
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
        val mixinJava = mapOf("java" to "JAVA_${requiredJava.majorVersion}")
        inputs.properties(mixinJava)
        filesMatching("*.mixins.json") { expand(mixinJava) }
        // NeoForge only
        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml")
    }

    // shaders follow the GLSL dialect of each version, older shader managers choke on the newer ones
    val shaderVariant = when {
        sc.current.parsed >= "26.3" -> "26.3"
        sc.current.parsed >= "26.2" -> "26.2"
        else -> null
    }
    if (shaderVariant != null) {
        processResources {
            from(rootProject.file("src/main/shaders/$shaderVariant")) { into("assets/tacz/shaders") }
        }
    }

    // before 1.21.2 recipe ingredients were {"item": ..} / {"tag": ..} objects
    if (sc.current.parsed < "1.21.2") {
        processResources {
            from(rootProject.file("src/main/legacy-recipes")) { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
        }
    }

    // before 1.21.4's item model definitions: builtin renderers and predicate overrides replace the plain item models
    if (sc.current.parsed < "1.21.4") {
        processResources {
            from(rootProject.file("src/main/legacy-item-models")) { duplicatesStrategy = DuplicatesStrategy.INCLUDE }
        }
    }

    jar {
        from(rootProject.file("LICENSE")) { rename { "${it}_tacz" } }
    }
}
