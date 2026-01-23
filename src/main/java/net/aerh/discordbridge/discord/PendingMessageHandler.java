package net.aerh.discordbridge.discord;

import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/**
 * Handles messages that need to be sent when two conditions are met
 * (e.g., server booted AND bot connected). Ensures the message is
 * sent exactly once when both conditions become true.
 */
public final class PendingMessageHandler {

    private final BooleanSupplier readyCheck;
    private final Runnable sendAction;

    private boolean conditionMet;
    private boolean sent;

    public PendingMessageHandler(@NotNull BooleanSupplier readyCheck, @NotNull Runnable sendAction) {
        this.readyCheck = readyCheck;
        this.sendAction = sendAction;
    }

    /**
     * Called when the external condition is met (e.g., server booted).
     * Will send immediately if ready, otherwise waits for {@link #onReady()}.
     */
    public void onConditionMet() {
        this.conditionMet = true;
        trySend();
    }

    /**
     * Called when the system becomes ready (e.g., bot connected).
     * Will send if the condition was already met.
     */
    public void onReady() {
        trySend();
    }

    /**
     * Resets the handler state for reuse.
     */
    public void reset() {
        this.conditionMet = false;
        this.sent = false;
    }

    private void trySend() {
        if (sent || !conditionMet || !readyCheck.getAsBoolean()) {
            return;
        }

        sent = true;
        sendAction.run();
    }
}