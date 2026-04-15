---
theme: default
title: Agentic Integration Blueprint
info: |
  How yugastore-java uses agentic configuration to help teams ship faster together.
class: lead
transition: fade
drawings:
  persist: false
duration: 25min
mdc: true
---

<style>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700&family=IBM+Plex+Mono:wght@400;500&display=swap');

:root {
  --bg-a: #081421;
  --bg-b: #12324a;
  --bg-c: #1d4f63;
  --ink: #f4f7fb;
  --muted: #b4c5d2;
  --accent: #f59e0b;
  --accent-2: #2dd4bf;
  --danger: #fb7185;
  --panel: rgba(7, 23, 36, 0.62);
  --stroke: rgba(180, 197, 210, 0.24);
}

.slidev-layout {
  font-family: 'Space Grotesk', sans-serif;
  color: var(--ink);
  background:
    radial-gradient(1200px 500px at 15% -10%, rgba(245, 158, 11, 0.18), transparent 55%),
    radial-gradient(900px 450px at 90% 0%, rgba(45, 212, 191, 0.18), transparent 60%),
    linear-gradient(145deg, var(--bg-a), var(--bg-b) 45%, var(--bg-c));
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  padding: clamp(0.6rem, 2.5vw, 1.5rem);
}

h1, h2, h3 {
  letter-spacing: 0.02em;
  line-height: 1.1;
  margin: 0 0 clamp(0.1rem, 0.4vw, 0.2rem) 0;
  font-size: clamp(1.2rem, 4vw, 2.5rem);
}

p, li, td, th {
  color: var(--ink);
  margin: clamp(0.1rem, 0.3vw, 0.2rem) 0;
  font-size: clamp(0.8rem, 1.8vw, 1.1rem);
}

.small, .muted {
  color: var(--muted);
  font-size: clamp(0.7rem, 1.5vw, 1rem);
}

.mono {
  font-family: 'IBM Plex Mono', monospace;
}

.panel {
  background: var(--panel);
  border: 1px solid var(--stroke);
  border-radius: clamp(10px, 1.5vw, 20px);
  padding: clamp(0.5rem, 1vw, 0.9rem) clamp(0.7rem, 1.3vw, 1.1rem);
  backdrop-filter: blur(3px);
  margin: clamp(0.15rem, 0.3vw, 0.25rem) 0;
}

.grid2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: clamp(0.3rem, 0.7vw, 0.5rem);
  margin: clamp(0.15rem, 0.3vw, 0.25rem) 0;
}

.grid3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: clamp(0.25rem, 0.6vw, 0.45rem);
  margin: clamp(0.15rem, 0.3vw, 0.25rem) 0;
}

.kpi {
  font-size: clamp(1.4rem, 4.5vw, 2.2rem);
  font-weight: 700;
  color: var(--accent);
  margin: 0;
}

.tag {
  display: inline-block;
  border: 1px solid var(--stroke);
  border-radius: 999px;
  padding: clamp(0.1rem, 0.25vw, 0.2rem) clamp(0.4rem, 0.9vw, 0.7rem);
  margin-right: clamp(0.2rem, 0.4vw, 0.3rem);
  margin-bottom: clamp(0.1rem, 0.25vw, 0.2rem);
  font-size: clamp(0.65rem, 1.3vw, 0.9rem);
  color: var(--muted);
}

.flow {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: clamp(0.2rem, 0.5vw, 0.35rem);
  align-items: center;
  margin: clamp(0.15rem, 0.3vw, 0.25rem) 0;
}

.flow .node {
  text-align: center;
  border: 1px solid var(--stroke);
  border-radius: clamp(8px, 1.2vw, 15px);
  padding: clamp(0.3rem, 0.6vw, 0.5rem);
  background: rgba(7, 23, 36, 0.48);
  font-size: clamp(0.6rem, 1.1vw, 0.85rem);
}

.flow .arrow {
  text-align: center;
  color: var(--accent-2);
  font-size: clamp(0.7rem, 1.3vw, 1rem);
}

.accent {
  color: var(--accent);
}

.ok {
  color: var(--accent-2);
}

.warn {
  color: var(--danger);
}

ul, ol {
  margin: clamp(0.05rem, 0.15vw, 0.15rem) 0;
  padding-left: clamp(0.6rem, 1.2vw, 0.9rem);
  line-height: clamp(1.2, 1.4vw, 1.3);
}

