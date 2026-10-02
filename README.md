# TACZ Fabric

Unofficial port of [Timeless & Classics Guns: Zero](https://github.com/MCModderAnchor/TACZ) (Forge 1.20.1) to Fabric, one jar per Minecraft version.

Code is GPL-3.0 like upstream. Assets are CC BY-NC-ND 4.0 and belong to the original authors, so don't publish builds of this.

## Status

| Minecraft | Jar | State |
|---|---|---|
| 1.21.11 | `tacz-fabric-1.21.11-*.jar` | works, client gametest passes |
| 26.1 - 26.1.2 | `tacz-fabric-26.1.2-*.jar` | works, client gametest passes |

Quilt loads the Fabric jars (with Fabric API installed).

- Dedicated server boots and loads the default gun pack (54 guns, 24 ammo, 99 attachments, 3 blocks).
- In game: holding, shooting (bullets hit and hurt mobs), reloading, iron sight and scope ADS, third person hold pose, inventory icons.
- Not tested yet: multiplayer on a dedicated server, the gun smith table GUI, refit screen, melee, explosives, every gun in the pack.
- Dropped: all optional mod compat (KubeJS, JEI, Cloth Config, Oculus, Shoulder Surfing, Controllable, Player Animator, Accelerated Rendering, Create), the target minecart.

## Building

One codebase for every version through [Stonecutter](https://stonecutter.kikugie.dev/). The code in `src/` is written for the version marked `active` in `stonecutter.gradle.kts` (1.21.11), other versions get it rewritten:

- simple renames (Mojang or Fabric API renamed a class or method) are string replacements in `stonecutter.gradle.kts`,
- real differences are `//? if >=26.1 { ... //?} else { ... //?}` blocks in the code, the inactive branch is commented out.

Versions and dependency versions are in `stonecutter.properties.toml`. Gradle needs a Java 25 JDK.

- `./gradlew build` builds every version, jars end up in `versions/<minecraft>/build/libs/`.
- `./gradlew :26.1.2:build` builds one.
- `./gradlew "Set active project to 26.1.2"` rewrites `src/` for that version (handy in an IDE), switch back to 1.21.11 before committing.

## How it is put together

- `com.tacz.guns.platform.forge` is a small stand-in for the Forge APIs TACZ used (event bus, DeferredRegister, SimpleChannel, events). Forge events are fired from Fabric callbacks (`platform/FabricEventBridge`, `FabricClientEventBridge`) and mixins (`mixin/forge`, `mixin/client/forge`).
- Item NBT lives in the `minecraft:custom_data` component with the same keys (`util/ItemNbt`).
- Item rendering: every TACZ item model is `minecraft:special` -> `tacz:item` (`platform/client/TaczItemSpecialRenderer`), which calls the old BEWLR style renderers and draws into the main buffer source.
- Gun packs are served as one built-in pack (`resource/GunPackResources`), old 1.20 folder names (`recipes`, `loot_tables`, `tags/items`...) are remapped, Forge tags map to `c:` tags (`util/LegacyJson`).
- Config uses Forge Config API Port (`forgeconfigapiport`, required at runtime).
- Scopes mask their eyepiece with the stencil buffer like on Forge. 1.21.5+ render targets have no stencil, so depth textures are allocated as depth + stencil (`mixin/client/GlDeviceStencilMixin`, `DirectStateAccessStencilMixin`) and the stencil state is set with raw GL calls around the scope draws (`util/RenderHelper`, `client/renderer/ScopeRenderTypes`).

## Testing

`./gradlew :<minecraft>:runClientGameTest` (for example `:26.1.2:runClientGameTest`) starts the client, makes a world, gives an AK-47, shoots a husk, aims, reloads, mounts an ACOG and checks third person (`src/gametest`). It fails on wrong ammo counts, missed shots or a failed reload, screenshots go to `versions/<minecraft>/build/run/clientGameTest/screenshots`. Needs a display, on a headless box run it under `xvfb-run -a`.

## Next steps

- More Minecraft versions: 26.2, 26.3, then back down 1.21.10 to 1.21. Loaders: NeoForge and Forge builds.
- 26.2 can run on Vulkan, the scope stencil uses raw OpenGL calls and needs a Vulkan path or a fallback there.
- Join a dedicated server with the client (`TestDedicatedServerContext` in the gametest can do it) to check the networking side.
- Gun smith table crafting and the refit screen (attachment install/remove over the network).
- Run through the rest of the default pack: shotguns, RPG/M320 explosions, minigun, burst fire, heat.
