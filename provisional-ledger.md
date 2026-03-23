F7 Protocol for Adjacent-System Analysis

Working note on repo role

At the moment, `futon7` is better understood as an excursion-heavy layer
than as a mission-complete layer. Its job is not to prove that FUTON is
"best at everything" or to collapse external systems into FUTON terms.
Its job is to study adjacent systems honestly enough to understand the
context FUTON is operating in, what pressures are real in the world, and
where FUTON may have something distinctive to contribute. That makes F7 a
comparative and contextual layer, not just a survey graveyard and not a
brand-defense exercise.

Boundary note: public F7 vs private-adjacent F5a

Some of the most important F7 evidence may arise in live client or
commercial work where FUTON methods are tested under real external
constraints. That material should not be forced into public `futon7` if
it depends on private relationships, unpublished project details, or
confidential deliverables. A useful distinction is:

- `futon7`: public adjacent-system and landscape intelligence notes
- `futon5a`: private applied practice, including client-facing,
  consulting, and confidential transfer contexts

One especially important private-adjacent category is the
**reciprocal proof of concept**: an external project may be framed by
the client as a proof of concept, while simultaneously functioning as
evidence that FUTON methods themselves can support paid work in ordinary
technical settings. For example, if flexiarg-driven development works in
a conventional TypeScript consulting context, that is not just delivery
success for the client project; it is also contextual evidence that
FUTON methods can transfer outside the stack.

Use one writeup per external system, with the same headings every time.

1. System identity
What does it say it is, in its own terms? Quote or paraphrase the repo’s self-description as the baseline. This avoids importing FUTON categories too early. For BMAD, that baseline is agile AI-driven development with structured expert workflows. For TeleClaude, it is distributed intelligence / agent nervous system / universal lobby.

2. Primary problem claim
What deficit in the world is it reacting to?
BMAD reacts against unstructured AI usage that “does the thinking for you,” and proposes guided structured collaboration instead.
TeleClaude reacts against fragmented context across machines, agents, and sessions.

3. Unit of coordination
What is the basic thing being coordinated?
For BMAD, it looks like workflows, roles, and project-phase guidance.
For TeleClaude, it looks like sessions, machines, identities, and deterministic phase transitions.

4. Memory model
How does the system remember?
Does it rely on prompts, files, registries, state machines, event logs, durable storage, or post-processing?
TeleClaude is explicit here: work state in state.yaml, identity-scoped memory, session post-processing, and long-running maintenance jobs.
For BMAD, the emphasis in the visible repo text is more on structured guidance and modular workflows than on a strong explicit memory substrate.

5. Control logic
How does it decide what happens next?
BMAD appears to use guided workflow structure and role-based prompting, with /bmad-help as a procedural steering surface.
TeleClaude appears closer to a deterministic orchestration model with named phase transitions such as Prepare → Build → Review → Fix → Finalize → Demo.

6. Evidence discipline
What counts as proof that work happened correctly?
Look for tests, gates, post-processing, logs, artifacts, review stages, and explicit done criteria.
TeleClaude explicitly describes Definition of Ready / Definition of Done gates and post-session metabolization of work.
BMAD explicitly emphasizes structured workflows and lifecycle coverage, but from the repo front page alone its evidence model is less foregrounded than its process model.

7. Human role
Is the human operator a supervisor, collaborator, consumer, or fallback authority?
BMAD presents AI as expert collaborator in structured partnership with the user.
TeleClaude presents a stronger governed-collective model, but still keeps human escalation and role-gated access in view.

8. Adjacency to FUTON
Which FUTON layer or concern does it resemble?
Do not ask whether it “matches FUTON.” Ask which local resemblance is strongest.

9. Non-equivalence
State clearly what the external system does that FUTON is not trying to do, or what FUTON does that is not visible there. This keeps the comparison from collapsing into brand warfare.

10. Reusable insight
Extract one thing FUTON could learn from it, and one thing it clarifies about FUTON by contrast.

A compact scoring frame

For quick comparison, each system can get a 0–3 note on:

workflow structure

durable memory

deterministic orchestration

explicit evidence/gates

multi-agent coordination

cross-machine continuity

transfer/generalization

contextual framing

That would give F7 a repeatable comparative surface.

Provisional writeup: BMAD-METHOD

System identity
BMAD presents itself as an open-source, agile, AI-driven development framework with facilitated workflows, specialized agent roles, adaptive planning depth, and end-to-end lifecycle coverage.

Primary problem claim
Its stated problem is that ordinary AI tools produce mediocre results by “doing the thinking for you.” Its answer is structured collaboration that elicits better human thinking.

Strongest adjacency to FUTON
The nearest overlap seems to be F3/F5 territory: structured workflow, role differentiation, and explicit procedural scaffolding around development activity. It also has some resonance with your interest in pattern-mediated coordination, though BMAD’s public language is more workflow-and-agent oriented than pattern-and-evidence oriented. This last sentence is my inference from the repo description, not a direct repo claim.

Likely contrast with FUTON
BMAD appears to foreground guided delivery flow. FUTON, as you’ve described it here, is trying to do something more reflexive: not just coordinate work, but preserve evidence, compare aspiration with actuality, and support a self-argument about development. That contrast is partly an inference from your FUTON sketch plus BMAD’s repo front page.

