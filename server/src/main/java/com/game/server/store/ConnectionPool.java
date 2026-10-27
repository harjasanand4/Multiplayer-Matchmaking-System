package com.game.server.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A small fixed-size JDBC connection pool.
 *
 * <p>Connections are opened lazily up to {@code size}. A caller that can't get one within
 * {@code borrowTimeoutMillis} gets an exception (and the request a 503) instead of queueing
 * forever, which is what keeps a slow database from piling up every request thread. A connection
 * that throws a connection-class error (SQLSTATE 08xxx) is discarded rather than returned.
 */
public final class ConnectionPool implements AutoCloseable {

    @FunctionalInterface
    public interface Work<T> {
        T run(Connection c) throws SQLException;
    }

    private final String url;
    private final String user;
    private final String password;
    private final int size;
    private final long borrowTimeoutMillis;
    private final BlockingQueue<Connection> idle;
    private final List<Connection> all = new ArrayList<>();
    private volatile boolean closed;

    public ConnectionPool(String url, String user, String password, int size, long borrowTimeoutMillis) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.size = size;
        this.borrowTimeoutMillis = borrowTimeoutMillis;
        this.idle = new ArrayBlockingQueue<>(size);
    }

    public <T> T withConnection(Work<T> work) throws SQLException {
        final Connection c = borrow();
        boolean broken = false;
        try {
            return work.run(c);
        } catch (SQLException e) {
            broken = isConnectionError(e);
            throw e;
        } finally {
            release(c, broken);
        }
    }

    /** Runs {@code work} in a transaction; commits on success, rolls back on any exception. */
    public <T> T inTransaction(Work<T> work) throws SQLException {
        return withConnection(c -> {
            c.setAutoCommit(false);
            try {
                final T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException ignored) {
                    // the original error is more useful
                }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        });
    }

    public int size() {
        return size;
    }

    public synchronized int open() {
        return all.size();
    }

    public int idle() {
        return idle.size();
    }

    private Connection borrow() throws SQLException {
        if (closed) {
            throw new SQLException("pool closed", "08003");
        }
        Connection c = idle.poll();
        if (c != null) {
            return c;
        }
        synchronized (this) {
            if (all.size() < size) {
                c = DriverManager.getConnection(url, user, password);
                all.add(c);
                return c;
            }
        }
        try {
            c = idle.poll(borrowTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("interrupted waiting for a connection", "08001", e);
        }
        if (c == null) {
            throw new SQLException("no database connection free within " + borrowTimeoutMillis + " ms", "08001");
        }
        return c;
    }

    private void release(Connection c, boolean broken) {
        if (broken || closed) {
            synchronized (this) {
                all.remove(c);
            }
            try {
                c.close();
            } catch (SQLException ignored) {
                // already broken
            }
            return;
        }
        idle.offer(c);
    }

    private static boolean isConnectionError(SQLException e) {
        final String state = e.getSQLState();
        return state != null && state.startsWith("08");
    }

    @Override
    public synchronized void close() {
        closed = true;
        for (Connection c : all) {
            try {
                c.close();
            } catch (SQLException ignored) {
                // shutting down
            }
        }
        all.clear();
        idle.clear();
    }
}