li {
  margin: clamp(0.02rem, 0.08vw, 0.05rem) 0;
  line-height: clamp(1.2, 1.4vw, 1.3);
}

.panel ul, .panel ol {
  margin: clamp(0.05rem, 0.12vw, 0.1rem) 0;
  padding-left: clamp(0.6rem, 1.1vw, 0.85rem);
}

.panel li {
  margin: clamp(0.01rem, 0.05vw, 0.04rem) 0;
}

.w-full {
  width: 100%;
}

.text-xl {
  font-size: clamp(0.95rem, 2.2vw, 1.4rem);
}

.panel h3 {
  font-size: clamp(0.85rem, 1.8vw, 1.1rem);
  margin: 0 0 clamp(0.05rem, 0.15vw, 0.1rem) 0;
}

.mt-2 { margin-top: clamp(0.15rem, 0.3vw, 0.2rem); }
.mt-3 { margin-top: clamp(0.2rem, 0.4vw, 0.25rem); }
.mt-4 { margin-top: clamp(0.2rem, 0.4vw, 0.25rem); }
.mt-5 { margin-top: clamp(0.15rem, 0.3vw, 0.2rem); }
.mt-6 { margin-top: clamp(0.15rem, 0.3vw, 0.2rem); }
.mt-8 { margin-top: clamp(0.15rem, 0.3vw, 0.2rem); }
</style>

# Agentic Integrations in yugastore-java

<div class="panel mt-6">
  <div class="text-xl">How configuration-driven AI workflows enable safe concurrent work in a Java microservices brownfield project.</div>
  <div class="mt-4">
    <span class="tag">Execute → Write</span>
    <span class="tag">Task locking</span>
    <span class="tag">Guardrails as quality</span>
    <span class="tag">Brownfield discipline</span>
  </div>
</div>

<div class="muted mt-8">Focus: fewer collisions, tighter coordination, explicit bounds on AI-assisted work.</div>

---
transition: slide-left
---

# The Brownfield Challenge

<div class="grid2 mt-4">
  <div class="panel">
    <h3>What we inherited</h3>
    <ul>
      <li>Java 17 monorepo, 6 Spring Boot services</li>
      <li>Mixed YugabyteDB APIs (YCQL + YSQL)</li>
      <li>Existing bugs and tech debt</li>
      <li>Hard-coded assumptions scattered in code</li>
      <li>No clear tenant/multi-merchant model</li>
    </ul>
  </div>
  <div class="panel">
    <h3>Why this matters for AI work</h3>
    <ul>
      <li>One mistake cascades across services</li>
      <li>Contract mismatches are silent</li>
      <li>Multiple agents lose context fast</li>
      <li>Tests and setup are incomplete</li>
      <li>Need strong coordination defaults</li>
    </ul>
  </div>
</div>

---
---

# From Requirement to Executable Tasks

<div class="panel mt-3">
  <h3>The artifact chain</h3>
  <div class="flow">
    <div class="node">Requirement<br><span class="small">product need</span></div>
    <div class="arrow">→</div>
    <div class="node">PRD<br><span class="small">scoped intent</span></div>
    <div class="arrow">→</div>
    <div class="node">OpenSpec<br><span class="small">execution contract</span></div>
  </div>
  <div class="flow mt-4">
    <div class="node">Tasks<br><span class="small">atomic units</span></div>
    <div class="arrow">→</div>
    <div class="node">Issues<br><span class="small">path-locked</span></div>
    <div class="arrow">→</div>
    <div class="node">Implementation<br><span class="small">bounded scope</span></div>
  </div>
</div>

<div class="panel mt-5">
  <h3>PRD → OpenSpec conversion</h3>
  <div class="grid2">
    <div>
      <p class="accent">PRD contains</p>
      <p class="mono small">
        Goals, users, scope, constraints, assumptions, dependencies, open questions, success metrics
      </p>
    </div>
    <div>
      <p class="accent">OpenSpec breaks it down</p>
      <p class="mono small">
        proposal.md (why), specs/ (behavior deltas), design.md (decisions), tasks.md (execution steps)
      </p>
    </div>
  </div>
</div>

<div class="muted mt-4">Each artifact is a contract. Agents read them, not guess from code.</div>

---
---

# How Agents Use OpenSpec

