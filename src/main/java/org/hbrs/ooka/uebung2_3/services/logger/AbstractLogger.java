package org.hbrs.ooka.uebung2_3.services.logger;

import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public abstract class AbstractLogger implements ILogger {
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    protected final Logger logger;
    protected AbstractLogger(String name) {
        logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(new SynchronizedConsoleHandler(new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder();
                sb.append(sdf.format(new Date(record.getMillis())))
                        .append(": [")
                        .append(record.getLoggerName())
                        .append("]")
                        .append(" (")
                        .append(record.getLevel())
                        .append(") ")
                        .append(record.getMessage())
                        .append(System.lineSeparator());

                @Nullable Throwable e = record.getThrown();
                if (e != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    sb.append(System.lineSeparator()).append(sw).append(System.lineSeparator());
                }

                return sb.toString();
            }
        }));
    }

    @Override
    public void sendLog(Level level, String s, @Nullable Throwable throwable) {
        if (throwable == null) logger.log(level, s);
        else logger.log(level, s, throwable);
    }

    private static class SynchronizedConsoleHandler extends StreamHandler {
        public SynchronizedConsoleHandler(Formatter formatter) {
            super(System.out, formatter);
        }

        @Override
        public synchronized void publish(LogRecord record) {
            super.publish(record);
            flush();
        }

        @Override
        public synchronized void close() {
            flush();
        }
    }
}
