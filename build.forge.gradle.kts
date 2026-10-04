import groovy.json.JsonOutput

plugins {
    id("net.minecraftforge.gradle")
}

val mcVersion = sc.current.version
version = "${property("mod.version")}".replace("-fabric", "-forge")
group = property("mod.group") as String

// jar name: tacz-<loader>-<first>-to-<last>.jar over the Minecraft releases it supports, or tacz-<loader>-<version>.jar
val releaseRange = property("mod.jar_range") as String
base.archivesName = "tacz-forge-$releaseRange"
tasks.withType<AbstractArchiveTask>().configureEach { archiveVersion.set("") }

val requiredJava: JavaVersion = if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21

// the access widener stays the source of truth, Forge reads access transformers. Mavenizer applies them while
// resolving Minecraft, so the file is written while the build is configured.
val atFile = layout.buildDirectory.file("generated/accesstransformer.cfg").get().asFile
run {
    val widener = sc.process(rootProject.file("src/main/resources/tacz.accesswidener"), "build/processed.accesswidener")
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
    // things Fabric API widens for everyone (transitive access wideners) that Forge leaves closed
    val extra = buildList {
        if (sc.current.parsed >= "26.1") add("public net.minecraft.client.gui.GuiGraphicsExtractor guiRenderState")
        else if (sc.current.parsed >= "1.21.6") add("public net.minecraft.client.gui.GuiGraphics guiRenderState")
        // render state shards the scope render types reuse before 1.21.5's pipelines (NeoForge opens these up)
        if (sc.current.parsed < "1.21.5") {
            for (shard in listOf("LEQUAL_DEPTH_TEST", "NO_DEPTH_TEST", "COLOR_WRITE")) add("public net.minecraft.client.renderer.RenderStateShard $shard")
        }
        // item model registries Fabric fills directly, NeoForge through events
        if (sc.current.parsed >= "1.21.4") {
            add("public net.minecraft.client.renderer.special.SpecialModelRenderers ID_MAPPER")
            add("public net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties ID_MAPPER")
            add("public net.minecraft.client.color.item.ItemTintSources ID_MAPPER")
        }
    }
    val text = (lines + extra).joinToString("\n", postfix = "\n")
    atFile.parentFile.mkdirs()
    if (!atFile.exists() || atFile.readText() != text) atFile.writeText(text)
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    maven("https://jitpack.io")
    mavenCentral()
}

// dev only smoke test mod, the Forge stand-in for Fabric's client gametest (src/gametest)
val gametest = sourceSets.create("gametest") {
    compileClasspath += sourceSets.main.get().compileClasspath + sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().runtimeClasspath + sourceSets.main.get().output
}

// libraries TACZ ships inside its jar (Forge's jar-in-jar, META-INF/jarjar)
val bundled: Configuration = configurations.create("bundled") { isTransitive = false }

minecraft {
    mappings("official", mcVersion)
    accessTransformer.from(atFile)
    runs {
        // ./gradlew :<version>-forge:runClient -Psmoke plays the scripted session (src/gametest), screenshots in run/client/screenshots
        register("client") {
            workingDir.set(layout.projectDirectory.dir("run/client"))
            maxHeapSize.set("3G")
            mods {
                create("tacz") { sources(sourceSets.main.get()) }
                create("tacz_gametest") { sources(gametest) }
            }
            // the bundled libraries live on FML's legacy class path in dev
            extraLibraries(bundled)
            // the jar manifest names the mixin configs in production
            args("--mixin.config", "tacz.mixins.json", "--mixin.config", "tacz.sbm.mixins.json")
            if (project.hasProperty("smoke")) systemProperty("tacz.smokeTest", "true")
        }
        register("server") {
            workingDir.set(layout.projectDirectory.dir("run/server"))
            args("--nogui")
        }
    }
}

