# Hydroponicraft — Survival Prototype TODO

Goal: get the mod to a state where it can be loaded, crafted, and played in survival mode.

---

## ✅ Done

- Step 1–11 — All blueprint code steps complete and build-verified
- Digester block textures (6 face PNGs) + Blockbench model
- Digester GUI texture (`textures/gui/digester.png` — 256×166 sprite sheet)
- Mixer block textures (5 face PNGs) + Blockbench model
- Crafting recipes for all 3 machines (Digester, Mixer, Growth Bed) + fluid_pipe removed
- Fluid Pipe removed — using Create's own fluid pipes
- DigesterBlock + MixerBlock fixed to extend `KineticBlock implements IBE<T>` (shaft now works)
- `hasShaftTowards()` overridden on both — shaft connects on model-west face
- Growth Bed fluid capability fixed: blocked on Direction.UP, open on all other sides
- `nutrient_fluid` added to GrowthModifier at 1.0× speed, 0 fortune
- Large batch of Digester input recipes added (wheat, leaves, meats, berries, etc.)
- **Basic loop confirmed in game**: Digester → Create pipes → Growth Bed → crops grow and auto-harvest ✅
- **Growth Bed outputs drops downward to hopper** via IItemHandler — confirmed working ✅
- **Growth Bed does NOT auto-replant** — intentional design; player places a Create Deployer above to replant. Considered complete and correct. ✅

---

## 🔲 Remaining

### 🏃 Next session priorities

#### 1. Balance fixes

Fluid consumption and tick rate are currently too aggressive.

```
- Reduce Growth Bed tick rate significantly (current rate burns through fluid too fast)
- Reduce fluid consumption per growth tick drastically (currently 250 mB per tick)
```

#### 2. Digester redesign

Remove the GUI entirely. The Digester should be a simple hopper-fed machine with no right-click UI.

```
Rewrite DigesterBlock + DigesterBlockEntity:
1. Remove GUI — right-click should NOT open any inventory screen
2. Reduce to 1 input slot only (no output/byproduct slot)
3. Input only via hopper or belt from the top — no manual slot access
4. Shaft stays on model-west face (facing.getCounterClockWise()) — no change
5. Fluid output stays on model-east face (facing.getClockWise()) — no change
6. Remove DigesterMenu, DigesterScreen, MenuProvider wiring
7. Clean up any extra slots in DigesterBlockEntity beyond the single input slot
8. Remove debug log: LOGGER.info("Digester ticking, speed: " + getSpeed())
```

#### 3. Polish

```
- Fix fluid display names: en_us.json currently shows raw keys like
  "fluid_type.hydroponicraft.nutrient_fluid" in game. NeoForge 1.21.1 uses
  "fluid_type.hydroponicraft.<id>" format — verify keys match exactly.
- Fix creative tab not appearing in the creative menu (tab may be registered
  but not showing up — check registration order or tab key)
- Digester currently has more slots than needed — resolved by Digester redesign above
```

---

### 💻 After next session

#### 4. Mixer implementation and in-game testing

```
- Test Mixer in game: connect shaft, fill input tanks via Create pipes, verify output
- Add Enriched Solution + Yield Tonic recipes once nether_wart_fluid and
  chorus_fruit_fluid are registered (see Deferred Mixer Recipes below)
```

#### 5. Register nether_wart_fluid + chorus_fruit_fluid

```
Register in HydroponiCraftFluids.java using the same BaseFlowingFluid pattern:
- nether_wart_fluid: still, flowing, block, bucket
- chorus_fruit_fluid: still, flowing, block, bucket

Add Digester recipe JSONs:
- nether_wart_fluid: input = minecraft:nether_wart, outputMb = 500, baseTicks = 200
- chorus_fruit_fluid: input = minecraft:chorus_fruit, outputMb = 500, baseTicks = 250

Then add Mixer recipe JSONs:
- Enriched Solution: 1000 mB nutrient_fluid + 500 mB nether_wart_fluid → enriched_solution
- Yield Tonic: 1000 mB nutrient_fluid + 500 mB chorus_fruit_fluid → yield_tonic
```

