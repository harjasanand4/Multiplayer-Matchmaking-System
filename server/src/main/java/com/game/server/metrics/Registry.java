package com.game.server.metrics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.DoubleSupplier;

/**
 * A small metrics registry that renders the Prometheus text exposition format (version 0.0.4).
 *
 * <p>Counters, gauges and histograms with labels. Updates are lock-free ({@link LongAdder},
 * {@link DoubleAdder}), so recording a request costs a few nanoseconds on the hot path; all the
 * formatting work happens when Prometheus scrapes {@code /metrics}. Written by hand instead of
 * pulling in a client library to keep the service dependency-free; the output is checked with
 * {@code promtool check metrics} in CI.
 */
public final class Registry {

    private final Map<String, Family> families = new ConcurrentHashMap<>();

    public Counter counter(String name, String help, String... labelNames) {
        return (Counter) families.computeIfAbsent(name, n -> new Counter(n, help, labelNames));
    }

    public Histogram histogram(String name, String help, double[] buckets, String... labelNames) {
        return (Histogram) families.computeIfAbsent(name, n -> new Histogram(n, help, buckets, labelNames));
    }

    /** A gauge read at scrape time. Register once per label combination. */
    public void gauge(String name, String help, DoubleSupplier value, String... labelPairs) {
        final Gauge g = (Gauge) families.computeIfAbsent(name, n -> new Gauge(n, help));
        g.add(labelPairs, value);
    }

    /** Same, but rendered with {@code # TYPE counter} (for monotonic values sampled from elsewhere). */
    public void sampledCounter(String name, String help, DoubleSupplier value, String... labelPairs) {
        final Gauge g = (Gauge) families.computeIfAbsent(name, n -> new Gauge(n, help, "counter"));
        g.add(labelPairs, value);
    }

    public String scrape() {
        final StringBuilder out = new StringBuilder(8192);
        final List<String> names = new ArrayList<>(families.keySet());
        Collections.sort(names);
        for (String name : names) {
            families.get(name).render(out);
        }
        return out.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private abstract static class Family {
        final String name;
        final String help;
        final String[] labelNames;

        Family(String name, String help, String[] labelNames) {
            if (!name.matches("[a-zA-Z_:][a-zA-Z0-9_:]*")) {
                throw new IllegalArgumentException("bad metric name " + name);
            }
            this.name = name;
            this.help = help;
            this.labelNames = labelNames;
        }

        abstract void render(StringBuilder out);

        void header(StringBuilder out, String type) {
            out.append("# HELP ").append(name).append(' ').append(help.replace("\\", "\\\\").replace("\n", "\\n"))
                    .append('\n');
            out.append("# TYPE ").append(name).append(' ').append(type).append('\n');
        }

        List<String> key(String... values) {
            if (values.length != labelNames.length) {
                throw new IllegalArgumentException(name + " expects labels " + Arrays.toString(labelNames));
            }
            return List.of(values);
        }

        static void labels(StringBuilder out, String[] names, List<String> values, String extraName, String extraValue) {
            if (names.length == 0 && extraName == null) {
                return;
            }
            out.append('{');
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(names[i]).append("=\"").append(escape(values.get(i))).append('"');
            }
            if (extraName != null) {
                if (names.length > 0) {
                    out.append(',');
                }
                out.append(extraName).append("=\"").append(extraValue).append('"');
            }
            out.append('}');
        }

        static String escape(String v) {
            return v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        }

        static String number(double v) {
            if (Double.isInfinite(v)) {
                return v > 0 ? "+Inf" : "-Inf";
            }
            if (v == Math.rint(v) && Math.abs(v) < 1e15) {
                return Long.toString((long) v);
            }
            return Double.toString(v);
        }

        static <V> List<Map.Entry<List<String>, V>> sorted(Map<List<String>, V> children) {
            final List<Map.Entry<List<String>, V>> entries = new ArrayList<>(children.entrySet());
            entries.sort(Comparator.comparing(e -> String.join("\u0000", e.getKey())));
            return entries;
        }
    }

    public static final class Counter extends Family {
        private final Map<List<String>, LongAdder> children = new ConcurrentHashMap<>();

        Counter(String name, String help, String[] labelNames) {
            super(name, help, labelNames);
        }

        public void inc(String... labelValues) {
            add(1, labelValues);
        }

