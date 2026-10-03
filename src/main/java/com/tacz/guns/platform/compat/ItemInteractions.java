package com.tacz.guns.platform.compat;

//? if <1.21.2 {
/*import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

/^*
 * Using an item on a block had its own result type before 1.21.2.
 ^/
public final class ItemInteractions {
    private ItemInteractions() {
    }

    public static ItemInteractionResult of(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.CONSUME;
        };
    }
}
*///?}
