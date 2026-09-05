"""Summarize completed local fixture experiments without claiming production performance."""

import json
import statistics
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "build/pipeline-experiment-python"))
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

OUT = ROOT / "docs/experiments/2026-09-06"
data = json.loads((OUT / "benchmark.json").read_text(encoding="utf-8"))
groups = {}
for case in data["results"]:
    groups.setdefault(case["label"], []).append(case)
summary = {}
for label, cases in groups.items():
    summary[label] = {
        "jobs": sum(c["count"] for c in cases),
        "runs": len(cases),
        "successes": sum(j["status"] == "COMPLETED" for c in cases for j in c["jobs"]),
    }
    for key in ["admission_ms", "queue_wait_ms", "e2e_ms"]:
        values = [c["metrics"][key]["p95"] for c in cases]
        summary[label][key] = {
            "median_run_p95": statistics.median(values),
            "min_run_p95": min(values),
            "max_run_p95": max(values),
        }
    summary[label]["throughput_jobs_per_second"] = statistics.median(
        c["throughput_jobs_per_second"] for c in cases
    )
(OUT / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
fig, axes = plt.subplots(1, 2, figsize=(11, 4.5), layout="constrained")
labels = ["async_c1", "stream_c1", "stream_c2"]
names = ["Existing async", "Stream / stage=1", "Stream / stage=2"]
colors = ["#68758b", "#16a6a1", "#326ee8"]
for ax, shape in zip(axes, ["steady", "burst"]):
    rows = [summary[label + "_" + shape] for label in labels]
    values = [r["e2e_ms"]["median_run_p95"] / 1000 for r in rows]
    lower = [v - r["e2e_ms"]["min_run_p95"] / 1000 for v, r in zip(values, rows)]
    upper = [r["e2e_ms"]["max_run_p95"] / 1000 - v for v, r in zip(values, rows)]
    ax.bar(names, values, color=colors, yerr=[lower, upper], capsize=5)
    ax.set_title("Steady: 1 request/s" if shape == "steady" else "Burst: 24 requests")
    ax.set_ylabel("End-to-end p95 (seconds)")
    ax.spines[["top", "right"]].set_visible(False)
    for i, v in enumerate(values):
        ax.text(i, v + upper[i] + max(values) * 0.025, f"{v:.2f}s", ha="center", fontsize=10)
fig.suptitle("Local synthetic AI experiment: median of 3 run p95s", fontsize=13)
fig.savefig(OUT / "comparison.png", dpi=180)
print(json.dumps(summary, indent=2))
