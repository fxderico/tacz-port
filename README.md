# TACZ Port

[Timeless & Classics Guns: Zero](https://github.com/MCModderAnchor/TACZ) (TaCZ) 1.1.8, ported from Forge 1.20.1 to **Fabric, NeoForge and Forge for every Minecraft version from 1.21 to 26.3**. Quilt runs the Fabric jars.

This is an unofficial port, published and distributed with the permission of the TaCZ owner. All credit for the mod, its guns, models, sounds and gun packs goes to the original authors, **Serene Wave Studio | Timeless Squad** and the [TaCZ team](https://github.com/MCModderAnchor/TACZ). Please report problems with this port here, not to the TaCZ authors.

## Download

Get the jars from the [Releases](https://github.com/fxderico/tacz-port/releases) page. Pick your loader, then the jar whose name covers your Minecraft version: `tacz-<loader>-<first>-to-<last>.jar`, or `tacz-<loader>-<version>.jar` for a single version.

| Minecraft | Fabric / Quilt | NeoForge | Forge |
|---|---|---|---|
| 1.21 - 1.21.1 | `tacz-fabric-1.21-to-1.21.1.jar` | `tacz-neoforge-1.21-to-1.21.1.jar` | `tacz-forge-1.21-to-1.21.1.jar` |
| 1.21.2 - 1.21.3 | `tacz-fabric-1.21.2-to-1.21.3.jar` | `tacz-neoforge-1.21.2-to-1.21.3.jar` | `tacz-forge-1.21.2-to-1.21.3.jar` |
| 1.21.4 | `tacz-fabric-1.21.4.jar` | `tacz-neoforge-1.21.4.jar` | `tacz-forge-1.21.4.jar` |
| 1.21.5 | `tacz-fabric-1.21.5.jar` | `tacz-neoforge-1.21.5.jar` | `tacz-forge-1.21.5.jar` |
| 1.21.6 - 1.21.8 | `tacz-fabric-1.21.6-to-1.21.8.jar` | `tacz-neoforge-1.21.6-to-1.21.8.jar` | `tacz-forge-1.21.6-to-1.21.8.jar` |
| 1.21.9 - 1.21.10 | `tacz-fabric-1.21.9-to-1.21.10.jar` | `tacz-neoforge-1.21.9-to-1.21.10.jar` | `tacz-forge-1.21.9-to-1.21.10.jar` |
| 1.21.11 | `tacz-fabric-1.21.11.jar` | `tacz-neoforge-1.21.11.jar` | `tacz-forge-1.21.11.jar` |
| 26.1 - 26.1.2 | `tacz-fabric-26.1-to-26.1.2.jar` | `tacz-neoforge-26.1-to-26.1.2.jar` | `tacz-forge-26.1-to-26.1.2.jar` |
| 26.2 | `tacz-fabric-26.2.jar` | `tacz-neoforge-26.2.jar` | `tacz-forge-26.2.jar` |
| 26.3 | `tacz-fabric-26.3.jar` | `tacz-neoforge-26.3.jar` | `tacz-forge-26.3.jar` |

### Requirements

- **Fabric / Quilt:** [Fabric API](https://modrinth.com/mod/fabric-api) and [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) for your Minecraft version.
- **NeoForge / Forge:** nothing else.
- Java 21 for 1.21.x, Java 25 for 26.x (the versions Minecraft itself asks for).

Gun packs made for TaCZ 1.1.x go into the `tacz` folder in your game directory, like on the original mod.

## What works

Every jar is started in game and runs a scripted session (`src/gametest`) that fails on any error:

- the default gun pack loads, an AK-47 shoots and hits a target, ammo goes down, reload refills it,
- aiming down the iron sights, mounting an ACOG scope and aiming through it (scope masking included),
- third person poses, inventory icons,
- ammo box variants (iron, gold, diamond, filled, dyed, creative),
- the gun smith table opened with a right click shows its recipes, the attachment workbench filters its recipes to the gun in hand.

Not tested yet: multiplayer on a dedicated server, the refit screen, melee, explosives and every gun of the pack. Optional compat with other mods (JEI, KubeJS, Shoulder Surfing, shader mods...) is not ported.

## Building

One codebase builds every jar through [Stonecutter](https://stonecutter.kikugie.dev/). The code in `src/` is written for Fabric 1.21.11 (the `active` version in `stonecutter.gradle.kts`); other versions and loaders get it rewritten:

- renames between versions are string replacements in `stonecutter.gradle.kts`,
- real differences are `//? if >=26.1 { ... //?} else { ... //?}` and `//? if neoforge` / `forge` / `fabric` blocks, the inactive branch is commented out.

Versions and dependency versions are in `stonecutter.properties.toml`. Gradle needs a Java 25 JDK.

- `./gradlew :1.21.11:build` builds the Fabric 1.21.11 jar, `:1.21.11-neoforge:build` and `:1.21.11-forge:build` the other loaders. Jars end up in `versions/<project>/build/libs/`.
- Build tools: Fabric Loom, NeoForge ModDevGradle, ForgeGradle 7.

## Testing

- Fabric: `./gradlew :<version>:runSmokeTest`, screenshots in `versions/<version>/build/run/smokeTest/screenshots`.
- NeoForge: `./gradlew :<version>-neoforge:runGametest`, screenshots in `versions/<version>-neoforge/run/gametest/screenshots`.
- Forge: `./gradlew :<version>-forge:runClient -Psmoke`, screenshots in `versions/<version>-forge/run/client/screenshots`.

The log ends with `TACZ_SMOKE_TEST PASSED` or `FAILED`. On a headless machine run it under `xvfb-run -a`. 26.3 uses SDL and wants a Wayland compositor instead: `weston --backend=headless --socket=wl-tacz &`, then `WAYLAND_DISPLAY=wl-tacz SDL_VIDEO_DRIVER=wayland`.

## License

- Code: GPL-3.0, like upstream TaCZ (see `LICENSE`).
- Assets (models, textures, sounds, the default gun pack): CC BY-NC-ND 4.0, owned by the TaCZ authors. Do not use them commercially or publish modified versions of them.
