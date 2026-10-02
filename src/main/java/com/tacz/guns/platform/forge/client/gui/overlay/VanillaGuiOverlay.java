package com.tacz.guns.platform.forge.client.gui.overlay;

import net.minecraft.resources.Identifier;

public enum VanillaGuiOverlay {
    CROSSHAIR("crosshair"),
    HOTBAR("hotbar");

    private final Identifier id;

    VanillaGuiOverlay(String id) {
        this.id = Identifier.withDefaultNamespace(id);
    }

    public Identifier id() {
        return id;
    }

    public NamedGuiOverlay type() {
        return new NamedGuiOverlay(id);
    }
}
