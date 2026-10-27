package com.game.server;

import com.game.server.core.Matchmaker;

import java.util.Map;

/**
 * Settings from environment variables (twelve-factor style), with defaults that work locally.
 *
 * <pre>
 *   PORT                    8080
 *   DATABASE_URL            unset = in-memory store; e.g. jdbc:postgresql://db:5432/omg
 *   DATABASE_USER / DATABASE_PASSWORD
 *   DATABASE_POOL_SIZE      16
 *   TICK_MS                 100      matchmaking pass interval
 *   WINDOW_BASE             50       initial Elo window
 *   WINDOW_WIDEN_PER_S      25       window growth per second waited
 *   WINDOW_MAX              400
 *   TICKET_TIMEOUT_S        60
 *   SHUTDOWN_DRAIN_S        5        how long readiness fails before the server stops
 * </pre>
 */
public record Config(int port, String databaseUrl, String databaseUser, String databasePassword, int poolSize,
                     long tickMillis, Matchmaker.Policy policy, long shutdownDrainMillis) {

    public static Config fromEnv(Map<String, String> env) {
        final Matchmaker.Policy defaults = Matchmaker.Policy.defaults();
        final Matchmaker.Policy policy = new Matchmaker.Policy(
                intVar(env, "WINDOW_BASE", defaults.baseWindow()),
                intVar(env, "WINDOW_WIDEN_PER_S", defaults.widenPerSecond()),
                intVar(env, "WINDOW_MAX", defaults.maxWindow()),
                intVar(env, "TICKET_TIMEOUT_S", (int) (defaults.ticketTimeoutMillis() / 1000)) * 1000L,
                defaults.retainResolvedMillis());
        return new Config(
                intVar(env, "PORT", 8080),
                blankToNull(env.get("DATABASE_URL")),
                env.getOrDefault("DATABASE_USER", "omg"),
                env.getOrDefault("DATABASE_PASSWORD", ""),
                intVar(env, "DATABASE_POOL_SIZE", 16),
                intVar(env, "TICK_MS", 100),
                policy,
                intVar(env, "SHUTDOWN_DRAIN_S", 5) * 1000L);
    }

    private static int intVar(Map<String, String> env, String name, int fallback) {
        final String v = env.get(name);
        if (v == null || v.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer, got '" + v + "'");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
