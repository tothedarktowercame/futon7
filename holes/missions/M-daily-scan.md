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

The deficit isn't information — it's cadence. Joe's stated intention is
to work **20 hours a month** at consulting and earn enough to not worry
about money. This is half serious — but it becomes testable by
committing to **1 hour per day, each weekday, for 20 working days**
doing scan + workup, then evaluating whether the 20h/month estimate
holds.

A daily brief that takes 5 minutes to read plus 55 minutes of workup:
1. Keep the consulting pipeline visible (not buried in EDN files)
2. Surface time-sensitive opportunities (new funding rounds, job posts)
3. Provide a heartbeat signal for the War Machine frame
4. Move the `:depositing` cardinal direction from 0.0 to >0
5. Test the 20h/month hypothesis empirically over 4 weeks

### Joe's demonstrable capacities (scan context)

The scan isn't cold outreach — it's backed by capabilities that are
plausibly client-relevant:

- **Bayesian modelling:** Logic models, beta-binomial posteriors,
  training evaluation frameworks (cf. UKRN-S delivery, futon5a)
- **Pattern languages:** 853 patterns catalogued, flexiarg format
  for structured argumentation (cf. vsatelier, futon3)
- **Hypergraph tooling:** WebArxana, Arxana Browser, XTDB-backed
  knowledge management (cf. futon4, futon1a)
- **AI integration:** Multi-agent coordination, Claude/Codex wiring,
  evidence-based development methodology (cf. futon3c)
- **Training delivery:** UKRN-S programme delivery, logic model
  methodology, reproducibility training (cf. ~/vsat/, ~/vsat.wiki/)

These capacities can generate responses to landscape signals:
a scan might surface a project needing evaluation methodology →
Joe can generate a Bayesian model or flexiarg as a demonstration
of fit. The scan + workup hour should include time for this kind
of targeted response.

### Relevant sources

- `~/vsat/` — current paid consulting work (VSAT Ltd)
- `~/vsat.wiki/` — ideation and methodology for consulting
- `~/vsat.wiki/ukrn-demo/` — developing Working Paper for UKRN/UKRN-S
  (uncommitted files — a capacity demonstration in progress)
- `futon7/analysis-demo/` — cached probe results from Feb 2026
- `futon7/src/` — probe code from M-f7-lead-report

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

1. A daily scan runs and produces a readable brief (≤5 min to read)
2. The brief surfaces at least 1 new or changed item per day
3. Each brief is logged as evidence in futon1a
4. The War Machine frame shows futon7 health > :dormant when
   briefs are being produced
5. Joe does scan + workup for 1 hour/day for 20 weekdays
6. At day 20, evaluate: did 20h of scan+workup produce at least
   one concrete consulting conversation? If yes, the 20h/month
   hypothesis has evidence. If no, the approach needs revision.
7. At least 3 workups include a targeted response (Bayesian model,
   flexiarg, or capability demo) sent or prepared for a prospect

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

### Pilot role: War Machine assemblage upgrade

This mission serves as a pilot for how locally-interesting data flows
through the entire system. The success path:

1. **futon7** produces the daily brief (local work)
2. **futon1a** stores each brief as evidence (persistence)
3. **futon5a** frame schema registers the derivative event
4. **futon0** War Machine reads the evidence (strategic awareness)
5. **futon4** WebArxana can display briefs as entities (navigation)

If this works end-to-end, it demonstrates that a new data source
(daily scan) can be added to the War Machine without modifying the
War Machine itself — the frame schema's port system handles it.

This is also a test of the cardinal direction model: daily scanning
should rotate the mana vector from pure hermit! toward depositing!.
At the end of 20 days, the frame should show a measurably different
cardinal direction profile compared to the two retrospective frames
(faa3ea73 and 14459c97, both ≈0% depositing).

### Exit criterion for IDENTIFY

A human has read this proposal and agrees that daily scanning is the
right cadence, the 20-day test is the right evaluation frame, and
the two-level accounting (local brief + frame derivative) is the
right architecture.

## 2. MAP

### Q1: What probe infrastructure already exists?

| Component | Status | Reuse? |
|-----------|--------|--------|
| `f7/probes.clj` — 10 probe categories (3 tiers) | Operational | Yes — reuse as-is for initial scans |
| `f7/gh.clj` — GitHub API wrapper with EDN cache | Operational | Yes — `gh` CLI authenticated, cache layer ready |
| `f7/signals.clj` — 6 signal extractors, scoring | Operational | Yes — scoring function is pure |
| `f7/report.clj` — Markdown report with lead tiers | Operational (not run) | Adapt — needs delta mode (what changed since yesterday) |
| `f7/core.clj` — Orchestrator: run → density → intersect → gaps | Operational | Adapt — needs incremental/delta variant |
| `bb.edn` — `bb lead-report` task | Operational | Extend — add `bb daily-scan` task |
| `analysis-demo/` — value-flow model, invoices | Stale (2025-03) | Reference only — separate pipeline |
| `data/cache/` — GitHub API cache | Empty | Will populate on first run |

**Verdict:** The probe infrastructure is operational and can run today.
The main gap is *delta detection* — the existing code does full scans,
not incremental "what changed since yesterday."

### Q2: GitHub API rate limits for daily use

- Search API: 30 requests/min, 1000 results/search
- Core API: 5000 requests/hour (authenticated)
- 10 probes × ~3 API calls each = ~30 calls per scan
- Cache layer means repeated runs within a day are free
- **Verdict:** Daily scanning is well within rate limits

