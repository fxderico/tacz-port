package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.AmmoBoxItem;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.GunSmithTableItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import com.tacz.guns.platform.forge.registries.DeferredRegister;
import com.tacz.guns.platform.forge.registries.RegistryObject;

@SuppressWarnings("all")
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GunMod.MOD_ID);

    private static CreativeModeTab.Builder tabBuilder() {
        //? if neoforge {
        /*return CreativeModeTab.builder();
        *///?} else {
        return net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder();
        //?}
    }

    public static RegistryObject<CreativeModeTab> OTHER_TAB = TABS.register("other", () -> tabBuilder()
            .title(Component.translatable("itemGroup.tab.tacz.other"))
            .icon(() -> ModItems.GUN_SMITH_TABLE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.acceptAll(GunSmithTableItem.fillItemCategory());
                output.accept(ModItems.TARGET.get());
                output.accept(ModItems.STATUE.get());
                AmmoBoxItem.fillItemCategory(output);
            }).build());

    public static RegistryObject<CreativeModeTab> AMMO_TAB = TABS.register("ammo", () -> tabBuilder()
            .title(Component.translatable("itemGroup.tab.tacz.ammo"))
            .icon(() -> AmmoItemBuilder.create().setId(DefaultAssets.DEFAULT_AMMO_ID).build())
            .displayItems((parameters, output) -> output.acceptAll(AmmoItem.fillItemCategory())).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_SCOPE_TAB = TABS.register("scope", () -> tabBuilder()
            .title(Component.translatable("tacz.type.scope.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "scope_acog_ta31")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.SCOPE))).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_MUZZLE_TAB = TABS.register("muzzle", () -> tabBuilder()
            .title(Component.translatable("tacz.type.muzzle.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "muzzle_compensator_trident")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.MUZZLE))).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_STOCK_TAB = TABS.register("stock", () -> tabBuilder()
            .title(Component.translatable("tacz.type.stock.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "stock_militech_b5")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.STOCK))).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_GRIP_TAB = TABS.register("grip", () -> tabBuilder()
            .title(Component.translatable("tacz.type.grip.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "grip_magpul_afg_2")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.GRIP))).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_EXTENDED_MAG_TAB = TABS.register("extended_mag", () -> tabBuilder()
            .title(Component.translatable("tacz.type.extended_mag.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "extended_mag_3")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.EXTENDED_MAG))).build());

    public static RegistryObject<CreativeModeTab> ATTACHMENT_LASER_TAB = TABS.register("laser", () -> tabBuilder()
            .title(Component.translatable("tacz.type.laser.name"))
            .icon(() -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "laser_compact")).build())
            .displayItems((parameters, output) -> output.acceptAll(AttachmentItem.fillItemCategory(AttachmentType.LASER))).build());

    public static RegistryObject<CreativeModeTab> GUN_PISTOL_TAB = TABS.register("pistol", () -> tabBuilder()
            .title(Component.translatable("tacz.type.pistol.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "glock_17")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.PISTOL))).build());

    public static RegistryObject<CreativeModeTab> GUN_SNIPER_TAB = TABS.register("sniper", () -> tabBuilder()
            .title(Component.translatable("tacz.type.sniper.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ai_awp")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.SNIPER))).build());

    public static RegistryObject<CreativeModeTab> GUN_RIFLE_TAB = TABS.register("rifle", () -> tabBuilder()
            .title(Component.translatable("tacz.type.rifle.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ak47")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.RIFLE))).build());

    public static RegistryObject<CreativeModeTab> GUN_SHOTGUN_TAB = TABS.register("shotgun", () -> tabBuilder()
            .title(Component.translatable("tacz.type.shotgun.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "db_short")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.SHOTGUN))).build());

    public static RegistryObject<CreativeModeTab> GUN_SMG_TAB = TABS.register("smg", () -> tabBuilder()
            .title(Component.translatable("tacz.type.smg.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "hk_mp5a5")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.SMG))).build());

    public static RegistryObject<CreativeModeTab> GUN_RPG_TAB = TABS.register("rpg", () -> tabBuilder()
            .title(Component.translatable("tacz.type.rpg.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "rpg7")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.RPG))).build());

    public static RegistryObject<CreativeModeTab> GUN_MG_TAB = TABS.register("mg", () -> tabBuilder()
            .title(Component.translatable("tacz.type.mg.name"))
            .icon(() -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "m249")).build())
            .displayItems((parameters, output) -> output.acceptAll(AbstractGunItem.fillItemCategory(GunTabType.MG))).build());
}
