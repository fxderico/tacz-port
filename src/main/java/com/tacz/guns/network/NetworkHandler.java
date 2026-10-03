package com.tacz.guns.network;

import com.tacz.guns.GunMod;
import com.tacz.guns.network.message.*;
import com.tacz.guns.network.message.event.*;
import com.tacz.guns.network.message.handshake.ServerMessageSyncedEntityDataMapping;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.tacz.guns.platform.forge.network.NetworkDirection;
import com.tacz.guns.platform.forge.network.PacketDistributor;
import com.tacz.guns.platform.forge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkHandler {
    private static final String VERSION = "1.0.5";

    public static final SimpleChannel CHANNEL = new SimpleChannel(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "network"));

    private static final AtomicInteger ID_COUNT = new AtomicInteger(1);

    public static void init() {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerShoot.class, ClientMessagePlayerShoot::encode, ClientMessagePlayerShoot::decode, ClientMessagePlayerShoot::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerReloadGun.class, ClientMessagePlayerReloadGun::encode, ClientMessagePlayerReloadGun::decode, ClientMessagePlayerReloadGun::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerCancelReload.class, ClientMessagePlayerCancelReload::encode, ClientMessagePlayerCancelReload::decode, ClientMessagePlayerCancelReload::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerFireSelect.class, ClientMessagePlayerFireSelect::encode, ClientMessagePlayerFireSelect::decode, ClientMessagePlayerFireSelect::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerAim.class, ClientMessagePlayerAim::encode, ClientMessagePlayerAim::decode, ClientMessagePlayerAim::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerCrawl.class, ClientMessagePlayerCrawl::encode, ClientMessagePlayerCrawl::decode, ClientMessagePlayerCrawl::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerDrawGun.class, ClientMessagePlayerDrawGun::encode, ClientMessagePlayerDrawGun::decode, ClientMessagePlayerDrawGun::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSound.class, ServerMessageSound::encode, ServerMessageSound::decode, ServerMessageSound::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageCraft.class, ClientMessageCraft::encode, ClientMessageCraft::decode, ClientMessageCraft::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageCraft.class, ServerMessageCraft::encode, ServerMessageCraft::decode, ServerMessageCraft::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerZoom.class, ClientMessagePlayerZoom::encode, ClientMessagePlayerZoom::decode, ClientMessagePlayerZoom::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageRefitGun.class, ClientMessageRefitGun::encode, ClientMessageRefitGun::decode, ClientMessageRefitGun::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageRefreshRefitScreen.class, ServerMessageRefreshRefitScreen::encode, ServerMessageRefreshRefitScreen::decode, ServerMessageRefreshRefitScreen::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageUnloadAttachment.class, ClientMessageUnloadAttachment::encode, ClientMessageUnloadAttachment::decode, ClientMessageUnloadAttachment::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSwapItem.class, ServerMessageSwapItem::encode, ServerMessageSwapItem::decode, ServerMessageSwapItem::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerBoltGun.class, ClientMessagePlayerBoltGun::encode, ClientMessagePlayerBoltGun::decode, ClientMessagePlayerBoltGun::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageLevelUp.class, ServerMessageLevelUp::encode, ServerMessageLevelUp::decode, ServerMessageLevelUp::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunHurt.class, ServerMessageGunHurt::encode, ServerMessageGunHurt::decode, ServerMessageGunHurt::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunKill.class, ServerMessageGunKill::encode, ServerMessageGunKill::decode, ServerMessageGunKill::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageUpdateEntityData.class, ServerMessageUpdateEntityData::encode, ServerMessageUpdateEntityData::decode, ServerMessageUpdateEntityData::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSyncGunPack.class, ServerMessageSyncGunPack::encode, ServerMessageSyncGunPack::decode, ServerMessageSyncGunPack::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerMelee.class, ClientMessagePlayerMelee::encode, ClientMessagePlayerMelee::decode, ClientMessagePlayerMelee::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunDraw.class, ServerMessageGunDraw::encode, ServerMessageGunDraw::decode, ServerMessageGunDraw::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunFire.class, ServerMessageGunFire::encode, ServerMessageGunFire::decode, ServerMessageGunFire::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunFireSelect.class, ServerMessageGunFireSelect::encode, ServerMessageGunFireSelect::decode, ServerMessageGunFireSelect::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunMelee.class, ServerMessageGunMelee::encode, ServerMessageGunMelee::decode, ServerMessageGunMelee::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunReload.class, ServerMessageGunReload::encode, ServerMessageGunReload::decode, ServerMessageGunReload::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunShoot.class, ServerMessageGunShoot::encode, ServerMessageGunShoot::decode, ServerMessageGunShoot::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSyncBaseTimestamp.class, ServerMessageSyncBaseTimestamp::encode, ServerMessageSyncBaseTimestamp::decode, ServerMessageSyncBaseTimestamp::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageSyncBaseTimestamp.class, ClientMessageSyncBaseTimestamp::encode, ClientMessageSyncBaseTimestamp::decode, ClientMessageSyncBaseTimestamp::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageLaserColor.class, ClientMessageLaserColor::encode, ClientMessageLaserColor::decode, ClientMessageLaserColor::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // was a login handshake on Forge, now a normal packet sent on join (see GunMod)
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSyncedEntityDataMapping.class, ServerMessageSyncedEntityDataMapping::encode, ServerMessageSyncedEntityDataMapping::decode, ServerMessageSyncedEntityDataMapping::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        // extra spawn data for bullets, bundled with the vanilla spawn packet
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageBulletSpawnData.class, ServerMessageBulletSpawnData::encode, ServerMessageBulletSpawnData::decode, ServerMessageBulletSpawnData::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        //? if fabric && <1.21.9 {
        /*// the smith table recipes, Fabric API syncs them itself from 1.21.9
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), com.tacz.guns.network.message.ServerMessageSyncRecipes.class, com.tacz.guns.network.message.ServerMessageSyncRecipes::encode, com.tacz.guns.network.message.ServerMessageSyncRecipes::decode, com.tacz.guns.network.message.ServerMessageSyncRecipes::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        *///?}
    }

    public static void sendToClientPlayer(Object message, Player player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }

    /**
     * 发送给所有监听此实体的玩家
     */
    public static void sendToTrackingEntityAndSelf(Entity centerEntity, Object message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> centerEntity), message);
    }

    public static void sendToAllPlayers(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToTrackingEntity(Object message, final Entity centerEntity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> centerEntity), message);
    }

    public static void sendToDimension(Object message, final Entity centerEntity) {
        ResourceKey<Level> dimension = centerEntity.level().dimension();
        CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }
}
