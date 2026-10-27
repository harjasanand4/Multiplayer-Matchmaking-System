package com.game.server;

import com.game.server.core.Matchmaker;
import com.game.server.http.Api;
import com.game.server.metrics.ServiceMetrics;
import com.game.server.store.ConnectionPool;
import com.game.server.store.InMemoryStore;
import com.game.server.store.JdbcStore;
import com.game.server.store.Store;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/** Wires the service together and handles shutdown. */
public final class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        setUpLogging();
        final Config config = Config.fromEnv(System.getenv());

        final Store store;
        if (config.databaseUrl() == null) {
            LOG.warning("DATABASE_URL not set: using the in-memory store (ratings are lost on restart)");
            store = new InMemoryStore();
        } else {
            final JdbcStore jdbc = new JdbcStore(new ConnectionPool(config.databaseUrl(), config.databaseUser(),
                    config.databasePassword(), config.poolSize(), 2_000));
            connectWithRetry(jdbc);
            store = jdbc;
        }

        final ServiceMetrics metrics = new ServiceMetrics();
        final Matchmaker matchmaker = new Matchmaker(config.policy(), System::currentTimeMillis,
                store::saveMatches, metrics);
        metrics.watch(matchmaker);

        final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "matchmaker");
            t.setDaemon(true);
            return t;
        });
        ticker.scheduleWithFixedDelay(() -> {
            try {
                matchmaker.tick();
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "matchmaking tick failed", e);
            }
        }, config.tickMillis(), config.tickMillis(), TimeUnit.MILLISECONDS);

        final Api api = new Api(config.port(), matchmaker, store, metrics, System::currentTimeMillis,
                Math.max(5_000, 20 * config.tickMillis()));
        api.start();
        LOG.info(String.format("matchmaking server listening on :%d (store=%s, tick=%d ms, window=%d+%d/s<=%d)",
                api.port(), store.getClass().getSimpleName(), config.tickMillis(), config.policy().baseWindow(),
                config.policy().widenPerSecond(), config.policy().maxWindow()));

        // SIGTERM (docker stop, Kubernetes): fail readiness, let the load balancer notice, then stop.
        final CountDownLatch stopped = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // java.util.logging resets itself in its own shutdown hook, so log straight to stdout here.
            say("shutting down: failing readiness for " + config.shutdownDrainMillis() + " ms, then stopping");
            api.drain();
            try {
                Thread.sleep(config.shutdownDrainMillis());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            api.close();
            ticker.shutdownNow();
            store.close();
            say("stopped");
            stopped.countDown();
        }, "shutdown"));
        stopped.await();
    }

    private static void say(String message) {
        final LogRecord record = new LogRecord(Level.INFO, message);
        record.setLoggerName(Main.class.getName());
        System.out.print(new JsonLogFormatter().format(record));
        System.out.flush();
    }

    private static void connectWithRetry(JdbcStore store) throws InterruptedException {
        long backoff = 500;
        for (int attempt = 1; ; attempt++) {
            try {
                store.migrate();
                LOG.info("database ready (attempt " + attempt + ")");
                return;
            } catch (RuntimeException e) {
                if (attempt >= 10) {
                    throw e;
                }
                LOG.warning("database not ready (attempt " + attempt + "): " + e.getMessage() + "; retrying in "
                        + backoff + " ms");
                Thread.sleep(backoff);
                backoff = Math.min(backoff * 2, 5_000);
            }
        }
    }

    private static void setUpLogging() {
        LogManager.getLogManager().reset();
        final Logger root = Logger.getLogger("");
        // Wrap stdout so the logging framework's shutdown reset can't close it under us.
        final OutputStream stdout = new FilterOutputStream(System.out) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
        final Handler h = new StreamHandler(stdout, new JsonLogFormatter()) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();  // one line per event, immediately: containers collect stdout
            }
        };
        h.setLevel(Level.INFO);
        root.addHandler(h);
        root.setLevel(Level.INFO);
    }

    private Main() {}
}
