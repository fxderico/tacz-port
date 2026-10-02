package com.tacz.guns.platform.forge.eventbus.api;

/**
 * Minimal stand-in for Forge's Event, enough for TACZ's own events and the vanilla hooks we fire.
 */
public class Event {
    private boolean canceled;
    private Result result = Result.DEFAULT;

    public boolean isCancelable() {
        return getClass().isAnnotationPresent(Cancelable.class);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean cancel) {
        if (!isCancelable()) {
            throw new UnsupportedOperationException("Attempted to cancel an uncancelable event " + getClass().getName());
        }
        this.canceled = cancel;
    }

    public boolean hasResult() {
        return true;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public enum Result {
        DENY, DEFAULT, ALLOW
    }
}