What it is useful for in F7
BMAD gives you a nearby example of a system that says: structured AI development needs explicit roles, phases, and help surfaces. That helps justify the contextual necessity of at least part of FUTON’s coordination thesis.

Provisional writeup: TeleClaude

System identity
TeleClaude presents itself as a distributed agent coordination system spanning machines, devices, and models, with a persistent state model and a governed collective of agents.

Primary problem claim
Its stated problem is fragmentation of context across machines and agent silos. Its intervention is a “universal lobby” that unifies access, memory, and execution flow.

Strongest adjacency to FUTON
The nearest overlap seems to be F0/F1/F3 territory: control surface, durable state, session continuity, deterministic workflow, and live coordination. The explicit phase machine and work-state handling make it especially relevant for comparison with any FUTON layer concerned with orchestration and mission flow.

Likely contrast with FUTON
TeleClaude appears to foreground operational continuity and governed agent collectives. FUTON, at least in your sketch, is aiming beyond continuity into reflexive evidence, pattern transfer, and self-argument. Again, that contrast is an inference, not a claim from the TeleClaude repo.

What it is useful for in F7
TeleClaude strengthens the contextual case that cross-session, cross-machine, multi-agent continuity is not an imaginary niche. Someone else is independently building around that same pressure.

Provisional writeup: Agent-o-rama

System identity
Agent-o-rama presents itself as an end-to-end LLM agent platform for
building, tracing, testing, and monitoring agents, with integrated
storage and deployment on the JVM. It is explicit that evaluation and
observability are first-class, not afterthoughts.

Primary problem claim
Its stated problem is that LLM applications are inherently unpredictable
and therefore require stronger testing, tracing, telemetry, and
observability than ordinary "just wire up a prompt" approaches provide.

Strongest adjacency to FUTON
The nearest overlap seems to be F1/F3/F6 territory: explicit agent
graphs, durable execution traces, evaluation artifacts, human input
during execution, and the idea that agent work should be inspectable
rather than magical. It is especially relevant as a comparison point for
stack concerns around evidence, observability, and recoverable work
history.

Likely contrast with FUTON
Agent-o-rama appears to foreground agent engineering, runtime telemetry,
and production evaluation on top of a strong JVM platform substrate.
FUTON, as described across the stack, is aiming at something more
reflexive and cross-domain: missions, patterns, capability growth,
personal/stack reporting, and self-representation. That contrast is an
inference from the repo description plus your stack framing, not a claim
made by Agent-o-rama itself.

What it is useful for in F7
Agent-o-rama is a useful reminder that "agent observability" is not a
niche concern invented inside FUTON. It gives a nearby example of a
system that treats traces, experiments, online evaluation, and telemetry
as part of the core surface. That helps clarify where FUTON may have
parallel concerns, and also where FUTON is trying to go beyond runtime
observability into broader evidence, interpretation, and stack-level
self-description.

Provisional writeup: Gas Town

System identity
Gas Town presents itself as a multi-agent orchestration system for Claude
Code with persistent work tracking. Its README foregrounds a workspace
manager model with a "Mayor" coordinator, rigs, worker identities,
git-worktree-backed hooks, and Beads-based work state.

Primary problem claim
Its stated problem is that multi-agent coding becomes chaotic when agents
lose context, coordination is manual, and work state lives only in
session memory. Its answer is persistent state, named roles, handoffs,
and a workspace structure intended to scale agent swarms more reliably.

Strongest adjacency to FUTON
The nearest overlap seems to be F0/F1/F3 territory: multi-agent
coordination, durable work state, handoffs, named roles, and explicit
workspace structure. It is especially relevant for comparison with
FUTON's concerns around session continuity, controlled adjacent action
surfaces, and the difference between productive orchestration and
attention-fragmenting supervision.

Likely contrast with FUTON
Gas Town appears to foreground high-throughput coding orchestration and
persistent work tracking around Claude Code itself. FUTON, by contrast,
is trying to do something broader and more reflexive: mission structure,
evidence-bearing work, capability growth, and self-representation across
the stack rather than only scaling agent coding throughput. That
contrast is an inference from the repo description plus your FUTON
framing, not a claim made by Gas Town itself.

What it is useful for in F7
Gas Town is worth studying because it is a serious nearby example of
workspace-scale agent orchestration rather than a toy prompt recipe. It
may also be worth using experimentally, but with caution: its value for
F7 is not only whether it is fast, but whether its coordination model
helps or harms human legibility, recoverability, and bounded attention.

A writeup template you can reuse

You could standardize each F7 note like this:

F7 Adjacent-System Note: [name]
Self-description:
Problem it thinks exists:
What it coordinates:
Memory model:
Control model:
Evidence model:
Human role:
Nearest FUTON layer(s):
Key parallel:
Key non-parallel:
What FUTON can learn from it:
What it helps justify about FUTON’s existence:

The key F7 discipline

The main thing is to avoid two bad moves:

One is “they are doing the same thing as FUTON.”
The other is “they are irrelevant because they are not FUTON.”

The useful middle is:
they illuminate a local niche, pressure, or technique that helps explain why some part of FUTON is not arbitrary.

That is exactly what a good literature layer does.

A next good step would be to turn BMAD and TeleClaude into the first two entries of a small F7 comparative ledger.

That next step can now be generalized slightly: BMAD, TeleClaude, and
Agent-o-rama, and Gas Town are enough to justify F7 as a live
comparative ledger for adjacent systems rather than a one-off note.
