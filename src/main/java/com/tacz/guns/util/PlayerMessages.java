package com.tacz.guns.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessages {
    private PlayerMessages() {
    }

    /**
     * Shows a message to the player, above the hotbar when {@code overlay} is set, in chat otherwise.
     */
    public static void send(Player player, Component message, boolean overlay) {
        //? if >=26.1 {
        /*if (overlay) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
        *///?} else {
        player.displayClientMessage(message, overlay);
        //?}
    }
}
