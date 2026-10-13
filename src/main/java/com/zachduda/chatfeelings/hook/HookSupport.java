package com.zachduda.chatfeelings.hook;

import com.zachduda.chatfeelings.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Shared error handling for hooks: a broken hook answers "no" and its first error is logged in debug mode. */
abstract class HookSupport implements PunishmentHook {
    private final AtomicBoolean reportedError = new AtomicBoolean(false);

    protected boolean safely(String action, BooleanSupplier check) {
        try {
            return check.getAsBoolean();
        } catch (RuntimeException | LinkageError err) {
            if (reportedError.compareAndSet(false, true)) {
                Log.debug(name() + " " + action + " check failed (further errors from it are hidden):", err);
            }
            return false;
        }
    }
}