        /**
         * Create the series at 0 without counting anything. Known label values should exist from
         * the start: a series that only appears on its first increment makes {@code rate()} and
         * {@code increase()} miss that first event, and dashboards show "No data" instead of 0.
         */
        public void touch(String... labelValues) {
            children.computeIfAbsent(key(labelValues), k -> new LongAdder());
        }

        public void add(long n, String... labelValues) {
            children.computeIfAbsent(key(labelValues), k -> new LongAdder()).add(n);
        }

        public long value(String... labelValues) {
            final LongAdder a = children.get(key(labelValues));
            return a == null ? 0 : a.sum();
        }

        @Override
        void render(StringBuilder out) {
            header(out, "counter");
            for (Map.Entry<List<String>, LongAdder> e : sorted(children)) {
                out.append(name);
                labels(out, labelNames, e.getKey(), null, null);
                out.append(' ').append(e.getValue().sum()).append('\n');
            }
        }
    }

    public static final class Histogram extends Family {
        private final double[] bounds;
        private final Map<List<String>, Child> children = new ConcurrentHashMap<>();

        private static final class Child {
            final LongAdder[] counts;
            final DoubleAdder sum = new DoubleAdder();
            final LongAdder count = new LongAdder();

            Child(int n) {
                counts = new LongAdder[n];
                for (int i = 0; i < n; i++) {
                    counts[i] = new LongAdder();
                }
            }
        }

        Histogram(String name, String help, double[] bounds, String[] labelNames) {
            super(name, help, labelNames);
            for (int i = 1; i < bounds.length; i++) {
                if (bounds[i] <= bounds[i - 1]) {
                    throw new IllegalArgumentException("histogram buckets must increase");
                }
            }
            this.bounds = bounds.clone();
        }

        public void observe(double value, String... labelValues) {
            final Child c = children.computeIfAbsent(key(labelValues), k -> new Child(bounds.length));
            int i = Arrays.binarySearch(bounds, value);
            if (i < 0) {
                i = -i - 1;  // first bucket whose bound is >= value
            }
            if (i < bounds.length) {
                c.counts[i].increment();
            }
            c.sum.add(value);
            c.count.increment();
        }

        public long count(String... labelValues) {
            final Child c = children.get(key(labelValues));
            return c == null ? 0 : c.count.sum();
        }

        @Override
        void render(StringBuilder out) {
            header(out, "histogram");
            for (Map.Entry<List<String>, Child> e : sorted(children)) {
                final Child c = e.getValue();
                long cumulative = 0;
                for (int i = 0; i < bounds.length; i++) {
                    cumulative += c.counts[i].sum();
                    out.append(name).append("_bucket");
                    labels(out, labelNames, e.getKey(), "le", number(bounds[i]));
                    out.append(' ').append(cumulative).append('\n');
                }
                final long total = c.count.sum();
                out.append(name).append("_bucket");
                labels(out, labelNames, e.getKey(), "le", "+Inf");
                out.append(' ').append(total).append('\n');
                out.append(name).append("_sum");
                labels(out, labelNames, e.getKey(), null, null);
                out.append(' ').append(number(c.sum.sum())).append('\n');
                out.append(name).append("_count");
                labels(out, labelNames, e.getKey(), null, null);
                out.append(' ').append(total).append('\n');
            }
        }
    }

    private static final class Gauge extends Family {
        private final String type;
        private final Map<String, DoubleSupplier> children = new TreeMap<>();

        Gauge(String name, String help) {
            this(name, help, "gauge");
        }

        Gauge(String name, String help, String type) {
            super(name, help, new String[0]);
            this.type = type;
        }

        synchronized void add(String[] labelPairs, DoubleSupplier value) {
            if (labelPairs.length % 2 != 0) {
                throw new IllegalArgumentException("label pairs must be name, value, name, value...");
            }
            final StringBuilder l = new StringBuilder();
            if (labelPairs.length > 0) {
                l.append('{');
                for (int i = 0; i < labelPairs.length; i += 2) {
                    if (i > 0) {
                        l.append(',');
                    }
                    l.append(labelPairs[i]).append("=\"").append(escape(labelPairs[i + 1])).append('"');
                }
                l.append('}');
            }
            children.put(l.toString(), value);
        }

        @Override
        synchronized void render(StringBuilder out) {
            header(out, type);
            for (Map.Entry<String, DoubleSupplier> e : children.entrySet()) {
                out.append(name).append(e.getKey()).append(' ').append(number(e.getValue().getAsDouble())).append('\n');
            }
        }
    }
}
