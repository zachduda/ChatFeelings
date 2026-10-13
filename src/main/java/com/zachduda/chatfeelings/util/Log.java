package com.zachduda.chatfeelings.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin-wide logging. Uses the plugin's own logger once {@link #init(Logger)} is called (so lines are
 * tagged "[ChatFeelings]" by the server), and a plain JUL logger before that, which keeps classes that
 * log (storage, migrations) usable from unit tests.
 */
public final class Log {
    private static volatile Logger logger = Logger.getLogger("ChatFeelings");
    private static volatile boolean debug = false;
    private static volatile boolean reduced = false;

    private Log() {
    }

    public static void init(Logger pluginLogger) {
        logger = pluginLogger;
    }

    /** Applies Other.Debug and Other.Reduce-Console-Msgs. */
    public static void configure(boolean debugEnabled, boolean reduceMessages) {
        debug = debugEnabled;
        reduced = reduceMessages;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isReduced() {
        return reduced;
    }

    /** Routine information. Hidden when Reduce-Console-Msgs is on. */
    public static void info(String msg) {
        if (!reduced) {
            logger.info(msg);
        }
    }

    /** Information that should always be shown, even with Reduce-Console-Msgs on. */
    public static void notice(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warning(msg);
    }

    /** Logs the message, plus the stack trace when debug mode is on. */
    public static void warn(String msg, Throwable err) {
        if (debug) {
            logger.log(Level.WARNING, msg, err);
        } else {
            logger.warning(msg + " (" + err + ")");
        }
    }

    public static void severe(String msg, Throwable err) {
        logger.log(Level.SEVERE, msg, err);
    }

    public static void debug(String msg) {
        if (debug) {
            logger.info("[Debug] " + msg);
        }
    }

    public static void debug(String msg, Throwable err) {
        if (debug) {
            logger.log(Level.INFO, "[Debug] " + msg, err);
        }
    }
}
