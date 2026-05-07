# Hydroponicraft

A hydroponics automation mod for NeoForge 1.21.1, designed for integration with the Create mod.
Convert organic waste into nutrient fluids, blend them into specialized growth solutions,
and pipe the results into Growth Beds that accelerate and enhance your crop yields automatically.

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

---

## C4 & Remote Detonator

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

### Remote Detonator

Detonates all matching C4 charges you own across all loaded chunks.

- **Right-click** (in air or on a block): detonate
- **Shift + Right-click**: cycle the selected color
- Tooltip shows the currently selected color
- Only detonates C4 blocks you placed yourself
- Only detonates blocks that match the selected color (uncolored/base C4 always matches regardless of selected color)
- Stacks to 1

**Crafting recipe (Remote Detonator):**
```
[      ] [Redstn] [      ]
[ Gold ] [Copper] [ Gold ]
[      ] [ Iron ] [      ]
```
- Redstone × 1
- Gold Ingot × 2
- Copper Ingot × 1
- Iron Ingot × 1

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
- Iron Block × 2
- Copper Ingot × 4
- Glass × 1
- Piston × 1
- Create Cogwheel × 1

---

### Digester
```
[ Copper ] [ Iron  ] [ Copper ]
[ Iron  ]  [Cogwheel] [ Iron  ]
[ Copper ] [ Iron  ] [ Copper ]
```
- Copper Ingot × 4
- Iron Ingot × 4
- Create Cogwheel × 1

---

### Mixer
```
[ Iron ] [ Glass ] [ Iron ]
[ Iron ] [Bucket ] [ Iron ]
[Copper] [ Iron  ] [Copper]
```
- Iron Ingot × 6
- Copper Ingot × 2
- Glass × 1
- Bucket × 1

---

### Growth Bed
```
[ Iron ] [ Sand ] [ Iron ]
[ Iron ] [Cobble] [ Iron ]
[ Iron ] [ Iron ] [ Iron ]
```
- Iron Ingot × 7
- Sand × 1
- Cobblestone × 1

---

## Progression

| Stage | Goal | How |
|---|---|---|
| Early | Basic automation | Digester + rotten flesh or leaves → Nutrient Fluid → Growth Bed |
| Early-Mid | First upgrade | Mixer + water → Nutrient Solution (1.5× speed) |
| Mid | Faster growth | Farm Nether Wart → Digester → Mixer → Enriched Solution (2.5×) |
| Mid-Late | Fortune harvests | Chorus Fruit (End) → Digester → Mixer → Yield Tonic (Fortune 2) |
| Late | Maximum throughput | Chain Mixer outputs → Accelerant Solution (4×) or Balanced Blend (2× + Fortune 1) |

---

## Notes

- Create fluid pipes are used for all fluid transport — no custom pipes in this mod
- The Digester, Mixer, and Chemical Synthesizer all expose `IFluidHandler` and
  `IItemHandler` capabilities, compatible with any mod that supports standard NeoForge capabilities
- Crops must be a vanilla `CropBlock` subclass (wheat, carrots, potatoes, beetroot, etc.)
  for the Growth Bed to detect and harvest them
- All active machines emit particles and sounds while processing — bubbles for the Digester,
  splashes for the Mixer, sparks for the Chemical Synthesizer, and green composter sparkles
  for the Growth Bed
- C4 can be dyed any of the 16 vanilla dye colors by combining it with a dye in a crafting table
