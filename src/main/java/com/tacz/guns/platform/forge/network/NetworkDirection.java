package com.tacz.guns.platform.forge.network;

import com.tacz.guns.platform.forge.fml.LogicalSide;

public enum NetworkDirection {
    PLAY_TO_SERVER(LogicalSide.SERVER),
    PLAY_TO_CLIENT(LogicalSide.CLIENT);

    private final LogicalSide receptionSide;

    NetworkDirection(LogicalSide receptionSide) {
        this.receptionSide = receptionSide;
    }

    public LogicalSide getReceptionSide() {
        return receptionSide;
    }

    public LogicalSide getOriginationSide() {
        return receptionSide == LogicalSide.SERVER ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }
}
