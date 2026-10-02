package com.tacz.guns.api.client.other;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * 用来在收物品时，让其保持一段时间渲染的接口
 */
public interface KeepingItemRenderer {
    /**
     * 物品保持渲染的时间
     *
     * @param itemStack 保持的物品
     * @param timeMs    时间，单位毫秒
     */
    void keep(ItemStack itemStack, long timeMs);

    /**
     * 获取当前主手正在渲染的物品
     */
    ItemStack getCurrentItem();

    /**
     * ItemInHandRenderer 通过 Mixin 的方式实现了此接口。
     * @return 返回 ItemInHandRenderer 实例
     */
    /**
     * No player yet (26.3 keeps the hand items on the local player).
     */
    KeepingItemRenderer NONE = new KeepingItemRenderer() {
        @Override
        public void keep(ItemStack itemStack, long timeMs) {
        }

        @Override
        public ItemStack getCurrentItem() {
            return ItemStack.EMPTY;
        }
    };

    static KeepingItemRenderer getRenderer(){
        //? if >=26.3 {
        /*net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? NONE : (KeepingItemRenderer) player.firstPersonHandsAndItems();
        *///?} else {
        return (KeepingItemRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        //?}
    }
}
