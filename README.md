# Hydroponicraft

A hydroponics automation mod for NeoForge 1.21.1, designed for integration with the Create mod.
Convert organic waste into nutrient fluids, blend them into specialized growth solutions,
and pipe the results into Growth Beds that accelerate and enhance your crop yields automatically.

Also includes a self-contained mining system: C4 explosives, Ender C4 for deep ore extraction,
and automation tools for large-scale remote detonation operations.

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Create | 6.0.9+ |

---

## Core Loop

```
Organic Items
     ↓
  Digester  (Create rotational power)
     ↓
Nutrient Fluid
     ↓
   Mixer  (Create rotational power + Water / Additives)
     ↓
Growth Solution
     ↓
Create Fluid Pipes
     ↓
 Growth Bed
     ↓
Accelerated + enhanced crop harvests  →  Hopper below
```

All machines require Create rotational power (shaft + RPM) to operate.
Processing speed scales with RPM — faster shafts mean shorter processing times.

---

## Machines

### Chemical Synthesizer

Converts items combined with a fluid reagent into new items. Used to craft advanced materials
like C4 explosive.

- **Item input:** top face — fed by hopper or Create belt
- **Item output:** bottom face — drained by hopper
- **Fluid input:** any horizontal face except the shaft face
- **Shaft:** connects on the east face
- **No GUI** — hopper-fed input, hopper-drained output

**Chemical Synthesizer recipes:**

| Input Item | Input Fluid | Output | Notes |
|---|---|---|---|
| Gunpowder | 1,000 mB Nutrient Fluid | C4 | Base explosive charge |
| Gunpowder | 1,000 mB Ender Pearl Fluid | Ender C4 | Deep-bore gathering explosive |

---

### Digester

Breaks down organic materials into raw Nutrient Fluid.

- **Input:** 1 item slot — fed by hopper or Create belt from above
- **Output:** Nutrient Fluid from the east face (fluid pipe or bucket)
- **Shaft:** connects on the west face
- **Tank:** 8,000 mB Nutrient Fluid capacity

**Digester input recipes:**

| Input | Output | Notes |
|---|---|---|
| Wheat Seeds | 500 mB | Common early source |
| Wheat | 400 mB | |
| Oak / Birch / Spruce / Jungle / Acacia / Dark Oak Leaves | 300 mB | Fast, low yield |
| Cherry Leaves | 300 mB | |
| Mangrove Leaves | 300 mB | |
| Rotten Flesh | 800 mB | High yield, easy to automate |
| Raw Beef | 700 mB | |
| Raw Porkchop | 700 mB | |
| Raw Chicken | 600 mB | |
| Bone Meal | 400 mB | |
| Slimeball | 600 mB | |
| Cobweb | 350 mB | |
| Moss Block | 450 mB | |
| Seagrass | 350 mB | |
| Kelp | 300 mB | |
| Vine | 300 mB | |
| Sweet Berries | 400 mB | |
| Glow Berries | 400 mB | |
| Nether Wart | 1,500 mB | Mid-game; unlocks Enriched Solution |
| Chorus Fruit | 2,000 mB | Late-game; unlocks Yield Tonic |
| Ender Pearl | 3,000 mB | Produces Ender Pearl Fluid for Ender C4 |

---

### Mixer

Blends Nutrient Fluid with water and optional additives to produce specialized growth solutions.

- **Input:** 3 internal fluid tanks — fill via bucket or Create fluid pipes
- **Output:** target solution from any side (fluid pipe or bucket)
- **Shaft:** connects on the west face
- **No GUI** — pipe fluids in directly or fill with buckets

**Mixer recipes:**

| Output | Input 1 | Input 2 | Notes |
|---|---|---|---|
| Nutrient Solution (1,000 mB) | 1,000 mB Nutrient Fluid | 1,000 mB Water | Basic mix; first upgrade |
| Enriched Solution (1,000 mB) | 1,000 mB Nutrient Fluid | 500 mB Nether Wart Fluid | Requires Nether Wart |
| Yield Tonic (1,000 mB) | 1,000 mB Nutrient Fluid | 500 mB Chorus Fruit Fluid | Requires Chorus Fruit (End) |
| Accelerant Solution (1,000 mB) | 1,500 mB Nutrient Fluid | 1,000 mB Enriched Solution | Late-game chain mix |
| Balanced Blend (1,000 mB) | 500 mB Nutrient Fluid | 500 mB Yield Tonic | Speed + fortune balance |

---

### Growth Bed

A full-block planter that accelerates crop growth using fluid solutions and auto-harvests mature crops.

- **Fluid input:** any face except the top (top is where crops sit)
- **Tank:** 4,000 mB; accepts any growth solution
- **Harvest output:** drops pushed downward into a hopper or other `IItemHandler` below;
  items fall as entities if nothing is below
- **No shaft required** — passive block, no kinetic power needed
- **Does not auto-replant** — use a Create Deployer pointed downward above the bed,
  loaded with seeds, to replant after each harvest

