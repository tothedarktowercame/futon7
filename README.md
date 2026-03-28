# futon7 — boundary layer

futon7 is the Markov blanket of the FUTON stack: the layer where the stack's internal capability meets the external world. It has two functions.

**Sensing.** The `holes/` directory and `provisional-ledger.md` study adjacent systems (BMAD, TeleClaude, Agent-o-rama, Gas Town) to understand the context the stack operates in and where it has something distinctive to contribute. This is comparative intelligence, not brand defence.

**Modelling.** The `docs/` directory holds four EDN hypergraphs that model the stack's value structure:

- `grand-unified-placemat.edn` — the Krowne Prism: three faces of sustainability (people, money, organisations) extruded along an I0–I4 maturity axis, with typed inter-edges connecting the faces.
- `vsat-value-model.edn` — the fiscal-sustainability face: actors, action arenas, invariants, and honest questions for the VSAT client relationship.
- `ukrns-value-model.edn` — the participatory-sustainability face: the same structure applied to UKRNS training evaluation.
- `futon-stack-invariant-model.edn` — the organisational-sustainability face: repos, invariant families, the promotion loop.

These are static hypergraphs. They describe what the value structure *is*, not how it behaves under uncertainty.

**The Bayesian value-flow model** in `analysis-demo/` adds the dynamic layer. It reads a ten-node value network (`assumptions.edn`) derived from the placemat, forward-samples from Bayesian priors (Poisson on engagement volume, Gamma on flow weights), and produces distributional estimates of capability growth and revenue under three scenarios. Invoice data (`invoices.edn`) maps real billable hours to edge activations, providing observations for future posterior inference.

The key framing: capability growth is the objective function; revenue is a catalytic constraint. The model tracks whether paid work also grows the capability bank (high alignment) or merely earns income (low alignment). The alignment ratio — not the revenue total — is the diagnostic that matters.

The model is designed to sharpen as evidence accumulates. Each invoice is an observation. When enough arrive, the prior predictive model can be handed to a posterior inference engine to update beliefs about which edges are actually carrying value and how the capability-growth cycle is closing.

## Structure

```
futon7/
  docs/                    # Static hypergraph models (EDN)
  holes/                   # Adjacent-system comparative notes
  analysis-demo/           # Bayesian value-flow model
    assumptions.edn        # Value network + scenario parameterisations
    invoices.edn           # Invoice data as edge-activation observations
    notebooks/             # Clojure/Clay analysis pipeline
    images/                # Generated SVG figures
    value-model.md         # Contextualising document
  provisional-ledger.md    # Repo role and adjacent-system protocol
```
