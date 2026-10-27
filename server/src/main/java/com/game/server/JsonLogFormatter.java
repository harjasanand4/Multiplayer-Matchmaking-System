package com.game.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/** One JSON object per line, so log collectors (Loki, CloudWatch, Stackdriver) can parse fields. */
public final class JsonLogFormatter extends Formatter {

    @Override
    public String format(LogRecord r) {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("{\"ts\":\"").append(Instant.ofEpochMilli(r.getMillis())).append('"');
        sb.append(",\"level\":\"").append(r.getLevel().getName()).append('"');
        sb.append(",\"logger\":\"").append(escape(shortName(r.getLoggerName()))).append('"');
        sb.append(",\"msg\":\"").append(escape(formatMessage(r))).append('"');
        if (r.getThrown() != null) {
            final StringWriter trace = new StringWriter();
            r.getThrown().printStackTrace(new PrintWriter(trace));
            sb.append(",\"error\":\"").append(escape(trace.toString())).append('"');
        }
        return sb.append("}\n").toString();
    }

    private static String shortName(String logger) {
        if (logger == null) {
            return "";
        }
        final int dot = logger.lastIndexOf('.');
        return dot >= 0 ? logger.substring(dot + 1) : logger;
    }

    static String escape(String s) {
        final StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