<div class="panel mt-3">
  <ul>
    <li><span class="accent">proposal.md:</span> understand intent and constraints before coding</li>
    <li><span class="accent">specs/:</span> define acceptance criteria and behavior changes per service</li>
    <li><span class="accent">tasks.md:</span> pick the next executable task and its reserved paths</li>
    <li><span class="accent">GitHub issues:</span> find ready-to-claim work with dependencies and locks documented</li>
  </ul>
</div>

<div class="muted mt-5">Each artifact is read by agents as an execution contract, not guesswork from code.</div>


---
---

# Task Locking: Concurrency Without Chaos

<div class="panel mt-3">
  <h3>How it works</h3>
  <ol>
    <li>Pick a ready issue with declared reserved paths</li>
    <li>Verify no other active lock overlaps</li>
    <li>Claim the issue globally (GitHub label + comment)</li>
    <li>Code only within reserved scope</li>
    <li>Mark issue agent-locked until done</li>
  </ol>
</div>

<div class="grid2 mt-5">
  <div class="panel">
    <div class="accent">Example reserved paths</div>
    <p class="mono small">
      api-gateway-microservice/src/main/java/com/yugastore/gateway
    </p>
    <p class="mono small">
      resources/schema.cql
    </p>
  </div>
  <div class="panel">
    <div class="accent">Why it matters</div>
    <p class="small">Multiple agents work in parallel safely. Reviewers know exactly what service was touched and why.</p>
  </div>
</div>

---
---

# AI Workflow Diagram

<div class="panel mt-4">
  <h3>Agent-safe execution cycle</h3>
  <ul>
    <li><span class="accent">1. Discover:</span> list ready issues, check overlap</li>
    <li><span class="accent">2. Lock:</span> claim issue, reserve paths</li>
    <li><span class="accent">3. Understand:</span> read linked OpenSpec artifacts</li>
    <li><span class="accent">4. Implement:</span> write code scoped to paths</li>
    <li><span class="accent">5. Verify:</span> run targeted tests for touched modules</li>
    <li><span class="accent">6. Mark:</span> update tasks, close issue, notify team</li>
  </ul>
</div>

<div class="muted mt-4">This is the repeatable pattern. Instructions enforce it. Skills automate it.</div>

---
---

# Friction Points We Solved

<div class="panel mt-2">
  <h3>The problem</h3>
  <p class="small">Silent collisions on issues. Spec debt. No declared ownership on cross-service work.</p>
</div>

<div class="panel mt-3">
  <h3 style="margin-bottom: 0.3em;">The solution</h3>
  <div class="grid2" style="gap: 0.6rem;">
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Specification first</p>
      <p class="small">OpenSpec defines behavior deltas per service before code.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Path-locked issues</p>
      <p class="small">When you claim issue #42, you declare which files you'll edit. No overlap possible.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Forced coordination</p>
      <p class="small">Cross-service changes reserve all touched services upfront. One agent owns the slice.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Repeatable workflow</p>
      <p class="small">Read spec → claim → implement → test → done.</p>
    </div>
  </div>
</div>

<div class="panel mt-2">
  <div class="ok">Result:</div>
  <p class="small">Zero overlapping edits. Specs stay in sync. Predictable cycle time.</p>
</div>

---
---

# Inheriting a Brownfield Codebase

<div class="panel mt-3">
  <h3>The inheritance challenge</h3>
  <p class="small">Outdated dependencies. Unfinished documentation. Sparse test coverage. Assumptions baked into code. Making any change risks breaking flows nobody remembers.</p>
</div>

<div class="panel mt-4">
  <h3>What this means for agents</h3>
  <div class="grid2" style="gap: 0.6rem;">
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">No guardrails</p>
      <p class="small">Tests don't cover all code paths. You can't trust the test suite to catch regressions.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Hidden contracts</p>
      <p class="small">API contracts live in someone's brain, not docs. A schema change silently breaks upstream services.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Stale dependencies</p>
      <p class="small">Libraries two major versions behind. Security patches languish. Upgrade risk is high.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Accidental coupling</p>
      <p class="small">Cart service hardcoded to expect exact product schema. Change one, break the other.</p>
    </div>
  </div>
</div>

<div class="panel mt-2">
  <div class="warn">The real risk:</div>
  <p class="small">An agent makes what looks like a safe change. Tests pass (the ones that exist). Months later, a user hits an edge case in production.</p>
</div>

---
---

# Existing Bugs: What We Don't Touch

