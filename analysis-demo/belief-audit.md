# Belief Audit: What Would You Have to Believe to Invest?

The VC question is: what would you have to believe to invest in Hyperreal? The backwards-chained version is: given that I am already investing time, what do I actually believe — and does the evidence support those beliefs or reveal that I am wrong about something?

Three canaries provide external signal. V-money (VSAT) and V-people (UKRN) are now partially active. V-orgs (the FUTON stack) is the face where the feeling is furthest ahead of the evidence. This document audits the three beliefs, scores them against the I0–I4 maturity axis, and identifies the things to try and things to measure that would move the most uncertain belief toward evidence.

## Belief 1: The capability bank compounds

Each engagement should leave the stack richer than before, and future engagements should be faster or better as a result.

**Current maturity: I2 (evidence reaches the people who need it), approaching I3.**

**Evidence:**
- The VSAT Bayesian pipeline (Clay, EDN assumptions, forward sampling, SVG visualisation) was built for value-chain analysis.
- The same pipeline was applied to UKRN institutional evaluation in a single session, producing a population model over 16 NPT factors with gate/support aggregation.
- A structural innovation emerged (gate/support as distinct from weighted averaging) that was not present in the VSAT model.
- The working paper appendix was produced as a deliverable — the method IS the deliverable.
- The peripherals/invariants language appears in client-facing work (the UKRN working paper's Section 5).
- The capability is independently exercised in First Proof work (different domain, same stack).

**What's missing for I3 (feedback improves practice):**
The gate/support innovation emerged in a notebook. It is not yet a named, reusable pattern in the futon3 library. If it stays in the notebook, the compounding is anecdotal — it happened once, and next time someone would have to rediscover the approach. The promotion loop (work → candidate → operational) needs to complete for this to be structural compounding rather than personal memory.

**Things to try:**

| Action | What it tests | Invariant family |
| --- | --- | --- |
| Write gate/support aggregation as a futon3 equity flexiarg | Does the innovation survive formalisation? | Artifact custody (I3) |
| Apply gate/support to a third domain (e.g. First Proof problem selection) | Does the method generalise beyond two contexts? | Method validation (upstream) |
| Log the VSAT→UKRN transfer as a contribution-log entry | Does the promotion loop produce a visible record? | Repo role clarity (I3) |

**Things to measure:**

| Signal | Detected how | I-level |
| --- | --- | --- |
| Next engagement reuses the gate/support pattern by name | Grep the pattern library before starting | I3 |
| Time-to-first-model decreases across engagements | Compare VSAT (built over multiple sessions) vs UKRN (built in one) vs next | I2 |
| Innovation appears in a deliverable without being manually inserted | The client or paper uses the pattern because it's the obvious tool | I4 |

---

## Belief 2: The stack is governable without heroics

The invariants, patterns, and contribution protocols are legible enough that someone else could steward part of the system — or that you could pick it up after a long break without folklore.

**Current maturity: I1 (state transitions are valid), possibly I0.**

The stack works. The invariant inventory exists and names the gap. But there is no evidence that a second person can steward any part of it. The I3 and I4 invariant families are all candidate status: peripheral custody, budgeted action selection, archaeology control, cross-store agreement — none have operational exemplars.

**Evidence:**
- futon1a has 9 operational invariants (I0). The substrate is solid.
- futon3b and futon3c have 7 operational invariants (I1). Phase ordering, status discipline, existence, dependency satisfaction.
- The self-representing stack (invariant-model.edn, grand-unified-placemat.edn) makes the architecture inspectable.
- The tooling investments (codex-repl, claude-repl, Tickle, WS-backed IRC) are ahead of commercial offerings by weeks-to-months.

**What's missing for I2 (failures are visible):**
The tooling lead matters only if the *methodology of producing tools that compound* is capturable. If the lead depends on being personally ahead of Anthropic, it is a speed advantage. If it depends on the stack's promotion loop making each tool's design lessons available to the next tool, it is a structural advantage — a moat. The former collapses when someone faster arrives. The latter widens with each cycle.

**Things to try:**

| Action | What it tests | Invariant family |
| --- | --- | --- |
| Make one subsystem legible enough for a collaborator to modify | Governability without heroics | Human-visible inspectability (I2) |
| Leave the stack untouched for 2 weeks, then resume with only written documentation | Self-legibility after a break | Repo role clarity (I3) |
| Promote one I4 candidate to operational | Can the governance layer actually bite? | Peripheral custody (I4) |

**Things to measure:**

| Signal | Detected how | I-level |
| --- | --- | --- |
| A collaborator submits a meaningful change without real-time guidance | Contribution log entry with no synchronous pairing | I3 |
| Recovery time after a break < 1 hour to productive work | Time from cold start to first meaningful commit | I2 |
| An I4 candidate prevents a class of mess that previously recurred | Before/after comparison on the specific failure class | I4 |

---

## Belief 3: The network effect is real

A network of clients sharing structured accounts of their experience provides cross-context method validation. The architecture makes sharing easier than not sharing. Demonstrable impact across a wide network does not require charging for participation.

**Current maturity: I1 in the stack architecture, I0 empirically.**

The stack supports interoperable models (one JVM, no code duplication, shared EDN format). The VSAT→UKRN transfer demonstrates that the architecture handles multiple domains without forking. But no client has yet shared a structured account of their experience with another client through the architecture. The network effect is structural, not observed.

**Evidence:**
- The VSAT value-chains analysis shows services-first has highest ecosystem spillover.
- The UKRN population model shows that shared visibility across institutions is the lever that opens the most gates (SM and VI both require relational integration).
- The VSATLAS constellation architecture is designed for interconnected navigable accounts.
- Zero code duplication between VSAT and UKRN models (same deps.edn, same pipeline, different assumptions files).

**What's missing for I2 (evidence reaches the people who need it):**
The shared story surface does not exist as a deployed artifact. The VSATLAS architecture could serve this role, but it currently serves one client's storytelling needs, not a cross-context evaluation infrastructure. The generalisation from "one client's planetarium" to "a network's shared evidence surface" is the architectural move that would activate the inter-edge (X-vsatlatarium) across all three vertices.

**Things to try:**

| Action | What it tests | Invariant family |
| --- | --- | --- |
| Create a minimal shared-story instance with VSAT + UKRN as the first two entries | Does the architecture support cross-domain accounts? | Cross-store agreement (I4) |
| Invite a third party to navigate the constellation without guidance | Is the shared surface legible without mediation? | Human-visible inspectability (I2) |
| Publish the assumptions files (EDN) as open data alongside the working papers | Does openness generate inbound interest without sales effort? | Published knowledge (downstream) |

**Things to measure:**

| Signal | Detected how | I-level |
| --- | --- | --- |
| A third party references a pattern or method from the shared surface | Citation or reuse in their own work | I2 |
| A new domain is modelled using the shared pipeline without Hyperreal involvement | Fork or adaptation of the EDN + Clay pipeline | I3 |
| Cross-context stories accumulate without active curation | Growth in the constellation that isn't directly authored by you | I4 |

---

## The shared bottleneck: the promotion loop

All three beliefs converge on the same mechanism. Belief 1 needs innovations to be promoted to named patterns. Belief 2 needs governance instruments to be promoted to operational status. Belief 3 needs the shared surface to be promoted from an architectural possibility to a deployed artifact.

The promotion loop — work → cleanup evidence → candidate → promotion → operational → prevented failures → cleaner work — is the equity loop for V-orgs. It is the same structure as the Bayesian update cycle in the UKRN model (prior → measurement → posterior → next design) and the same structure as the capability-growth feedback loop in the value model (capability → alignment → revenue → time → capability).

The efficient frontier for time investment is to push the least mature belief toward evidence. Currently:
- Belief 1 is at I2, approaching I3. The next action is small: write the gate/support flexiarg.
- Belief 2 is at I1. The next action is harder: make one subsystem legible.
- Belief 3 is at I0 empirically. The next action is structural: deploy the shared surface.

The variegated phase plane — wandering between client work, day job, math, and stack infrastructure — is not a distraction from the promotion loop. It is the phase plane the promotion loop operates on. Each completed mission is a work→cleanup cycle. The question is whether the cleanup evidence is being captured and promoted, or just experienced and forgotten.

## Sources

Invariant families and I-levels from `futon7/docs/futon-stack-invariant-model.edn`. Investment thesis framing from `futon7/analysis-demo/value-model.md`. UKRN population model from `vsat.wiki/ukrn-demo/assumptions-v2.edn`. Krowne prism and inter-edges from `futon7/docs/grand-unified-placemat.edn`.
