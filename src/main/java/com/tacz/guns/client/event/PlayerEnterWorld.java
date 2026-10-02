package com.tacz.guns.client.event;

import com.tacz.guns.resource.PackConvertor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import com.tacz.guns.platform.forge.api.distmarker.Dist;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.tacz.guns.platform.forge.event.entity.player.PlayerEvent;
import com.tacz.guns.platform.forge.eventbus.api.SubscribeEvent;
import com.tacz.guns.platform.forge.fml.common.Mod;

import java.io.File;


@Environment(EnvType.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PlayerEnterWorld {

    @SubscribeEvent
    public static void onPlayerEnterWorld(PlayerEvent.PlayerLoggedInEvent event) {
        File[] files = PackConvertor.FOLDER.toFile().listFiles();
        if (files != null && files.length > 0){
            event.getEntity().displayClientMessage(pre(Component.translatable("message.tacz.convert_from_legacy.intro")), false);
            event.getEntity().displayClientMessage(pre(Component.translatable("message.tacz.convert_from_legacy.intro2")), false);
            Component component = Component.translatable("message.tacz.convert_from_legacy")
                    .append(Component.translatable("message.tacz.convert_from_legacy.button")
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent.RunCommand("/tacz convert"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("message.tacz.convert_from_legacy.hover"))
            )));
            event.getEntity().displayClientMessage(pre(component), false);
            event.getEntity().displayClientMessage(pre(Component.translatable("message.tacz.convert_from_legacy.hint")), false);
            event.getEntity().displayClientMessage(pre(Component.translatable("message.tacz.convert_from_legacy.hide")), false);
        }
    }

    private static Component pre(Component component) {
        return Component.translatable("message.tacz.pre").append(component);
    }
}