<div class="panel mt-3">
  <h3>Unwritten rule</h3>
  <p>Do not fix unrelated bugs while implementing a feature.</p>
  <p class="muted small mt-3">
    If implementation touches buggy code, document it. Create a linked follow-up issue. Keep the diff tight and focused.
  </p>
</div>

<div class="panel mt-5">
  <div class="warn">Temptation:</div>
  <p class="small">
    "While I'm in cart-microservice, let me fix that stale todo in the repository layer."
  </p>
</div>

<div class="panel mt-3">
  <div class="ok">Reality:</div>
  <p class="small">
    Create a separate issue, test the fix independently, and link it. Reviewers and future agents need tight ownership.
  </p>
</div>

---
---

# Issue Claims: Exclusive Task Checkout

<div class="panel mt-3">
  <h3>When you claim an issue</h3>
  <p class="small">You declare intent on GitHub with a lock comment. That issue is now exclusively yours until you mark it done.</p>
</div>

<div class="panel mt-3">
  <h3>How it works</h3>
  <div class="grid2" style="gap: 0.6rem;">
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Issue declares reserved paths</p>
      <p class="small">"I will edit api-gateway/src/main/java/... and resources/schema.cql"</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Agent checks for overlap</p>
      <p class="small">Is another agent already locked to those paths? If yes, pick a different issue.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Claim the lock globally</p>
      <p class="small">Add agent-locked label and lock comment to the GitHub issue. Now you own it.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Exclusive checkout</p>
      <p class="small">No other agent can touch those paths. Only you can edit them until issue closes.</p>
    </div>
  </div>
</div>

<div class="panel mt-2">
  <div class="ok">Impact:</div>
  <p class="small">Zero overlapping edits. Each file is owned by exactly one agent at a time.</p>
</div>

---
---

# Code-Level Exclusivity: One Agent Per File

<div class="panel mt-3">
  <h3>The guarantee</h3>
  <p class="small">While an agent holds an issue lock, those exact file paths are exclusively checked out. No concurrent writes to the same code.</p>
</div>

<div class="panel mt-3">
  <h3>Example: Tenant context propagation</h3>
  <div class="grid2" style="gap: 0.6rem;">
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">Without exclusivity</p>
      <p class="small">Agent A edits api-gateway schema. Agent B edits products-microservice schema. Both assume different tenant contract. Silent conflict.</p>
    </div>
    <div>
      <p class="accent" style="margin-bottom: 0.2em;">With exclusive locking</p>
      <p class="small">One issue reserves all three services. One agent. One contract. All writes coordinated.</p>
    </div>
  </div>
</div>

<div class="panel mt-2">
  <div class="ok">Result:</div>
  <p class="small">Each codebase file has exactly one author at a time. No merge conflicts from concurrent agent edits. Contracts stay in sync.</p>
</div>

---
---

# Execute → Write

<div class="panel mt-3">
  <h3>The core mindset</h3>
  <p>Do not speculate. Execute first. Then write code.</p>
</div>

<div class="grid2 mt-5">
  <div class="panel">
    <div class="warn">Avoid</div>
    <p class="small">
      "Based on the code, I think the API expects a userId header. Let me refactor everywhere it's used."
    </p>
  </div>
  <div class="panel">
    <div class="ok">Better</div>
    <p class="small">
      "Let me trace the actual request flow in your API gateway test. Now I know exactly where userId comes from and what the contract is."
    </p>
  </div>
</div>

<div class="panel mt-5">
  <div class="accent">Rule:</div>
  <p class="small">Read tests. Run the app. Hit the API. Verify assumptions before proposing changes.</p>
</div>

---
---

# Configuration in Action: Merchant Tenant Foundation

<div class="panel mt-2">
  <p class="mono small">openspec/changes/merchant-tenant-foundation/</p>
  <ul>
    <li><span class="accent">proposal.md:</span> why and scope</li>
    <li><span class="accent">specs/:</span> behavior deltas</li>
    <li><span class="accent">tasks.md:</span> bounded, sequenced work</li>
    <li><span class="accent">GitHub issues:</span> one per task group, reserved paths declared explicitly</li>
  </ul>
</div>

<div class="muted mt-5">This is what "from requirement to implementation" looks like with agentic configuration.</div>

---
layout: center
---

# Q and A

<div class="panel mt-6">
  <div class="text-xl">What workflow bottleneck should we solve next?</div>
</div>

<div class="muted mt-6">
  Follow-up prompts:
  Do you want tighter local verification? Faster issue dispatch? Or deeper cross-service test coverage?
</div>