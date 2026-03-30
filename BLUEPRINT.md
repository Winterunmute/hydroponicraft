
HYDROPONICRAFT
NeoForge 1.21.1 Mod — Development Blueprint
Mod ID: hydroponicraft  |  Package: com.hydroponicraft  |  Dependency: Create 6.0.9+

1. Overview
Hydroponicraft is a hydroponics automation mod for NeoForge 1.21.1, designed for play in the FTB Direwolf20 Season 14 modpack. It introduces a kinetic-powered processing pipeline that converts organic materials into nutrient fluids, which are blended into specialized growth solutions and delivered via pipes to automated Growth Beds that accelerate and enhance crop yields.

Core processing loop:

Organic Items  →  Digester  →  Nutrient Fluid  →  Mixer (+ Water + Additives)  →  Solution  →  Pipes  →  Growth Bed  →  Crops

All machines require Create rotational power (SU + RPM) to operate
No required dependencies beyond Create and NeoForge
Designed to integrate naturally with Create's existing fluid and item pipe systems

2. Project Setup
Identifiers
Field
Value
Mod Name
Hydroponicraft
Mod ID
hydroponicraft
Package
com.hydroponicraft
Java Version
Java 21
Minecraft Version
1.21.1
Loader
NeoForge
Create Version
6.0.9-215

Base Template
Clone the NeoForge MDK for 1.21.1: https://github.com/neoforged/MDK/tree/1.21.1
This gives you a working Gradle build environment to start from

build.gradle — Create Dependency
Add the following to your build.gradle repositories and dependencies blocks:

repositories {
    maven { url = "https://maven.createmod.net" }   // Create, Ponder, Flywheel
    maven { url = "https://maven.ithundxr.dev/snapshots" }  // Registrate
}

dependencies {
    implementation("com.simibubi.create:create-${minecraft_version}:${create_version}:slim") { transitive = false }
    implementation("net.createmod.ponder:ponder-neoforge:${ponder_version}+mc${minecraft_version}")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-${minecraft_version}:${flywheel_version}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-${minecraft_version}:${flywheel_version}")
    implementation("com.tterrag.registrate:Registrate:${registrate_version}")
}

gradle.properties — Version Pins
minecraft_version = 1.21.1
create_version = 6.0.9-215
ponder_version = 1.0.81
flywheel_version = 1.0.6
registrate_version = MC1.21-1.3.0+67

neoforge.mods.toml — Required Dependency Declaration
[[dependencies.hydroponicraft]]
    modId = "create"
    type = "required"
    versionRange = "[6.0.9,6.1.0)"
    ordering = "NONE"
    side = "BOTH"

3. Fluids
Register all fluids using NeoForge BaseFlowingFluid. Each fluid needs a still variant, flowing variant, fluid block, and bucket item.

Registry Name
Display Name
Role
nutrient_fluid
Nutrient Fluid
Raw output from Digester
nutrient_solution
Nutrient Solution
Basic Mixer output (nutrient + water)
enriched_solution
Enriched Solution
Upgraded growth speed
yield_tonic
Yield Tonic
Fortune-like bonus on harvest
accelerant_solution
Accelerant Solution
Maximum growth speed, no yield bonus
balanced_blend
Balanced Blend
Moderate speed + small yield bonus

4. Machines
4.1 Digester
Converts organic items into raw Nutrient Fluid using Create rotational power.

Block Entity — DigesterBlockEntity
Extends KineticBlockEntity (Create)
Item handler: 3 organic input slots, 1 optional byproduct output slot
Fluid handler: 1 output tank — Nutrient Fluid, max 8,000 mB
Processing logic in tick():
Requires isSpeedRequirementFulfilled() — does nothing if not connected to rotation
Processing speed scales with RPM: base 200 ticks at minimum RPM, faster at higher RPM
On completion: consumes inputs, produces mB output defined by DigesterRecipe
Persists inventory + fluid tank to NBT

Recipe Type — DigesterRecipe
Input Item
Output mB
Base Ticks
Notes
Wheat Seeds
500 mB
200
Common early source
Rotten Flesh
800 mB
180
High yield, easy to automate
Oak Leaves
300 mB
150
Fast, low yield
Nether Wart
1500 mB
300
Mid-game, unlocks Enriched mix
Chorus Fruit
2000 mB
400
Late-game, unlocks Yield Tonic

4.2 Mixer
Combines Nutrient Fluid with water and optional additives to produce specialized growth solutions.

