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
- **Chemical Synthesizer** — fully implemented (KineticBlock, 2-slot item handler, fluid tank, recipe system, JSON recipes, crafting recipe, model/loot/lang) ✅
- **C4 block** — placeable, 6 directions, owner UUID stored in BlockEntity, 5-stage explosion over 16 ticks (strength 10→11→12→11→10), detonated via Remote Detonator only ✅
- **C4 colored variants** — 16 dye colors, each a separate block; dye recipe (shapeless) for each ✅
- **Remote Detonator** — color cycling (shift+click), ownership check, scans all loaded chunks, crafting recipe, tooltip ✅
- **Machine feedback** — particles and sounds added to all 4 machines: bubbles (Digester), splashes (Mixer), composter/happy-villager (Growth Bed), smoke+flame+flash (Chemical Synthesizer) ✅
- **Auto-copy to mods folder** — `copyToMods` Gradle task deploys jar to PrismLauncher on every build ✅

---

## 🔲 Remaining

#### 1. Balance fixes

Growth Bed tick rate and fluid consumption are too aggressive.

```
- Reduce Growth Bed BASE_INTERVAL (currently 100 ticks — too fast)
- Reduce fluid consumption per growth tick (currently 25 mB — too frequent)
```

#### 2. Digester redesign

The current Digester still has a `useWithoutItem()` that ejects the input item on right-click.
Remove it — the Digester should be a silent hopper-fed machine with zero player interaction.

```
- Remove useWithoutItem() from DigesterBlock
- Verify shaft face and fluid output face match README docs
```

#### 3. Missing Mixer recipes

`nether_wart_fluid` and `chorus_fruit_fluid` are registered but their Mixer recipes are not.

```
Add recipe JSONs:
- mixer_enriched_solution.json: 1000 mB nutrient_fluid + 500 mB nether_wart_fluid → enriched_solution
- mixer_yield_tonic.json: 1000 mB nutrient_fluid + 500 mB chorus_fruit_fluid → yield_tonic
```

#### 4. Polish

```
- Verify fluid display names in-game (keys: fluid_type.hydroponicraft.<id>)
- Verify creative tab appears in creative menu
```

---

## 🎨 Textures still needed (Blockbench + manual)

| Asset | Status |
|-------|--------|
| Growth Bed block textures + model | ❌ |
| Bucket icons for all 6 fluids | ❌ |

---

## 🧪 Deferred Mixer Recipes

| Solution | Status | Note |
|----------|--------|------|
| Enriched Solution | ❌ | `nether_wart_fluid` is registered; just needs recipe JSON |
| Yield Tonic | ❌ | `chorus_fruit_fluid` is registered; just needs recipe JSON |

---

## 🧱 Survival playable checklist

1. ✅ All blueprint code steps complete
2. ✅ Digester + Mixer textures and models
3. ✅ Crafting recipes for all machines
4. ✅ Shaft connections working
5. ✅ Basic loop confirmed in game
6. ✅ Chemical Synthesizer + C4 + Remote Detonator
7. 🔲 Balance (tick rate + fluid consumption)
8. 🔲 Missing Mixer recipes (Enriched Solution, Yield Tonic)
9. 🔲 Polish (fluid names, creative tab)
10. 🔲 Growth Bed textures + model

---

## 🚀 Testing in game

```
.\gradlew.bat runClient
```

Full loop to test:
1. Craft a Digester, Mixer, Growth Bed
2. Connect a Create shaft + hand crank to the Digester's shaft face
3. Drop rotten flesh into the Digester via hopper from above — produces Nutrient Fluid
4. Pipe Nutrient Fluid into Growth Bed (any face except top)
5. Plant wheat on top of the Growth Bed
6. Place a Create Deployer above pointed down, loaded with wheat seeds, to replant after harvest
7. Place a hopper below the Growth Bed to collect drops
8. Watch it grow, auto-harvest into the hopper, and get replanted by the Deployer

---

## Future Machines (not yet started)

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