**How to use:**
1. Place the Growth Bed
2. Plant a crop directly on top
3. Connect a fluid pipe to any side (not the top) supplying a growth solution
4. Place a hopper below to collect drops
5. Set up a Create Deployer above pointing down, loaded with seeds, to replant

---

## C4 Explosives

### C4

A placeable explosive charge. Attach it to any surface — floor, wall, or ceiling — and
detonate it remotely. C4 can be dyed with any of the 16 Minecraft dye colors for
color-coded grouping.

- Placed on any solid face (floor, wall, ceiling)
- Breaks free if its mounting surface is removed
- Only the player who placed a C4 block can detonate it
- Crafted in the Chemical Synthesizer: Gunpowder + Nutrient Fluid → C4
- Dye by combining any C4 + dye in a crafting table (shapeless)

**Explosion sequence** — five staged blasts over 16 ticks:

| Stage | Delay | Strength | Position |
|---|---|---|---|
| 1 | +0 ticks | 10 | Block centre |
| 2 | +4 ticks | 11 | 2 blocks below |
| 3 | +8 ticks | 12 | 3 blocks below |
| 4 | +12 ticks | 11 | 1–2 blocks in a random horizontal direction |
| 5 | +16 ticks | 10 | 1–2 blocks in another random horizontal direction |

---

### Ender C4

An advanced explosive that teleports all item drops from the blast directly into a linked
**Gathering Chest**, bypassing the ground entirely.

- Same five-stage explosion sequence as regular C4
- Drops from all five stages are swept into the nearest matching Gathering Chest
- Can be colored with any dye — used to link specific Ender C4 colors to specific chests
- Only the owning player's Gathering Chests collect drops
- Crafted in the Chemical Synthesizer: Gunpowder + Ender Pearl Fluid → Ender C4
- Dye by combining any Ender C4 + dye in a crafting table (shapeless)

---

### Remote Detonator

Detonates all matching C4 and Ender C4 charges you own across all loaded chunks.

- **Right-click** (in air or on a block): detonate all matching charges
- **Shift + Right-click**: cycle the selected color
- Tooltip shows the currently selected color
- Only detonates C4 blocks you placed yourself
- Only detonates blocks that match the selected color (base/uncolored C4 always matches)
- Stacks to 1

**Crafting:**
```
[      ] [Redstn] [      ]
[ Gold ] [Copper] [ Gold ]
[      ] [ Iron ] [      ]
```
Redstone × 1 · Gold Ingot × 2 · Copper Ingot × 1 · Iron Ingot × 1

---

### Ender C4 Launcher

A handheld launcher that fires Ender C4 from your inventory deep into rock faces.

- **Right-click**: fires one Ender C4 (any color variant) from your inventory
  1. Raycasts up to 100 blocks in the look direction to find the first solid block surface
  2. If no surface found: does nothing (no item consumed)
  3. From that surface, bores a further [boring depth] blocks in the same direction
  4. Places the Ender C4 block at that final position, through solid rock
- **Shift + Right-click**: cycle boring depth (5 → 10 → … → 50 → 5), shown in action bar
- Boring depth stored per item (default 10 blocks, range 5–50)
- 1-second cooldown between shots
- Plays ender pearl throw sound on fire
- Stacks to 1

**Crafting:**
```
[      ] [EPearl] [      ]
[IronBlk] [BlazeR] [IronBlk]
[      ] [ Iron ] [      ]
```
Ender Pearl × 1 · Iron Block × 2 · Blaze Rod × 1 · Iron Ingot × 1

---

### Redstone Detonator

A placeable block that detonates nearby C4 and Ender C4 when powered by redstone.

---

## Gathering Chest

A large storage chest (108 slots, 54 visible in GUI) that automatically collects item drops
from Ender C4 explosions.

- **Right-click**: open the 54-slot GUI
- **Right-click with dye**: link the chest to that dye color — it will only collect drops
  from Ender C4 of the matching color (or uncolored Ender C4)
- Unlinked chests collect drops from any Ender C4 owned by the same player
- Only collects drops from Ender C4 placed by the same player who placed the chest
- Items are collected in order of chest proximity to the explosion; full chests spill to the next nearest
- Uncollected items remain in the world (all chests full or no matching chest)

### Void Filter

Nine filter slots are shown at the bottom of the Gathering Chest GUI (below the 54 storage slots),
labeled **Void Filter**.

- Place any item in a filter slot to void that item type — matching drops are discarded
  instead of stored
- Matching is by item type only (count and NBT are ignored)
- Useful for discarding unwanted stone, dirt, or gravel from ore-mining detonations

### Filter Template

A tool for copying and pasting Gathering Chest void filter configurations.

- **Right-click an empty template on a chest that has filter items**: saves all 9 filter slots
  to the template — shows "Filter saved" in action bar
- **Right-click a loaded template on any chest**: applies the saved filters to that chest —
  shows "Filter applied" in action bar
- Tooltip shows each saved item type when the template is loaded
- Empty template shows "Empty" in tooltip
- Stacks to 1

