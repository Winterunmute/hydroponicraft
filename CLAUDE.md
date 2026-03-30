# Hydroponicraft — Claude Code Session Instructions

You are helping me build a NeoForge 1.21.1 Minecraft mod called **Hydroponicraft**
(mod ID: `hydroponicraft`, package: `com.hydroponicraft`).
It is a hydroponics automation mod that integrates with the Create mod for rotational power.

**Read `BLUEPRINT.md` in the project root before doing anything else.**
It is the authoritative spec for all machines, fluids, recipes, progression, and build order.
Do not write any code until you have read it.

---

## Core Loop

Organic Items → Digester (Create-powered) → Nutrient Fluid → Mixer (Create-powered, 3 fluid inputs)
→ Specialized Growth Solution → Fluid Pipes → Growth Bed → Accelerated + enhanced crop harvests

---

## Key Facts

- NeoForge 1.21.1, Java 21
- Create 6.0.9-215 is a **required** dependency
- All machines extend `KineticBlockEntity` and must call `isSpeedRequirementFulfilled()`
  before any processing logic
- Processing speed scales with RPM via `getSpeed()`
- Registration: always `DeferredRegister` + `DeferredHolder` — never direct registry calls
- Capabilities: `IFluidHandler` via `FluidTank`, `IItemHandler` via `ItemStackHandler`,
  both exposed through `RegisterCapabilitiesEvent` on the mod event bus
- Fluids registered via `BaseFlowingFluid` — each needs still, flowing, fluid block, and bucket item

---

## How We Work

- We follow the numbered build order in `BLUEPRINT.md` exactly, one step at a time
- **Before writing any code for a step, tell me your plan.** I will approve before you write anything
- After each step I will run `./gradlew build` to confirm it compiles before we move on
- Only touch files relevant to the current step — do not refactor or change other files
- If you are unsure about a Create API detail, say so rather than guessing
- Keep a running note at the end of your responses of which step we just completed
  and what the next step is, so it is easy to resume in a new session

---

## Resuming a Session

### Completed steps (all build-verified):
- **Step 1** ✅ — Registration infrastructure (`HydroponiCraftMod`, `HydroponiCraftRegistry`)
- **Step 2** ✅ — Create dependency wired (`KineticBlockEntity` compiles; Flywheel + Ponder added)
- **Step 3** ✅ — All 6 fluids registered (`HydroponiCraftFluids` — still, flowing, block, bucket, FluidType each)
- **Step 4** ✅ — Digester block + BE + recipe system (`DigesterBlock`, `DigesterBlockEntity`, `DigesterRecipe`, `DigesterRecipeSerializer`, ~25 recipe JSONs)
- **Step 5** ✅ — Digester GUI (`DigesterMenu`, `DigesterScreen`, `ContainerData` 5 values, `MenuProvider`)
- **Step 6** ✅ — Mixer block + BE (`MixerBlock`, `MixerBlockEntity`, `MixerRecipe`, `MixerRecipeSerializer`, 3 recipe JSONs; Enriched Solution + Yield Tonic deferred)
- **Step 7** ✅ — GrowthModifier registry (`GrowthModifier.java` — static map: fluid → speedMultiplier + fortuneLevel for all 6 fluids including `nutrient_fluid`)
- **Step 8** ✅ — Growth Bed block + BE (`GrowthBedBlock`, `GrowthBedBlockEntity` — fluid tank, seed slot, harvest + drops to hopper below)
- **Step 9** ✅ — Fluid Pipes removed — using Create's own fluid pipes instead
- **Step 10** ✅ — Data generation (block states, models, loot tables, lang file)
- **Step 11** ✅ — Creative tab (`BuildCreativeModeTabContentsEvent` — Growth Bed icon, 3 machines + 6 buckets)

### Post-blueprint work (build-verified):
- Digester + Mixer textures (6 face PNGs each) + Blockbench models — fixed format_version + coordinate shift
- Digester GUI texture (`textures/gui/digester.png` — 256×166 sprite sheet) + `DigesterScreen` wired to render correctly
- Crafting recipes for Digester, Mixer, Growth Bed (shaped JSON)
- `DigesterBlock` + `MixerBlock` fixed to extend `KineticBlock implements IBE<T>` — shaft now works
- `hasShaftTowards()` overridden on both — shaft connects on model-west face (`facing.getCounterClockWise()`)
- Growth Bed fluid capability: blocked on `Direction.UP`, open on all other sides
- `nutrient_fluid` added to `GrowthModifier` at 1.0× speed, 0 fortune
- Create version range relaxed to `[6.0.4,6.1.0)` in `neoforge.mods.toml`

### What works in game (confirmed or build-verified):
- **Digester** — shaft connects on model-west face; processes items; fluid output on east face; GUI being removed (redesign in progress)
- **Mixer** — shaft connects on model-west face; accepts fluid from bucket or Create pipes
- **Growth Bed** — accepts fluid on all sides except top; `nutrient_fluid` works; drops to hopper below or falls as entities
- **All machines have crafting recipes** and appear in the Hydroponicraft creative tab