val bundledLibs = listOf(
    "org.apache.commons:commons-math3:3.6.1",
    "com.github.FiguraMC.luaj:luaj-core:3.0.8-figura",
    "com.github.FiguraMC.luaj:luaj-jse:3.0.8-figura",
    "org.apache.bcel:bcel:6.6.1",
    "com.maydaymemory:mae:1.1.2",
)
dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:$mcVersion-${property("deps.forge")}"))
    for (lib in bundledLibs) {
        implementation(lib) {
            exclude(group = "org.apache.commons", module = "commons-lang3")
            exclude(group = "com.google.code.findbugs", module = "jsr305")
            exclude(group = "it.unimi.dsi", module = "fastutil")
            exclude(group = "org.joml", module = "joml")
        }
        bundled(lib)
    }
    // the smoke test mod rides along on the client run's class path, see runs.client
    if (project.hasProperty("smoke")) runtimeOnly(gametest.output)
    // Forge ships maven-artifact itself (FML's version ranges)
    compileOnly("org.apache.maven:maven-artifact:3.9.6")
    // Forge ships MixinExtras itself from 1.21.9, older versions get it bundled (it is a mod of its own)
    if (sc.current.parsed < "1.21.9") {
        compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.5")!!)
        implementation("io.github.llamalad7:mixinextras-forge:0.5.5")
        bundled("io.github.llamalad7:mixinextras-forge:0.5.5")
    }
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    if (sc.current.parsed >= "1.21.6") {
        annotationProcessor("net.minecraftforge:eventbus-validator:7.0.5")
    }
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

// META-INF/jarjar/metadata.json, what Forge's JarJar reads to load the nested libraries
val jarJarMetadata = tasks.register("jarJarMetadata") {
    val out = layout.buildDirectory.file("generated/jarjar/META-INF/jarjar/metadata.json")
    val artifacts = bundled.incoming.artifacts
    inputs.files(bundled)
    outputs.file(out)
    doLast {
        val jars = artifacts.artifacts.map { artifact ->
            val id = artifact.id.componentIdentifier as org.gradle.api.artifacts.component.ModuleComponentIdentifier
            mapOf(
                "identifier" to mapOf("group" to id.group, "artifact" to id.module),
                "version" to mapOf("range" to "[${id.version},)", "artifactVersion" to id.version),
                "path" to "META-INF/jarjar/${artifact.file.name}",
                "isObfuscated" to false,
            )
        }
        out.get().asFile.apply {
            parentFile.mkdirs()
            writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("jars" to jars))))
        }
    }
}

// Forge only serves a mod's assets and data with a pack.mcmeta (NeoForge and Fabric make one up), data pack formats
val packMeta = tasks.register("packMeta") {
    val out = layout.buildDirectory.file("generated/packmeta/pack.mcmeta")
    // the data pack formats of the first and last release this jar covers
    val (min, max) = when (mcVersion) {
        "1.21.1" -> 48 to 48
        "1.21.3" -> 57 to 57
        "1.21.4" -> 61 to 61
        "1.21.5" -> 71 to 71
        "1.21.8" -> 80 to 81
        "1.21.10" -> 88 to 88
        "1.21.11" -> 94 to 94
        "26.1.2" -> 101 to 101
        "26.2" -> 107 to 107
        else -> 121 to 121
    }
    inputs.property("formats", "$min-$max")
    outputs.file(out)
    doLast {
        val pack = if (sc.current.parsed >= "1.21.9") mapOf("description" to "tacz resources", "min_format" to min, "max_format" to max)
        else mapOf("description" to "tacz resources", "pack_format" to max, "supported_formats" to listOf(min, max))
        out.get().asFile.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("pack" to pack))))
    }
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
            "mc_version" to mcVersion,
            "forge" to project.property("deps.forge_min").toString(),
        )
        inputs.properties(props)
        filesMatching("META-INF/mods.toml") { expand(props) }
        // Forge ships Mixin 0.8.7, which knows no level above Java 21
        val mixinJava = mapOf("java" to "JAVA_21")
        inputs.properties(mixinJava)
        filesMatching("*.mixins.json") { expand(mixinJava) }
        // Fabric and NeoForge only
        exclude("fabric.mod.json", "tacz.accesswidener", "META-INF/neoforge.mods.toml", "data/neoforge/**")
        // the global loot modifier list sits under data/forge on Forge
        from(rootProject.file("src/main/resources/data/neoforge")) { into("data/forge") }
        // the generated access transformer, also needed by dev runs
        from(atFile) { into("META-INF") }
        from(packMeta)
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

    named<ProcessResources>("processGametestResources") {
        from(packMeta)
    }

    jar {
        from(rootProject.file("LICENSE")) { rename { "${it}_tacz" } }
        from(bundled) { into("META-INF/jarjar") }
        dependsOn(jarJarMetadata)
        from(layout.buildDirectory.dir("generated/jarjar"))
        manifest.attributes("MixinConfigs" to "tacz.mixins.json,tacz.sbm.mixins.json")
    }
}
