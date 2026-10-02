package com.tacz.guns.platform.forge.event.entity.player;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemTooltipEvent extends Event {
    private final TooltipFlag flags;
    private final ItemStack itemStack;
    private final List<Component> toolTip;
    private final Player player;

    public ItemTooltipEvent(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags) {
        this.itemStack = itemStack;
        this.player = player;
        this.toolTip = list;
        this.flags = flags;
    }

    public TooltipFlag getFlags() {
        return flags;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getToolTip() {
        return toolTip;
    }

    @Nullable
    public Player getEntity() {
        return player;
    }
}