### KEY DESIGN DECISION — Growth Bed:
**Growth Bed does NOT auto-replant.** The player uses a Create Deployer to replant seeds.
- Growth Bed detects a mature `CropBlock` above it, breaks it (no drops spawned in world), and pushes drops downward into the hopper (or `IItemHandler` below).
- Replanting is the player's responsibility — set up a Create Deployer with seeds above the bed.
- The current implementation has a seed slot that should be removed. See "Next actions" below.

### Next actions when resuming:
1. **Digester redesign** — Remove GUI entirely (no `useWithoutItem`, no `DigesterMenu`/`DigesterScreen`). Reduce to 1 input slot, hopper-fed from above. Remove `seedHandler` capability from Growth Bed (`GrowthBedBlockEntity` seed slot already removed). Shaft on model-east face (`facing.getClockWise()`); fluid output on all faces except `Direction.UP` and `facing.getClockWise()` (west, north, south, bottom).
2. **Balance fixes** — Growth Bed tick rate and fluid consumption (250 mB/tick) are too aggressive.
3. **Fix en_us.json** — fluid display names may use wrong keys; NeoForge 1.21.1 uses `fluid_type.hydroponicraft.*` format. Verify creative tab appears in creative menu.
4. **Growth Bed textures + Blockbench model** — still vanilla placeholder textures.
5. **Missing Mixer recipes** — Enriched Solution and Yield Tonic need `nether_wart_fluid` + `chorus_fruit_fluid` registered first (see TODO.md).
6. **Test in game** — `.\gradlew.bat runClient`

### Implementation notes:
- **KineticBlock pattern**: `DigesterBlock` and `MixerBlock` both extend `KineticBlock implements IBE<T>`. `KineticBlock.hasShaftTowards()` returns `false` by default — must override it. `KineticBlock.newBlockEntity()` is final — IBE provides it. `KineticBlock` extends `Block` not `BaseEntityBlock` — no `codec()` needed.
- **Mixer output capability**: only the output tank is exposed as `IFluidHandler` on all sides. Input tanks filled by bucket or Create pipes.
- **Growth Bed — crop detection**: uses `CropBlock instanceof` check (not `BonemealableBlock`). `BonemealableBlock` lacks `getAge()`/`getMaxAge()`/`getStateForAge()` APIs needed for per-tick age control.
- **Growth Bed — harvest**: uses `Block.getDrops()` with fortune-enchanted `ItemStack`, then `level.setBlock(cropPos, AIR, 3)`. Does NOT use `Level.destroyBlock()` — that spawns item entities and doesn't support custom fortune tools.
- **Growth Bed — output**: drops pushed into `IItemHandler` of block below, fallback to `Block.popResource()`. No output buffer. No auto-replant — player uses Create Deployer.
- **Deferred Mixer recipes**: `enriched_solution` and `yield_tonic` have no Mixer recipe. Need `nether_wart_fluid` and `chorus_fruit_fluid` registered first.
- **Digester redesign**: GUI removed; 1 input slot; hopper-fed from top only. Shaft on model-west (`facing.getCounterClockWise()`); fluid output on model-east (`facing.getClockWise()`).

### Key API facts confirmed:
- `FluidTank` is at `net.neoforged.neoforge.fluids.capability.templates.FluidTank`
- `FluidTank.writeToNBT` / `readFromNBT` both take `(HolderLookup.Provider, CompoundTag)`
- `KineticBlock` extends `Block` (not `BaseEntityBlock`) — no abstract `codec()` to implement
- `SmartBlockEntity.saveAdditional` is final — override `write(CompoundTag, HolderLookup.Provider, boolean)` and `read(CompoundTag, HolderLookup.Provider, boolean)` instead
- Plain `BlockEntity` overrides `saveAdditional` / `loadAdditional` (not `write`/`read`)
- `canSustainPlant()` in NeoForge 1.21.1: returns `TriState`, last param is `BlockState` (not `IPlantable`)
- `CropBlock.randomTick()` is protected — advance age via `getAge()` / `getStateForAge()` / `getMaxAge()` instead
- `new FluidStack(FluidStack, int)` does not exist — use `new FluidStack(fluidStack.getFluid(), amount)`
- Loot table files in 1.21.1 go in `data/namespace/loot_table/blocks/` (singular, not `loot_tables`)
- Ponder must be `compileOnly` when Create uses `transitive = false`
- `GuiGraphics.blit` for non-standard texture sheet sizes: use the 9-arg overload `blit(texture, x, y, u, v, w, h, texW, texH)` — the 5-arg overload assumes 256×256 and will sample wrong UVs on a 256×166 texture
- Blockbench exports have `format_version` (Bedrock field) and centered coordinates `[-8,0,-8]→[8,16,8]` — both must be fixed before use in Java edition (remove field, shift to `[0,0,0]→[16,16,16]`)