### Q3: What does the daily brief need to contain?

A 5-minute-readable brief, produced each weekday:

1. **Delta summary** — new repos since last scan, repos with changed
   signals (gained funding, license change, new enterprise keywords)
2. **Top 3 items** — highest-priority items worth Joe's attention today
3. **Hot lead update** — any movement in previously identified hot/warm leads
4. **Capacity match** — for the top items, which of Joe's capacities
   align (Bayesian modelling, pattern languages, hypergraph tooling, etc.)
5. **Workup suggestion** — what targeted response could Joe prepare in
   the remaining 55 minutes of the daily hour

### Q4: How does evidence emission work?

The brief should be emitted as a futon1a evidence entry:
```
{:evidence/type "scan-brief"
 :evidence/claim-type "observation"
 :evidence/author "joe"
 :evidence/session-id <session-id>
 :evidence/body {:date "2026-04-14"
                 :items-scanned N
                 :new-items N
                 :hot-leads [...]
                 :workup-done? bool}}
```

This can be done via `curl POST` to futon1a's evidence endpoint,
or via the existing `arxana-store-emit-evidence!` in Emacs.

### Q5: Structural dependency on M-war-machine

M-war-machine is not just a *consumer* of scan data. It is a
*supplier* of interpretive structure to M-daily-scan:

| War Machine supplies | Daily Scan uses it for |
|---------------------|----------------------|
| Cardinal directions (foraging/cargo/depositing/hermit) | Classifying each day's work: was today depositing or foraging? |
| Frame derivative schema | Logging the scan as a typed event with known effects |
| Mana accounting model | Evaluating whether the 20h investment is "paying off" |
| Hobbes response (coordination without sovereignty) | The scan IS nomadic practice — it responds to signals, not commands |
| Sorry topology | Connecting scan items to SORRY-market-interface |

M-daily-scan supplies back:
| Daily Scan supplies | War Machine uses it for |
|--------------------|----------------------|
| Actual depositing data | Moving the :depositing cardinal from 0 |
| Pipeline status | Updating :income-deadline constraint from :no-pipeline |
| futon7 hive health | Changing futon7 from :dormant to :active |
| Evidence entries | Loop health — a new arrow fires |

**This bidirectional dependency is itself a pattern:** the pattern
of *mutual constitution* where infrastructure and practice co-produce
each other. Without the War Machine's framework, the scan is just a
cron job. Without the scan's data, the War Machine's frame has a
zero-valued port.

### Q6: Publication as consulting surface

The briefs should be **published** on the Hyperreal Enterprises
website (futon7a, `hyperreal.enterprises` or equivalent) as blog
posts, not just stored as EDN. Reasons:

1. **Public signal:** Demonstrates capability to prospects. "Here's
   what I found interesting in knowledge graphs / AI tooling /
   reproducibility today" is a natural consulting-adjacent format.
2. **Content trail:** Builds a searchable archive that prospects
   can find via search engines.
3. **Grant→pitch transfer:** Joe's grant writing experience (UKRN,
   EPSRC, etc.) is directly transferable. "This grant proposal
   could have been a consulting pitch" — the skill is framing
   capability in terms of the recipient's problem. The daily brief
   is practice for this skill.
4. **Accountability:** Publishing forces quality — you don't publish
   a brief that's just a list of repos. You write a sentence about
   why each item matters.

**Format:** Short blog post (200-400 words) with:
- 3-5 items from the scan
- For each: what it is, why it matters, what Joe could offer
- A "capacity note" linking to relevant demonstrations (UKRN-S
  delivery, vsatelier, WebArxana, etc.)

**Infrastructure:** futon7a is cloned at `~/code/futon7a`. It's a
flat HTML site (19 pages, no blog structure). A `blog/` directory
with an index and dated post files would be the simplest addition.
Posts can be static HTML generated from the brief markdown.

### Q7: Existing consulting artefacts

| Artefact | Location | Relevance |
|----------|----------|-----------|
| VSAT consulting work | ~/vsat/ | Current paid work — demonstrates delivery |
| VSAT wiki / ideation | ~/vsat.wiki/ | Methodology, patterns, demos |
| UKRN-S Working Paper | ~/vsat.wiki/ukrn-demo/ | Capability demo in progress (uncommitted) |
| Hyperreal website | futon7a (NOT on this machine) | Publication surface for briefs |
| Lead report (M-f7-lead-report) | futon7/analysis-demo/ | One-shot version — reference for format |
| Invoice ledger | futon7/analysis-demo/invoices.edn | Revenue history — baseline for 20h/month test |
| Value-flow model | futon7/analysis-demo/value-model.md | Hyperreal business model analysis |

### Ready vs Missing

| Component | Status | Work needed |
|-----------|--------|-------------|
| Probe categories | Ready | None |
| GitHub API wrapper | Ready | None |
| Signal extraction | Ready | None |
| Full-scan orchestrator | Ready | None |
| Cache layer | Ready | None |
| Delta detection | Missing | Compare today's results to yesterday's cache |
| Daily brief renderer | Missing | Markdown template with delta, top-3, capacity match |
| Blog post formatter | Missing | Convert brief to publishable post |
| `bb daily-scan` task | Missing | Wire delta + brief into bb task |
| Evidence emission | Missing | POST to futon1a after each brief |
| Emacs integration | Missing | `M-x f7-daily-brief` command |
| futon7a (website repo) | Cloned at ~/code/futon7a | Flat HTML site (19 pages), no blog dir yet — create one |
| 20-day tracking | Missing | Simple log of days completed + outcomes |
