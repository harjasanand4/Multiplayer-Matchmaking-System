package com.game.server.sim;

import com.game.server.core.Game;
import com.game.server.core.Match;
import com.game.server.core.Matchmaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Offline what-if analysis of matchmaking policies, on simulated time.
 *
 * <p>Players arrive as a Poisson process with Elo ratings drawn from N(1500, 250). The real
 * {@link Matchmaker} runs every 100 ms of simulated time; nothing else is mocked. For each arrival
 * rate and window policy it reports how long players waited and how even the matches were, which
 * is the trade-off the widening window is meant to balance. Deterministic: a fixed seed gives the
 * same table every run.
 *
 * <pre>
 *   java -cp target/omg-matchmaking-server.jar com.game.server.sim.Simulation [minutes] [seed]
 * </pre>
 */
public final class Simulation {

    public record Result(String policy, double arrivalsPerSecond, int arrivals, int matchedPlayers, int expired,
                         double p50WaitS, double p95WaitS, double p99WaitS, double meanGap, double p95Gap) {}

    public static Result run(String name, Matchmaker.Policy policy, double arrivalsPerSecond, int minutes, long seed) {
        final long[] now = {0};
        final List<Match> matches = new ArrayList<>();
        final int[] expired = {0};
        final Matchmaker mm = new Matchmaker(policy, () -> now[0], matches::addAll, new Matchmaker.Events() {
            @Override
            public void expired(Game game, long waitedMillis) {
                expired[0]++;
            }
        });
        final Random rng = new Random(seed);
        final long end = minutes * 60_000L;
        double nextArrival = exponential(rng, arrivalsPerSecond) * 1000.0;
        int arrivals = 0;
        for (now[0] = 0; now[0] <= end; now[0] += 100) {
            while (nextArrival <= now[0]) {
                final int rating = (int) Math.round(Math.max(600, Math.min(2600, 1500 + 250 * rng.nextGaussian())));
                mm.submit("p" + arrivals, Game.CHECKERS, rating);
                arrivals++;
                nextArrival += exponential(rng, arrivalsPerSecond) * 1000.0;
            }
            mm.tick();
        }
        final List<Double> waits = new ArrayList<>();
        final List<Double> gaps = new ArrayList<>();
        for (Match m : matches) {
            waits.add(m.waitedMillisA() / 1000.0);
            waits.add(m.waitedMillisB() / 1000.0);
            gaps.add((double) m.ratingGap());
        }
        Collections.sort(waits);
        Collections.sort(gaps);
        return new Result(name, arrivalsPerSecond, arrivals, waits.size(), expired[0], pct(waits, 0.50),
                pct(waits, 0.95), pct(waits, 0.99), gaps.stream().mapToDouble(d -> d).average().orElse(0),
                pct(gaps, 0.95));
    }

    public static void main(String[] args) {
        final int minutes = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        final long seed = args.length > 1 ? Long.parseLong(args[1]) : 7;
        final Matchmaker.Policy widening = Matchmaker.Policy.defaults();
        final Matchmaker.Policy strict = new Matchmaker.Policy(50, 0, 50, 60_000, 120_000);
        final Matchmaker.Policy loose = new Matchmaker.Policy(400, 0, 400, 60_000, 120_000);

        System.out.printf(Locale.ROOT, "%d simulated minutes per row, ratings ~ N(1500, 250), seed %d%n%n", minutes, seed);
        System.out.println("| arrivals/s | policy | matched | expired | wait p50 | wait p95 | wait p99 | mean gap | gap p95 |");
        System.out.println("|---|---|---|---|---|---|---|---|---|");
        for (double rate : new double[] {0.2, 1, 5, 50}) {
            for (Object[] p : new Object[][] {{"fixed +-50", strict}, {"fixed +-400", loose},
                    {"widening 50 + 25/s, max 400", widening}}) {
                final Result r = run((String) p[0], (Matchmaker.Policy) p[1], rate, minutes, seed);
                System.out.printf(Locale.ROOT, "| %s | %s | %.1f %% | %.1f %% | %.1f s | %.1f s | %.1f s | %.0f | %.0f |%n",
                        rate, r.policy(), 100.0 * r.matchedPlayers() / r.arrivals(), 100.0 * r.expired() / r.arrivals(),
                        r.p50WaitS(), r.p95WaitS(), r.p99WaitS(), r.meanGap(), r.p95Gap());
            }
        }
    }

    private static double exponential(Random rng, double rate) {
        return -Math.log(1.0 - rng.nextDouble()) / rate;
    }

    private static double pct(List<Double> sorted, double p) {
        if (sorted.isEmpty()) {
            return 0;
        }
        return sorted.get((int) Math.min(sorted.size() - 1, Math.floor(p * sorted.size())));
    }

    private Simulation() {}
}
