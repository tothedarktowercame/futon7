# Mission: Daily GitHub Scan Brief

**Date:** 2026-04-13
**Status:** IDENTIFY
**Owner:** Joe + agent (futon7)
**Cross-ref:** M-f7-lead-report (predecessor, COMPLETE),
  M-war-machine (consumer — daily scan is a frame derivative),
  frame-schema.edn (the scan brief is a `:github-scan-brief` derivative event)

## 1. IDENTIFY

### Motivation

The futon stack has a consulting-shaped hole. M-f7-lead-report produced
a one-shot landscape report from cached GitHub probe data, but it ran
once and hasn't been repeated. The probes found ~1000 repos with
commercial signals, but the data is stale (2026-02-24) and Joe hasn't
acted on it.

The deficit isn't information — it's cadence. A daily brief that takes
5 minutes to read would:
1. Keep the consulting pipeline visible (not buried in EDN files)
2. Surface time-sensitive opportunities (new funding rounds, job posts)
3. Provide a heartbeat signal for the War Machine frame
4. Move the `:depositing` cardinal direction from 0.0 to >0

### Two-level accounting

**Level 1 (this mission):** Build a daily scan that produces a brief.
Concrete deliverable: a markdown file or Emacs buffer that Joe reads
each morning alongside the pocketwatch.

**Level 2 (War Machine frame derivative):** Each daily scan brief is a
`:github-scan-brief` derivative event. It changes the frame:

| Frame field | Before | After |
|------------|--------|-------|
| `:loop-health` | 0.6 | +0.05 (new arrow firing) |
| `:consulting-pct` | 0.0 | >0 (futon7 activity) |
| `:depositing` cardinal | 0.0 | >0 |
| `:futon7-business-model` port | :missing | :partial |
| `SORRY-market-interface` | :no-movement | :movement |
| `:income-deadline` status | :no-pipeline | :scanning |

The scan brief is also a proof of concept for how locally-interesting
data flows through the system:
- futon7 produces the brief (local)
- The brief is logged as evidence in futon1a (persistence)
- The War Machine frame reads the evidence (strategic awareness)
- The frame's changed `:depositing` value is visible in the hex
  visualiser and in WebArxana (if frames become viewable entities)

### Scope in

- Daily probe of GitHub API for categories from M-f7-lead-report
- Delta detection: what changed since yesterday (new repos, new
  signals, repos that gained stars/forks/funding)
- Brief generation: top 3-5 items worth Joe's attention
- Delivery surface: Emacs buffer via `M-x f7-daily-brief`, and/or
  a futon1a evidence entry
- Evidence emission: each brief becomes a `:github-scan-brief`
  evidence entry queryable by the War Machine

### Scope out

- Acting on the leads (that's Joe's job, not the scan's)
- Full re-probe of all categories (incremental delta, not full scan)
- Automated outreach (the brief informs, Joe decides)
- Building a CRM (futon7 is not a CRM — it's a probe + brief system)

### Completion criteria

1. A daily scan runs and produces a readable brief
2. The brief surfaces at least 1 new or changed item per day
3. Each brief is logged as evidence in futon1a
4. The War Machine frame shows futon7 health > :dormant when
   briefs are being produced
5. Joe reads the brief at least 3 days in a row (the cadence test)

### Relationship to other missions

| Mission | Relationship |
|---------|-------------|
| M-f7-lead-report | Predecessor — one-shot version of what this makes recurring |
| M-war-machine | Consumer — scan brief is a frame derivative event |
| M-webarxana | Potential viewer — briefs could be WebArxana entities |
| frame-schema.edn | Schema — defines how the derivative shows up |
| SORRY-market-interface | Sorry this addresses — scanning IS market interface work |

### Source material

- `futon7/src/` — existing probe code from M-f7-lead-report
- `futon7/analysis-demo/` — cached probe results
- `futon5a/data/frame-derivatives-catalogue.edn` — derivative definition
- `futon5a/data/frame-schema.edn` — frame schema for Level 2 accounting

### Exit criterion for IDENTIFY

A human has read this proposal and agrees that daily scanning is the
right cadence, and that the two-level accounting (local brief + frame
derivative) is the right architecture.