---

## 🎨 Textures still needed (Blockbench + manual)

| Asset | Status |
|-------|--------|
| Growth Bed block textures + model | ❌ |
| Bucket icons for all 6 fluids | ❌ |

---

## 🧪 Deferred Mixer Recipes

| Solution | Status | Additive fluid needed |
|----------|--------|-----------------------|
| Enriched Solution | ❌ | `nether_wart_fluid` (not yet registered) |
| Yield Tonic | ❌ | `chorus_fruit_fluid` (not yet registered) |

---

## 🧱 Minimum to be survival playable

1. ✅ All 11 code steps complete
2. ✅ Digester + Mixer textures and models
3. ✅ Crafting recipes for all machines
4. ✅ Digester + Mixer shaft connections working
5. ✅ Basic loop confirmed in game (Digester → pipes → Growth Bed → harvest → hopper)
6. 🔲 Digester redesign (no GUI, 1 slot, shaft on east, output on west)
7. 🔲 Balance (tick rate + fluid consumption)
8. 🔲 Polish (fluid names, creative tab)
9. 🔲 Growth Bed textures + model

---

## 🚀 Testing in game

```
.\gradlew.bat runClient
```

Full loop to test:
1. Craft a Digester, Mixer, Growth Bed
2. Connect a Create shaft + hand crank to the Digester's east face (after redesign)
3. Drop rotten flesh into the Digester via hopper from above — it produces Nutrient Fluid
4. Pipe Nutrient Fluid out the west face into Growth Bed (any face except top)
5. Plant wheat on top of the Growth Bed
6. Place a Create Deployer above the Growth Bed pointed down, loaded with wheat seeds, to replant after harvest
7. Place a hopper below the Growth Bed to collect drops
8. Watch it grow, auto-harvest into the hopper, and get replanted by the Deployer

---

## Future Machines (not yet started)

### Chemical Synthesizer
- Input: 1 item slot + 1 fluid tank → Output: 1 item slot
- Driven by Create rotation (KineticBlockEntity)
- Recipe-based (JSON): item + fluid → item
- First recipe: Clay Ball + 1000 mB Accelerant Solution → C4
- Same structural pattern as Digester but with fluid input instead of fluid output
- No GUI — hopper-fed input, hopper-drained output

### Explosion Quarry
- Driven by Create rotation
- Input slots: Ender Pearls + C4 (separate slots)
- Configurable depth via right-click GUI (range: 10–100 blocks)
- Teleports C4 downward, detonates, teleports drops back up into output buffer
- Output buffer drainable by hoppers
- Filter slots: items matching filter are voided automatically (e.g. cobblestone, dirt)
- Consumption: X Ender Pearls + Y C4 per explosion cycle
- RPM affects cycle speed

### Monorail Seed System
Three linked blocks working together:
- **Rail Block**: thin ceiling-mounted block, connects in straight lines and 90° curves
- **Charger/Loader Block**: placed along rail, connects to Create rotation, has seed slot fed
  by hopper, charges Launcher mechanically + refills seeds when Launcher docks
- **Seed Launcher**: entity that travels along rail, capacity 8 seeds, RPM-based speed,
  detects Growth Beds below needing replanting, fires seed down when passing,
  returns to Charger when empty, max 1 active Launcher per rail network

---

## 📝 Notes

- All fluids use water sprites with color tints — no extra texture work needed
- Growth Bed currently uses vanilla placeholder textures — loads without crashing but looks rough
- Mixer input tanks must be filled by bucket or Create pipe — no GUI
- Growth Bed does NOT auto-replant — player uses a Create Deployer for that (by design)
- Digester shaft will move to east face after redesign (currently model-west)
