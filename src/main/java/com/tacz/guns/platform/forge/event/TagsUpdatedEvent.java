package com.tacz.guns.platform.forge.event;

import com.tacz.guns.platform.forge.eventbus.api.Event;
import com.tacz.guns.platform.forge.eventbus.api.Cancelable;

public class TagsUpdatedEvent extends Event {
    private final UpdateCause updateCause;

    public TagsUpdatedEvent(UpdateCause updateCause) {
        this.updateCause = updateCause;
    }

    public UpdateCause getUpdateCause() {
        return updateCause;
    }

    public enum UpdateCause {
        SERVER_DATA_LOAD, CLIENT_PACKET_RECEIVED
    }
}
