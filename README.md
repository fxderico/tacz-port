# TACZ Fabric

Unofficial port of [Timeless & Classics Guns: Zero](https://github.com/MCModderAnchor/TACZ) (Forge 1.20.1) to Fabric 1.21.11.

Code is GPL-3.0 like upstream. Assets are CC BY-NC-ND 4.0 and belong to the original authors, so don't publish builds of this.

## Status

- Compiles and builds (`./gradlew build`), dedicated server boots and loads the default gun pack.
- Client not tested in game yet.
- Dropped: all optional mod compat (KubeJS, JEI, Cloth Config, Oculus, Shoulder Surfing, Controllable, Player Animator, Accelerated Rendering, Create), the target minecart.
- Scopes render without the stencil mask (1.21.5+ render pipelines have no stencil state).

## How it is put together

- `com.tacz.guns.platform.forge` is a small stand-in for the Forge APIs TACZ used (event bus, DeferredRegister, SimpleChannel, events). Forge events are fired from Fabric callbacks (`platform/FabricEventBridge`, `FabricClientEventBridge`) and mixins (`mixin/forge`, `mixin/client/forge`).
- Item NBT lives in the `minecraft:custom_data` component with the same keys (`util/ItemNbt`).
- Item rendering: every TACZ item model is `minecraft:special` -> `tacz:item` (`platform/client/TaczItemSpecialRenderer`), which calls the old BEWLR style renderers and draws into the main buffer source.
- Gun packs are served as one built-in pack (`resource/GunPackResources`), old 1.20 folder names (`recipes`, `loot_tables`, `tags/items`...) are remapped, Forge tags map to `c:` tags (`util/LegacyJson`).
- Config uses Forge Config API Port (`forgeconfigapiport`, required at runtime).

## Next steps

- Gun pack reload listeners now run in order (`platform/SequentialReloadListener`), retest the server: attachment indexes were failing because they loaded before their data.
- Client side: add `addReloaderOrdering(internal_assets, client_assets)` in `GunModClient`, then `./gradlew runClient` and fix whatever breaks (mixin targets, first person rendering, GUIs).
