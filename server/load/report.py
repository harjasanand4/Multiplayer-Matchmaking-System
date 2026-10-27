#!/usr/bin/env python3
"""Snapshot what Prometheus recorded during a load test as a PNG (the dashboard's key panels).

    python3 load/report.py --prometheus http://localhost:9090 --start 1793061241 --end 1793061400 -o load/results/run.png

Needs matplotlib. Times are Unix seconds (k6 prints the run's start; `date +%s` before and after works).
"""

from __future__ import annotations

import argparse
import json
import urllib.parse
import urllib.request
from datetime import datetime, timezone

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402

SURFACE, INK, INK_2, MUTED, GRID = "#fcfcfb", "#0b0b0b", "#52514e", "#898781", "#e1e0d9"
SERIES = ["#2a78d6", "#eb6834", "#1baf7a"]

plt.rcParams.update({
    "figure.facecolor": SURFACE, "axes.facecolor": SURFACE, "savefig.facecolor": SURFACE,
    "axes.edgecolor": "#c3c2b7", "axes.labelcolor": INK_2, "axes.titlecolor": INK, "axes.titlesize": 10,
    "axes.titleweight": "bold", "axes.titlelocation": "left", "axes.spines.top": False,
    "axes.spines.right": False, "axes.grid": True, "grid.color": GRID, "grid.linewidth": 0.6,
    "xtick.color": MUTED, "ytick.color": MUTED, "xtick.labelcolor": INK_2, "ytick.labelcolor": INK_2,
    "xtick.labelsize": 8, "ytick.labelsize": 8, "legend.frameon": False, "legend.fontsize": 8,
    "legend.labelcolor": INK_2, "font.size": 9,
})


def query(base: str, expr: str, start: float, end: float, step: float) -> list[tuple[dict, list[float], list[float]]]:
    url = f"{base}/api/v1/query_range?" + urllib.parse.urlencode(
        {"query": expr, "start": start, "end": end, "step": step})
    data = json.load(urllib.request.urlopen(url, timeout=30))["data"]["result"]
    out = []
    for series in data:
        ts = [(float(t) - start) / 60 for t, _ in series["values"]]
        vs = [float(v) if v not in ("NaN", "+Inf", "-Inf") else float("nan") for _, v in series["values"]]
        out.append((series["metric"], ts, vs))
    return out


def panel(ax, base, start, end, step, title, lines, unit_scale=1.0, ylabel=""):
    n = 0
    for expr, label in lines:
        for metric, ts, vs in sorted(query(base, expr, start, end, step), key=lambda r: str(r[0])):
            name = label.format(**metric) if "{" in label else label
            ax.plot(ts, [v * unit_scale for v in vs], color=SERIES[n % len(SERIES)], lw=1.6, label=name)
            n += 1
    ax.set_title(title)
    ax.set_ylabel(ylabel)
    ax.set_ylim(bottom=0)
    ax.legend(loc="upper left")


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--prometheus", default="http://localhost:9090")
    ap.add_argument("--start", type=float, required=True)
    ap.add_argument("--end", type=float, required=True)
    ap.add_argument("--title", default="Load test")
    ap.add_argument("-o", "--out", default="load/results/run.png")
    args = ap.parse_args()
    step = max(5.0, (args.end - args.start) / 300)
    b, s, e = args.prometheus, args.start, args.end

    fig, axes = plt.subplots(2, 2, figsize=(11, 6.2), sharex=True)
    panel(axes[0][0], b, s, e, step, "Requests per second by route",
          [('sum by (route) (rate(omg_http_requests_total{route=~"/v1/.*"}[30s]))', "{route}")], ylabel="req/s")
    panel(axes[0][1], b, s, e, step, "API latency p99 by route",
          [('histogram_quantile(0.99, sum by (route, le) (rate(omg_http_request_duration_seconds_bucket'
            '{route=~"/v1/.*"}[30s])))', "{route}")], unit_scale=1000, ylabel="ms")
    panel(axes[1][0], b, s, e, step, "Time to match (what players feel)",
          [("histogram_quantile(0.50, sum by (le) (rate(omg_time_to_match_seconds_bucket[30s])))", "p50"),
           ("histogram_quantile(0.95, sum by (le) (rate(omg_time_to_match_seconds_bucket[30s])))", "p95"),
           ("histogram_quantile(0.99, sum by (le) (rate(omg_time_to_match_seconds_bucket[30s])))", "p99")],
          ylabel="seconds")
    panel(axes[1][1], b, s, e, step, "Players searching",
          [("sum(omg_queue_depth)", "all games"),
           ("sum(rate(omg_store_errors_total[30s])) * 10", "database errors x10 /s")])
    for ax in axes[1]:
        ax.set_xlabel("minutes")
    when = datetime.fromtimestamp(s, tz=timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    fig.suptitle(f"{args.title}  ({when})", x=0.06, ha="left", fontsize=12, fontweight="bold", color=INK)
    fig.tight_layout(rect=(0, 0, 1, 0.96))
    fig.savefig(args.out, dpi=140)
    print("wrote", args.out)


if __name__ == "__main__":
    main()