Block Entity — MixerBlockEntity
Extends KineticBlockEntity (Create)
Fluid handler: 3 input tanks + 1 output tank
Tank 1: Water input (max 4,000 mB)
Tank 2: Nutrient Fluid input (max 4,000 mB)
Tank 3: Optional additive fluid input (max 4,000 mB)
Output tank: target solution (max 8,000 mB)
Tick logic: if input tanks meet recipe minimums and machine is spinning, consume inputs and produce output
No item slots — fluid-only operation
No GUI required — interact with buckets or connect pipes directly

Solution Recipes
Output Solution
Input 1
Input 2
Effect
Nutrient Solution
1000 mB Nutrient
1000 mB Water
1.5x growth speed
Enriched Solution
1000 mB Nutrient
500 mB Nether Wart Fluid
2.5x growth speed
Yield Tonic
1000 mB Nutrient
500 mB Chorus Fluid
1x speed + Fortune 2 on harvest
Accelerant Solution
1500 mB Nutrient
1000 mB Enriched
4x speed, no yield bonus
Balanced Blend
500 mB Nutrient
500 mB Yield Tonic
2x speed + Fortune 1

4.3 Fluid Pipes
Simple fluid transport blocks. No pressure simulation — first-come-first-served distribution.

Block — FluidPipeBlock
6 BooleanProperty connections: NORTH, SOUTH, EAST, WEST, UP, DOWN
updateShape() — connects to neighbors that expose IFluidHandler capability
Visually renders as a thin pipe with connection arms in each connected direction

Block Entity — FluidPipeBlockEntity
Internal buffer: 1,000 mB
Each tick: pull from connected IFluidHandler sources, push to connected sinks
Priority: push to Growth Beds first, then other pipes
Compatible with any block exposing IFluidHandler (Create fluid tanks, Mekanism pipes, etc.)

4.4 Growth Bed
A full-height block that auto-harvests mature crops, auto-replants by restoring the saved crop BlockState,
and pushes harvested drops downward into whatever is below.

Block — GrowthBedBlock
Full-width, full-height block (16/16)
Crops planted directly on top by the player — no seed slot
Accepts Create fluid pipes on all sides except top (Direction.UP is blocked — that is where crops sit)
canSustainPlant() returns TriState.TRUE for any CropBlock placed above it
isFertile() returns true

Block Entity — GrowthBedBlockEntity
Fluid handler: accepts any fluid in GrowthModifier.REGISTRY, max 4,000 mB
  Capability exposed on all sides except Direction.UP
No item handler / no seed slot — player plants once, bed handles replanting automatically
Saved state: when a CropBlock is first detected above, its age-0 BlockState is saved to NBT
  as savedCropState; used for replanting after every harvest
Tick rate: growth logic runs at most once every 40 server ticks (tickCooldown counter)
Per-tick logic:
  If fluid tank has ≥ 250 mB and a CropBlock is above:
    Consume 250 mB, advance crop age by Math.round(speedMultiplier) stages
  If crop is at max age: harvest, then replant from savedCropState if available
Harvested drops pushed into IItemHandler capability of block below (e.g. hopper);
  if nothing below, items fall as entities at the bed position. No internal output buffer.
Applies Fortune level defined by GrowthModifier to the harvest loot table

Design decisions (confirmed during implementation):
Crop detection uses CropBlock instanceof check — covers all vanilla crops and most modded crops.
  BonemealableBlock was considered but lacks getAge()/getMaxAge()/getStateForAge() APIs.
Auto-harvest uses Block.getDrops() with a fortune-enchanted ItemStack, then level.setBlock(AIR, 3).
  Level.destroyBlock() not used — it spawns item entities and doesn't accept a custom tool.
No seed slot — simpler UX; player plants once. Bed saves the age-0 BlockState of the first detected
  crop and restores it on replant. This works for all CropBlock subclasses without needing item access.
Tick rate floor of 40 server ticks prevents excessive fluid drain at high RPM speeds.

GrowthModifier Registry
Maps each solution fluid (and nutrient_fluid) → two values:
speedMultiplier (float) — how many age stages to advance per growth tick
fortuneLevel (int) — passed to the LootContext when collecting drops

