package com.tacz.guns.platform;

//? if fabric && <1.21.9 {
/*import com.tacz.guns.resource.manager.LootInjectionManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/^*
 * Reload listener registration through Fabric's v0 resource loader, before the 1.21.9 ResourceLoader API.
 ^/
public final class FabricReloadListeners {
    private FabricReloadListeners() {
    }

    public static void registerServer(Identifier id, PreparableReloadListener listener) {
        // the registry lookup comes with the listener factory instead of the 1.21.9 shared reload state
        // (the space keeps 26.1's registerReloader rename from rewriting the call)
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener (id, registries -> {
            LootInjectionManager.reloadRegistries = registries;
            return identifiable(id, listener);
        });
    }

    public static void registerClient(Identifier id, PreparableReloadListener listener) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).addReloadListener(identifiable(id, listener));
    }

    private static IdentifiableResourceReloadListener identifiable(Identifier id, PreparableReloadListener listener) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return id;
            }

            //? if >=1.21.2 {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
                return listener.reload(barrier, manager, prepareExecutor, applyExecutor);
            }
            //?} else {
            /^// reload listeners got profilers before 1.21.2
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, net.minecraft.util.profiling.ProfilerFiller prepareProfiler,
                                                  net.minecraft.util.profiling.ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
                return listener.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
            ^///?}

            @Override
            public String getName() {
                return listener.getName();
            }
        };
    }
}
*///?}
