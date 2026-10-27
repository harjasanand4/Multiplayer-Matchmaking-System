package com.game.server.metrics;

import com.game.server.core.Game;
import com.game.server.core.Match;
import com.game.server.core.Matchmaker;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;

/**
 * Every metric the service exports, in one place. Names follow Prometheus conventions
 * ({@code _total} counters, {@code _seconds} durations, base units).
 */
public final class ServiceMetrics implements Matchmaker.Events {

    // Latency buckets for HTTP: most requests are well under a millisecond in memory, a few ms with
    // Postgres; the upper buckets are there to make a slowdown visible.
    private static final double[] HTTP_BUCKETS = {0.0005, 0.001, 0.0025, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1};
    // Time to match spans from "instantly" to the 60 s timeout. The SLO bucket is 10 s.
    private static final double[] WAIT_BUCKETS = {0.25, 0.5, 1, 2, 3, 5, 7.5, 10, 15, 20, 30, 45, 60};
    private static final double[] GAP_BUCKETS = {10, 25, 50, 75, 100, 150, 200, 300, 400};
    private static final double[] TICK_BUCKETS = {0.0001, 0.00025, 0.0005, 0.001, 0.0025, 0.005, 0.01, 0.025, 0.05, 0.1};

    private final Registry registry = new Registry();

    private final Registry.Counter httpRequests = registry.counter("omg_http_requests_total",
            "HTTP requests by route, method and status code.", "route", "method", "status");
    private final Registry.Histogram httpDuration = registry.histogram("omg_http_request_duration_seconds",
            "HTTP request latency, from receiving the request to sending the last byte.", HTTP_BUCKETS, "route", "method");
    private final Registry.Counter tickets = registry.counter("omg_tickets_created_total",
            "Matchmaking tickets created.", "game");
    private final Registry.Counter matches = registry.counter("omg_matches_total",
            "Matches made (two tickets each).", "game");
    private final Registry.Counter expired = registry.counter("omg_tickets_expired_total",
            "Tickets that timed out without a match.", "game");
    private final Registry.Counter cancelled = registry.counter("omg_tickets_cancelled_total",
            "Tickets cancelled by the player.", "game");
    private final Registry.Histogram timeToMatch = registry.histogram("omg_time_to_match_seconds",
            "How long a player waited between joining the queue and being matched.", WAIT_BUCKETS, "game");
    private final Registry.Histogram ratingGap = registry.histogram("omg_match_rating_gap",
            "Absolute Elo difference between matched players.", GAP_BUCKETS, "game");
    private final Registry.Histogram tickDuration = registry.histogram("omg_matchmaker_tick_duration_seconds",
            "Time spent in one matchmaking pass over all queues, including persisting new matches.", TICK_BUCKETS);
    private final Registry.Counter persistFailures = registry.counter("omg_match_persist_failures_total",
            "Matches that could not be written to the database (players were put back in the queue).");
    private final Registry.Counter results = registry.counter("omg_results_total",
            "Reported match results by outcome.", "outcome");
    private final Registry.Counter storeErrors = registry.counter("omg_store_errors_total",
            "Database operations that failed.", "operation");

    public ServiceMetrics() {
        for (Game g : Game.values()) {
            tickets.touch(g.label());
            matches.touch(g.label());
            expired.touch(g.label());
            cancelled.touch(g.label());
        }
        for (String outcome : new String[] {"win", "draw", "duplicate", "not_found", "rejected"}) {
            results.touch(outcome);
        }
        persistFailures.touch();
        final long start = ManagementFactory.getRuntimeMXBean().getStartTime();
        registry.gauge("process_start_time_seconds", "Start time of the process since the Unix epoch.",
                () -> start / 1000.0);
        final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        registry.gauge("jvm_memory_used_bytes", "Used JVM memory.",
                () -> memory.getHeapMemoryUsage().getUsed(), "area", "heap");
        registry.gauge("jvm_memory_used_bytes", "Used JVM memory.",
                () -> memory.getNonHeapMemoryUsage().getUsed(), "area", "nonheap");
        registry.gauge("jvm_memory_max_bytes", "Maximum JVM heap.", () -> memory.getHeapMemoryUsage().getMax(),
                "area", "heap");
        final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        registry.gauge("jvm_threads_live", "Live platform threads (virtual threads are not counted).",
                threads::getThreadCount);
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            registry.sampledCounter("jvm_gc_collection_seconds_total", "Time spent in garbage collection.",
                    () -> gc.getCollectionTime() / 1000.0, "gc", gc.getName());
        }
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) {
            registry.sampledCounter("process_cpu_seconds_total", "CPU time used by the process.",
                    () -> os.getProcessCpuTime() / 1e9);
        }
    }

    public Registry registry() {
        return registry;
    }

    /** Register the per-game queue depth and the matchmaker heartbeat gauges. */
    public void watch(Matchmaker matchmaker) {
        for (Game g : Game.values()) {
            registry.gauge("omg_queue_depth", "Players currently searching for a match.",
                    () -> matchmaker.queueDepth(g), "game", g.label());
        }
        registry.gauge("omg_matchmaker_last_tick_timestamp_seconds",
                "When the matchmaker last completed a pass (Unix seconds). Stalls show up as this falling behind.",
                () -> matchmaker.lastTickMillis() / 1000.0);
    }

    public void request(String route, String method, int status, long durationNanos) {
        httpRequests.inc(route, method, Integer.toString(status));
        httpDuration.observe(durationNanos / 1e9, route, method);
    }

    public void result(String outcome) {
        results.inc(outcome);
    }

    public void storeError(String operation) {
        storeErrors.inc(operation);
    }

    @Override
    public void ticketCreated(Game game) {
        tickets.inc(game.label());
    }

    @Override
    public void matched(Match m) {
        matches.inc(m.game().label());
        timeToMatch.observe(m.waitedMillisA() / 1000.0, m.game().label());
        timeToMatch.observe(m.waitedMillisB() / 1000.0, m.game().label());
        ratingGap.observe(m.ratingGap(), m.game().label());
    }

    @Override
    public void expired(Game game, long waitedMillis) {
        expired.inc(game.label());
    }

    @Override
    public void cancelled(Game game) {
        cancelled.inc(game.label());
    }

    @Override
    public void tick(long durationNanos, int made) {
        tickDuration.observe(durationNanos / 1e9);
    }

    @Override
    public void persistFailed(int count, Exception error) {
        persistFailures.add(count);
    }
}
