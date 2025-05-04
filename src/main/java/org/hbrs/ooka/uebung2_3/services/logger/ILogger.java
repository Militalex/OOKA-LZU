package org.hbrs.ooka.uebung2_3.services.logger;

import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public interface ILogger {

    void sendLog(Level logLevel, String msg, @Nullable Throwable throwable);

    default void sendLog(Level logLevel, String msg){
        sendLog(logLevel, msg, null);
    }

    default void info(String msg){
        sendLog(Level.INFO, msg);
    }

    default void warning(String msg){
        sendLog(Level.WARNING, msg);
    }

    default void warning(String msg, Throwable throwable){
        sendLog(Level.WARNING, msg, throwable);
    }

    default void severe(String msg){
        sendLog(Level.SEVERE, msg);
    }

    default void severe(String msg, Throwable throwable){
        sendLog(Level.SEVERE, msg, throwable);
    }
}
