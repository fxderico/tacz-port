package com.tacz.guns.network.message;

//? if fabric && <1.21.9 {
/*import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.GunSmithTableSerializer;
import com.tacz.guns.crafting.TaczRecipes;
import com.tacz.guns.platform.forge.network.NetworkEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/^*
 * Sends the gun smith table recipes to the client. Since 1.21.2 vanilla no longer syncs recipes and Fabric API
 * only gained its recipe synchronization in 1.21.9, so older versions carry them in this packet.
 ^/
public class ServerMessageSyncRecipes {
    private final List<RecipeHolder<?>> recipes;

    public ServerMessageSyncRecipes(List<RecipeHolder<?>> recipes) {
        this.recipes = recipes;
    }

    public static ServerMessageSyncRecipes of(MinecraftServer server) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (holder.value() instanceof GunSmithTableRecipe) {
                recipes.add(holder);
            }
        }
        return new ServerMessageSyncRecipes(recipes);
    }

    public static void encode(ServerMessageSyncRecipes message, FriendlyByteBuf buf) {
        RegistryFriendlyByteBuf registryBuf = (RegistryFriendlyByteBuf) buf;
        registryBuf.writeVarInt(message.recipes.size());
        for (RecipeHolder<?> holder : message.recipes) {
            registryBuf.writeIdentifier(holder.id().identifier());
            GunSmithTableSerializer.STREAM_CODEC.encode(registryBuf, (GunSmithTableRecipe) holder.value());
        }
    }

    public static ServerMessageSyncRecipes decode(FriendlyByteBuf buf) {
        RegistryFriendlyByteBuf registryBuf = (RegistryFriendlyByteBuf) buf;
        int size = registryBuf.readVarInt();
        List<RecipeHolder<?>> recipes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Identifier id = registryBuf.readIdentifier();
            GunSmithTableRecipe recipe = GunSmithTableSerializer.STREAM_CODEC.decode(registryBuf);
            recipe.setId(id);
            recipes.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
        }
        return new ServerMessageSyncRecipes(recipes);
    }

    public static void handle(ServerMessageSyncRecipes message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> TaczRecipes.clientRecipes = RecipeMap.create(message.recipes));
        }
        context.setPacketHandled(true);
    }
}
*///?}
