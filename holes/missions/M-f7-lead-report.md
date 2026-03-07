# M-f7-lead-report

## Status: COMPLETE

## Goal

Generate a human-readable landscape report from existing futon7 probe data,
identifying concrete consulting leads where the futon stack's capabilities
align with projects showing commercial signals.

## Context

futon7 has ~1000 cached GitHub API results and a complete probe run from
2026-02-24. The data includes commercial signal extraction (enterprise
keywords, FUNDING.yml, licenses, fork ratios) across 10 probe categories
spanning neighborhood, domain, and adjacent spaces.

The missing piece: a report that a human can read and act on. The probe data
is raw EDN — useful for analysis but not for decision-making.

## Approach

1. Parse the existing stack-grounded-run.edn
2. Score repos by alignment with futon stack capabilities
3. Generate a markdown report with:
   - Top leads by category (neighborhood/domain/adjacent)
   - For each lead: what they do, what signals they show, why futon fits
   - Gap opportunities (high adoption, low commercial extraction)
4. The report itself demonstrates pattern discipline (this mission has PSR/PUR)

## Futon Stack Alignment Criteria

Projects score higher if they touch areas where the futon stack has working code:
- Knowledge graphs / entity resolution (futon1a, futon3a)
- Pattern-based reasoning / rule systems (futon3)
- Multi-agent coordination (futon3c)
- Mathematical content / formal methods (futon5, futon6)
- Developer workflow / code intelligence (futon4, futon7)
- Annotation / provenance / transparency (futon7/Gravpad vision)

## Deliverables

- `src/f7/report.clj` — report generation from probe data
- `data/results/lead-report.md` — the generated report
- This mission file updated with outcome

## PSR

- Pattern chosen: construct-an-explicit-witness (math-informal)
- Candidates: construct-an-explicit-witness, reduce-to-known-result
- Rationale: We need a concrete artifact (the report) to witness that
  landscape intelligence produces actionable leads. An existence proof
  for the value of the probe infrastructure.

## PUR

- Pattern: construct-an-explicit-witness (math-informal)
- Actions taken: Built `src/f7/report.clj` with stack-alignment scoring
  (keyword matching against 28 futon capability keywords across 7 areas),
  lead classification (hot/warm/aligned/cold), and markdown report generation.
  Added `bb lead-report` task.
- Outcome: success
- Prediction error: low — straightforward data transformation.
  The alignment scoring is crude (keyword matching) but sufficient
  to surface genuine leads.
- Notes: 293 repos analyzed. 4 hot leads, 37 warm, 43 aligned-but-uncommercial.
  Most promising areas: multi-agent coordination (highest commercial density
  in aligned space), Clojure ecosystem (native stack), formal methods (niche
  but aligned). Notable leads: datahike, code-maat, rocq-of-rust, semantica.

## Observations

The report reveals the landscape structure:
- **Multi-agent coordination** dominates hot leads — this is where commercial
  money flows and the futon stack has real capabilities (futon3c).
- **Clojure commercial** shows the native ecosystem — babashka, datahike,
  metabase, code-maat are all warm leads with high stack alignment.
- **Formal methods** is niche but strongly aligned with futon5/futon6 work.
- **Annotation/transparency** aligns with the Gravpad vision but has fewer
  clear commercial players.

The gap analysis shows huge demand for multi-agent frameworks (autogen 55K
stars, swarm 21K) with no commercial extraction — potential consulting
opportunity in helping teams operationalize these tools.