**Crafting:**
```
[      ] [ Paper] [      ]
[EPearl] [ Gold ] [EPearl]
[      ] [ Paper] [      ]
```
Paper × 2 · Ender Pearl × 2 · Gold Ingot × 1

---

## Ender Pearl Launcher Cart

A minecart with an internal 27-slot chest inventory that automatically fires Ender C4 down
through the terrain when passing over a detector rail.

- **Right-click**: open the 27-slot inventory GUI (title shows current depth)
- **Shift + Right-click**: cycle boring depth (1 → 2 → … → 50 → 1), shown in action bar
- When the cart crosses a **detector rail**, it consumes one Ender C4 from its inventory,
  places the Ender C4 block [depth] blocks directly below the rail, and plays the ender
  pearl throw sound
- Supports all 16 colored Ender C4 variants — color and owner are set automatically
- Drops all inventory contents when broken or killed

---

## Growth Solutions

| Solution | Speed | Fortune | How to obtain |
|---|---|---|---|
| Nutrient Fluid | 1.0× | 0 | Raw Digester output |
| Nutrient Solution | 1.5× | 0 | Mixer: Nutrient Fluid + Water |
| Enriched Solution | 2.5× | 0 | Mixer: Nutrient Fluid + Nether Wart Fluid |
| Yield Tonic | 1.0× | Fortune 2 | Mixer: Nutrient Fluid + Chorus Fruit Fluid |
| Accelerant Solution | 4.0× | 0 | Mixer: Nutrient Fluid + Enriched Solution |
| Balanced Blend | 2.0× | Fortune 1 | Mixer: Nutrient Fluid + Yield Tonic |

Speed multipliers control how many crop age stages are advanced per growth tick.
Fortune applies to the harvest loot table identically to a Fortune-enchanted tool.

---

## Crafting Recipes

### Chemical Synthesizer
```
[IronBlk] [  Glass ] [IronBlk]
[ Copper] [ Piston ] [ Copper]
[ Copper] [Cogwheel] [ Copper]
```
Iron Block × 2 · Copper Ingot × 4 · Glass × 1 · Piston × 1 · Create Cogwheel × 1

---

### Digester
```
[ Copper ] [ Iron  ] [ Copper ]
[ Iron  ]  [Cogwheel] [ Iron  ]
[ Copper ] [ Iron  ] [ Copper ]
```
Copper Ingot × 4 · Iron Ingot × 4 · Create Cogwheel × 1

---

### Mixer
```
[ Iron ] [ Glass ] [ Iron ]
[ Iron ] [Bucket ] [ Iron ]
[Copper] [ Iron  ] [Copper]
```
Iron Ingot × 6 · Copper Ingot × 2 · Glass × 1 · Bucket × 1

---

### Growth Bed
```
[ Iron ] [ Sand ] [ Iron ]
[ Iron ] [Cobble] [ Iron ]
[ Iron ] [ Iron ] [ Iron ]
```
Iron Ingot × 7 · Sand × 1 · Cobblestone × 1

---

### Gathering Chest
```
[ Chest] [ Iron ] [ Chest]
[ Iron ] [EPearl] [ Iron ]
[ Iron ] [ Iron ] [ Iron ]
```
Chest × 2 · Iron Ingot × 6 · Ender Pearl × 1

---

### Redstone Detonator
```
[ Iron ] [Redstn] [ Iron ]
[Redstn] [ Iron ] [Redstn]
[ Iron ] [Redstn] [ Iron ]
```
Iron Ingot × 4 · Redstone × 4

---

### Ender Pearl Launcher Cart
Combines a minecart with an ender pearl mechanism — see in-game recipe book.

---

## Progression

| Stage | Goal | How |
|---|---|---|
| Early | Basic automation | Digester + rotten flesh or leaves → Nutrient Fluid → Growth Bed |
| Early-Mid | First upgrade | Mixer + water → Nutrient Solution (1.5× speed) |
| Mid | Faster growth | Farm Nether Wart → Digester → Mixer → Enriched Solution (2.5×) |
| Mid-Late | Fortune harvests | Chorus Fruit (End) → Digester → Mixer → Yield Tonic (Fortune 2) |
| Late | Maximum throughput | Chain Mixer outputs → Accelerant Solution (4×) or Balanced Blend (2× + Fortune 1) |
| Late | Automated deep mining | Ender Pearl Fluid → Ender C4 → Launcher Cart on rail loop → Gathering Chest |

---

## Notes

- Create fluid pipes are used for all fluid transport — no custom pipes in this mod
- The Digester, Mixer, and Chemical Synthesizer all expose `IFluidHandler` and
  `IItemHandler` capabilities, compatible with any mod that supports standard NeoForge capabilities
- Crops must be a vanilla `CropBlock` subclass (wheat, carrots, potatoes, beetroot, etc.)
  for the Growth Bed to detect and harvest them
- All active machines emit particles and sounds while processing
- C4 and Ender C4 can be dyed any of the 16 vanilla dye colors by combining with a dye in a crafting table
- Gathering Chest void filter discards matched items silently — they are not dropped in the world
- The Ender C4 Launcher raycasts to the first solid surface before boring; firing into open air does nothing
