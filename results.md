# zero-onboard results — hydroponicraft

Ran `zero-onboard https://github.com/Winterunmute/hydroponicraft` on 2026-04-17.

The project directory existed but only contained the Zero System task file (CLAUDE.md) with
no actual source code — the `zero-onboard` tool skipped cloning because the directory was
already present (created by `ensure_project()`). The upstream repo was fetched manually via
HTTPS and `zero-onboard` was re-run against the populated source tree (78 files analysed).

---

## What the project is

**Hydroponicraft** is a NeoForge 1.21.1 Minecraft mod adding a Create-powered hydroponics
pipeline. Players convert organic waste (rotten flesh, leaves, meats, nether wart, chorus
fruit) into nutrient fluids via the **Digester**, blend them into growth solutions via the
**Mixer**, and feed those solutions into **Growth Beds** that auto-harvest crops above them.
Hard dependency on Create 6.0.x. Built for the FTB Direwolf20 Season 14 modpack.

---

## Onboarding documents written

| File | Contents |
|---|---|
| `~/sync/hydroponicraft/onboarding.md` | Executive summary (this) |
| `~/sync/hydroponicraft/onboarding/system_design.md` | End-to-end pipeline, all 3 machines, recipe system, capability wiring, NBT persistence, build system |
| `~/sync/hydroponicraft/onboarding/module_map.md` | File tree with one-line purpose per file, grouped by role |
| `~/sync/hydroponicraft/onboarding/constraints.md` | External tool requirements, registration invariants, Create API constraints, capability wiring rules, recipe constraints, file path conventions |
| `~/sync/hydroponicraft/onboarding/unknowns.md` | 10 open gaps including: Digester redesign not complete, `MixerRecipe.input3` never checked, balance values inconsistent with TODO comments, creative tab not appearing, bucket/Growth Bed textures are placeholders |

---

## Three most important things for a new contributor

1. **Create is a required runtime dependency.** All three machines extend `KineticBlockEntity`.
   Without Create the mod will not load. Flywheel and Ponder are compile-only (pulled in by Create
   at runtime) — do not add them as `implementation`.

2. **Fluid static init order is fragile.** `HydroponiCraftFluids.register()` must be called
   before `HydroponiCraftRegistry.register(bus)` in the mod constructor. The `static {}` block
   in `HydroponiCraftFluids` assigns `BaseFlowingFluid.Properties` after all `DeferredHolder`
   fields — changing this order causes NPE at class load time.

3. **The codebase is mid-redesign.** The Digester GUI removal, two missing Mixer recipes
   (Enriched Solution, Yield Tonic), balance tuning, creative tab fix, and texture work are all
   explicitly deferred. See `TODO.md` and `unknowns.md` before assuming anything is complete.

---

## Issue found during onboarding

The `zero-onboard` tool uses SSH (`git@github.com:`) for cloning and skips cloning when the
target directory already exists. Since Zero System creates project directories via `ensure_project()`
before running tasks, the skip logic will always trigger for Zero System-managed projects,
producing an empty analysis. Mitigation: either remove the directory before running
`zero-onboard`, or add a check inside the tool for whether the directory is a real clone vs.
a Zero System stub.
## [plan-only] Run zero-onboard https://github.com/Winterunmute/hydroponicraft and report results to results.md

**Completed:** 2026-04-17 21:03:20

`zero-onboard` ran successfully against the 78-file codebase. Results written to `results.md`.

**Summary:** Hydroponicraft is a NeoForge 1.21.1 Minecraft mod — a Create-powered hydroponics pipeline with three machines (Digester → Mixer → Growth Bed). Five onboarding docs were written to `~/sync/hydroponicraft/onboarding/`. The analysis found 10 open gaps including an incomplete Digester redesign, a parsed-but-unused `MixerRecipe.input3` field, placeholder textures, and a creative tab registration bug.

**One issue flagged:** `zero-onboard` always skips cloning when the directory exists, which breaks for Zero System-managed projects (where `ensure_project()` pre-creates the directory). The tool needed a manual workaround this run.

---

