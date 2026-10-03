package com.tacz.guns.platform;

import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Runs a list of reload listeners one after another (prepare and apply), then joins the outer reload barrier.
 * TACZ's loaders depend on each other in order (index files resolve the data files loaded before them),
 * which only worked by luck with vanilla running them side by side.
 */
public class SequentialReloadListener implements PreparableReloadListener {
    private static final PreparationBarrier IMMEDIATE = new PreparationBarrier() {
        @Override
        public <T> CompletableFuture<T> wait(T value) {
            return CompletableFuture.completedFuture(value);
        }
    };

    private final String name;
    private final Supplier<List<PreparableReloadListener>> listenerFactory;

    /**
     * @param listenerFactory called on every reload, so a fresh set of listeners can be created each time
     */
    public SequentialReloadListener(String name, Supplier<List<PreparableReloadListener>> listenerFactory) {
        this.name = name;
        this.listenerFactory = listenerFactory;
    }

    @Override
    //? if >=1.21.9 {
    public CompletableFuture<Void> reload(SharedState sharedState, Executor prepareExecutor, PreparationBarrier barrier, Executor applyExecutor) {
        List<PreparableReloadListener> listeners = listenerFactory.get();
        listeners.forEach(listener -> listener.prepareSharedState(sharedState));
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (PreparableReloadListener listener : listeners) {
            chain = chain.thenCompose(ignored -> listener.reload(sharedState, prepareExecutor, IMMEDIATE, applyExecutor));
        }
        return chain.thenCompose(ignored -> barrier.wait(null));
    }
    //?} else if >=1.21.2 {
    /*public CompletableFuture<Void> reload(PreparationBarrier barrier, net.minecraft.server.packs.resources.ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        List<PreparableReloadListener> listeners = listenerFactory.get();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (PreparableReloadListener listener : listeners) {
            chain = chain.thenCompose(ignored -> listener.reload(IMMEDIATE, manager, prepareExecutor, applyExecutor));
        }
        return chain.thenCompose(ignored -> barrier.wait(null));
    }
    *///?} else {
    /*public CompletableFuture<Void> reload(PreparationBarrier barrier, net.minecraft.server.packs.resources.ResourceManager manager,
                                          net.minecraft.util.profiling.ProfilerFiller prepareProfiler, net.minecraft.util.profiling.ProfilerFiller applyProfiler,
                                          Executor prepareExecutor, Executor applyExecutor) {
        List<PreparableReloadListener> listeners = listenerFactory.get();
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (PreparableReloadListener listener : listeners) {
            chain = chain.thenCompose(ignored -> listener.reload(IMMEDIATE, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor));
        }
        return chain.thenCompose(ignored -> barrier.wait(null));
    }
    *///?}

    @Override
    public String getName() {
        return name;
    }
}