Fluid                 speedMultiplier  fortuneLevel  Notes
nutrient_fluid        1.0              0             Raw digester output; weakest — incentive to build Mixer
nutrient_solution     1.5              0             Basic Mixer output
enriched_solution     2.5              0             Mid-game, requires Nether Wart
yield_tonic           1.0              2             Fortune 2 harvest; requires Chorus Fruit
accelerant_solution   4.0              0             Maximum speed; late-game chain mix
balanced_blend        2.0              1             Speed + Fortune 1 balance

5. Build Order for Claude Code
Work through these steps one at a time. Build and test after each step before proceeding.

Step
Task
Key Classes / Notes
1
Registration infrastructure
HydroponiCraftMod.java, HydroponiCraftRegistry.java — DeferredRegister for blocks, items, fluids, block entities, creative tab
2
Create dependency check
Confirm KineticBlockEntity compiles. Add Create to build.gradle + mods.toml. Run ./gradlew build.
3
Register all fluids
All 6 fluids using BaseFlowingFluid — still, flowing, block, bucket for each
4
Digester block + BE
DigesterBlock, DigesterBlockEntity (KineticBlockEntity), DigesterRecipe + RecipeSerializer
5
Digester GUI
DigesterMenu (AbstractContainerMenu), DigesterScreen — item slots, fluid tank level, progress arrow, RPM indicator
6
Mixer block + BE
MixerBlock, MixerBlockEntity (KineticBlockEntity), 3 fluid input tanks + output tank
7
GrowthModifier registry
GrowthModifier.java — data class with speedMultiplier + fortuneLevel. Static registry map: fluid → modifier
8
Growth Bed block + BE
GrowthBedBlock, GrowthBedBlockEntity — fluid tank, seed slot, output buffer, harvest+replant tick logic
9
Fluid Pipes
FluidPipeBlock (6 BooleanProperty connections), FluidPipeBlockEntity (1000 mB buffer, push/pull)
10
Data generation
Block states, item models, loot tables, recipe JSONs (Digester + Mixer), lang/en_us.json
11
Creative tab + polish
Register creative tab with Growth Bed icon. Add all items/blocks/buckets to tab.

6. Key NeoForge Patterns
Registration
Always use DeferredRegister + DeferredHolder — never Registry.register() directly
Register all DeferredRegisters to the mod event bus in your main mod class constructor

Capabilities
Expose IItemHandler and IFluidHandler via RegisterCapabilitiesEvent on the mod event bus
Use FluidTank from NeoForge for fluid storage — handles NBT serialization and capacity checks cleanly
Use ItemStackHandler from NeoForge for item storage

Ticking Block Entities
Register tick via BlockEntityTicker in your Block class (getBlockEntityTicker override)
Implement separate server-side and client-side tick methods

Create Integration
Extend KineticBlockEntity for all powered machines
Call isSpeedRequirementFulfilled() before any processing logic — gate everything behind this
Speed scaling: getSpeed() returns current RPM — use this to compute processing tick intervals
Stress consumption: override getStressImpact() to return your machine's SU cost

Recipe System
Extend Recipe<RecipeInput> for DigesterRecipe
Register RecipeType + RecipeSerializer in your DeferredRegister
Store recipes as JSON in resources/data/hydroponicraft/recipe/

7. Intended Progression
Stage
Unlock
How
Early
Basic Nutrient Solution
Feed Digester rotten flesh or leaves → mix with water
Early-Mid
Automated harvesting
Route solution through pipes to Growth Bed, add seed slot + hopper output
Mid
Enriched Solution
Farm Nether Wart → Digester → blend with base nutrient
Mid-Late
Yield Tonic
Obtain Chorus Fruit (End) → Digester → blend → Fortune 2 harvests
Late
Accelerant + Balanced Blend
Chain Mixer outputs, full Create automation, maximum throughput

8. How to Use This With Claude Code
1. Clone the NeoForge 1.21.1 MDK and open the project folder in your terminal
2. Run: claude  (starts a session in your project directory)
3. Paste this document as context and say: "Use this as the blueprint. Let's start with Step 1 — set up the registration infrastructure."
4. For each step, ask Claude Code to explain its plan before writing any code
5. After each step, run: ./gradlew build  — confirm it compiles before moving on
6. If fluid registration gets confusing, point Claude Code at: https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1

Useful Prompt Patterns
"Show me the plan for this step before touching any files"
"The build is failing with [error] — read the relevant files and fix it"
"We finished Step 4. Summarize what was built and confirm we're ready for Step 5"
"Only touch files related to [class name] — don't change anything else"

Hydroponicraft — Development Blueprint  |  NeoForge 1.21.1 + Create 6.0.9