package com.game.server.metrics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryTest {

    @Test
    void countersRenderWithHelpTypeAndSortedLabels() {
        final Registry r = new Registry();
        final Registry.Counter c = r.counter("omg_requests_total", "Requests.", "route", "status");
        c.inc("/b", "200");
        c.inc("/a", "200");
        c.add(4, "/a", "200");
        assertEquals("""
                # HELP omg_requests_total Requests.
                # TYPE omg_requests_total counter
                omg_requests_total{route="/a",status="200"} 5
                omg_requests_total{route="/b",status="200"} 1
                """, r.scrape());
    }

    @Test
    void histogramBucketsAreCumulativeAndEndWithInf() {
        final Registry r = new Registry();
        final Registry.Histogram h = r.histogram("omg_wait_seconds", "Wait.", new double[] {1, 5, 10});
        for (double v : new double[] {0.5, 1.0, 3, 7, 30}) {
            h.observe(v);
        }
        assertEquals("""
                # HELP omg_wait_seconds Wait.
                # TYPE omg_wait_seconds histogram
                omg_wait_seconds_bucket{le="1"} 2
                omg_wait_seconds_bucket{le="5"} 3
                omg_wait_seconds_bucket{le="10"} 4
                omg_wait_seconds_bucket{le="+Inf"} 5
                omg_wait_seconds_sum 41.5
                omg_wait_seconds_count 5
                """, r.scrape());
    }

    @Test
    void labelValuesAreEscaped() {
        final Registry r = new Registry();
        r.counter("omg_x_total", "X.", "v").inc("a\"b\\c\nd");
        assertTrue(r.scrape().contains("omg_x_total{v=\"a\\\"b\\\\c\\nd\"} 1"));
    }

    @Test
    void gaugesAreReadAtScrapeTime() {
        final Registry r = new Registry();
        final int[] depth = {3};
        r.gauge("omg_queue_depth", "Depth.", () -> depth[0], "game", "checkers");
        assertTrue(r.scrape().contains("omg_queue_depth{game=\"checkers\"} 3"));
        depth[0] = 7;
        assertTrue(r.scrape().contains("omg_queue_depth{game=\"checkers\"} 7"));
    }

    @Test
    void rejectsWrongLabelCountsAndBadNames() {
        final Registry r = new Registry();
        final Registry.Counter c = r.counter("omg_y_total", "Y.", "a");
        assertThrows(IllegalArgumentException.class, () -> c.inc());
        assertThrows(IllegalArgumentException.class, () -> r.counter("bad-name", "No."));
        assertThrows(IllegalArgumentException.class, () -> r.histogram("omg_h", "H.", new double[] {2, 1}));
    }

    @Test
    void concurrentIncrementsAreNotLost() throws InterruptedException {
        final Registry r = new Registry();
        final Registry.Counter c = r.counter("omg_z_total", "Z.", "k");
        final List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            threads.add(Thread.ofVirtual().start(() -> {
                for (int i = 0; i < 100_000; i++) {
                    c.inc("same");
                }
            }));
        }
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(800_000, c.value("same"));
    }
}
