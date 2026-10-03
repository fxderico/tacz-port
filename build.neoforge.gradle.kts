plugins {
    id("net.neoforged.moddev")
}

val mcVersion = sc.current.version
version = "${property("mod.version")}".replace("-fabric", "-neoforge")
group = property("mod.group") as String

// jar name: tacz-<loader>-<first>-to-<last>.jar over the Minecraft releases it supports, or tacz-<loader>-<version>.jar
val releaseRange = property("mod.jar_range") as String
base.archivesName = "tacz-neoforge-$releaseRange"
tasks.withType<AbstractArchiveTask>().configureEach { archiveVersion.set("") }

val requiredJava: JavaVersion = if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

// the access widener stays the source of truth, NeoForge reads access transformers
val accessTransformer = layout.buildDirectory.file("generated/accesstransformer.cfg")
// things Fabric API widens for everyone (transitive access wideners) that NeoForge leaves closed
val extraAccess = buildList {
    add(if (sc.current.parsed < "26.1") "public net.minecraft.client.gui.GuiGraphics guiRenderState"
        else "public net.minecraft.client.gui.GuiGraphicsExtractor guiRenderState")
}
val generateAccessTransformer = tasks.register("generateAccessTransformer") {
    val widener = sc.process(rootProject.file("src/main/resources/tacz.accesswidener"), "build/processed.accesswidener")
    inputs.file(widener)
    outputs.file(accessTransformer)
    doLast {
        val lines = widener.readLines().mapNotNull { line ->
            val parts = line.substringBefore('#').trim().split(Regex("\\s+"))
            if (parts.size < 3 || parts[0] != "accessible") return@mapNotNull null
            val owner = parts[2].replace('/', '.')
            when (parts[1]) {
                "class" -> "public $owner"
                "method" -> "public $owner ${parts[3]}${parts[4]}"
                "field" -> "public $owner ${parts[3]}"
                else -> null
            }
        }
        val extra = extraAccess
        accessTransformer.get().asFile.apply {
            parentFile.mkdirs()
            writeText((lines + extra).joinToString("\n", postfix = "\n"))
        }
    }
}

// dev only smoke test mod, the NeoForge stand-in for Fabric's client gametest (src/gametest)
val gametest = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

neoForge {
    version = property("deps.neoforge") as String
    accessTransformers.from(accessTransformer)


    mods {
        register("tacz") {
            sourceSet(sourceSets.main.get())
        }
        register("tacz_gametest") {
            sourceSet(gametest)
        }
    }

    runs {
        register("client") {
            client()
            jvmArguments.add("-Xmx3G")
            gameDirectory = layout.projectDirectory.dir("run/client")
            loadedMods = listOf(mods["tacz"])
        }
        // ./gradlew :<version>-neoforge:runGametest plays the scripted session, screenshots in run/gametest/screenshots
        register("gametest") {
            client()
            sourceSet = gametest
            jvmArguments.add("-Xmx3G")
            gameDirectory = layout.projectDirectory.dir("run/gametest")
        }
        register("server") {
            server()
            gameDirectory = layout.projectDirectory.dir("run/server")
            programArgument("--nogui")
            loadedMods = listOf(mods["tacz"])
        }
    }

}

// libraries TACZ ships inside its jar
val bundled = listOf(
    "org.apache.commons:commons-math3:3.6.1",
    "com.github.FiguraMC.luaj:luaj-core:3.0.8-figura",
    "com.github.FiguraMC.luaj:luaj-jse:3.0.8-figura",
    "org.apache.bcel:bcel:6.6.1",
    "com.maydaymemory:mae:1.1.2",
    "org.apache.maven:maven-artifact:3.9.6",
)

dependencies {
    for (lib in bundled) {
        implementation(lib) {
            exclude(group = "org.apache.commons", module = "commons-lang3")
            exclude(group = "com.google.code.findbugs", module = "jsr305")
            exclude(group = "it.unimi.dsi", module = "fastutil")
            exclude(group = "org.joml", module = "joml")
        }
        jarJar(lib) {
            isTransitive = false
        }
    }
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

tasks {
    named("createMinecraftArtifacts") { dependsOn(generateAccessTransformer) }

    withType<JavaCompile>().configureEach {
        options.release = requiredJava.majorVersion.toInt()
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "100000"))
    }

    processResources {
        val props = mapOf(
            "version" to project.version.toString(),
            "minecraft" to project.property("mod.mc_compat").toString(),
            "mc_version" to mcVersion,
            "neoforge" to project.property("deps.neoforge_min").toString(),
        )
        inputs.properties(props)
        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
        val mixinJava = mapOf("java" to "JAVA_${requiredJava.majorVersion}")
        inputs.properties(mixinJava)
        filesMatching("*.mixins.json") { expand(mixinJava) }
        // Fabric only
        exclude("fabric.mod.json", "tacz.accesswidener")
        // 26.1+ loads every data/*/loot_modifiers file on its own, the old global list would be read as a modifier
        if (sc.current.parsed >= "26.1") exclude("data/neoforge/loot_modifiers/global_loot_modifiers.json")
        // the generated access transformer, also needed by dev runs
        from(generateAccessTransformer) { into("META-INF") }
    }

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

    jar {
        from(rootProject.file("LICENSE")) { rename { "${it}_tacz" } }
    }
}
