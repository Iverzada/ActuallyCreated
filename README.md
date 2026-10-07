# Actually Created

Create / Actually Additions integration for Minecraft 1.21.1 (NeoForge).

## Kinetic Atomic Reconstructor

The kinetic reconstructor uses Actually Additions' native `actuallyadditions:laser`
recipes directly from the world recipe manager. Datapack changes to those recipes
apply automatically. The kinetic block is a catalyst for Actually Additions' existing JEI category:
press U over it to see native Atomic Reconstructor recipes. No duplicate conversion
category is registered.

Connect rotation to the rear shaft and aim the front along the item path. The
machine consumes 16 SU per RPM. Its cooldown is `ceil(2560 / abs(RPM))` ticks;
without rotation it does not fire. Each shot converts up to four input items from
one stack, using the native recipe's output and count. Rotation powers the kinetic
variant instead of FE. Range follows Create's fan configuration; obstacles stop it.

Dropped items, Create depots and belts are supported. Aim along the space above
the depot/belt (the machine is one block higher than the transport block).
The machine checks live items each ready tick, so insertion, belt movement and
assembly updates automatically trigger a visible laser shot, respecting the RPM
cooldown. Unprocessed items are preserved; depot outputs use Create's output buffer.

## Original Actually Additions reconstructor

The original `actuallyadditions:atomic_reconstructor` conversion lens also processes
Create depots and belts in its beam path, including the laser assembly operation.
Use the Actually Additions redstone configuration item to cycle Automatic → Pulse →
Created → Automatic. In Created Mode, the reconstructor fires only when a depot or
belt holds an item with a currently valid native `actuallyadditions:laser` recipe
or `actuallycreated:reconstructing` assembly step. Recipes loaded through datapacks
or KubeJS are discovered through the recipe manager. It checks every tick and can
fire once per tick while a valid target remains. Redstone does not trigger or disable Created Mode.
The native range and 1,000 FE firing cost apply. Native conversions additionally
consume the recipe's FE cost per item; assembly laser steps consume 1,000 FE.
Insufficient energy leaves the remaining input on the transport. Other lens types
keep their original behaviour.

The kinetic beam stays on the facing axis and accounts for Actually Additions'
particle renderer centering offset.

## Sequenced assembly

Datapacks can embed an `actuallycreated:reconstructing` operation in a
`create:sequenced_assembly` sequence using Create's standard processing step format:

```json
{
  "type": "actuallycreated:reconstructing",
  "ingredients": [{ "item": "yourpack:incomplete_item" }],
  "results": [{ "id": "yourpack:incomplete_item" }]
}
```

This is an assembly operation only, not a separate family of conversion recipes.
The enclosing Create recipe supplies the transitional item, loops and final result.
Each shot advances one item by one laser step. An item waiting for a different step
is not converted by native laser recipes. The laser step is displayed inside
Create's existing sequenced assembly JEI category.

## Development

- `./gradlew build`: compile and package the addon.
- `./gradlew runGameTestServer`: verify native recipes, depot/belt conversions,
  assembly decoding/progression, cooldown, obstacles and item conservation.
- `./gradlew runData`: regenerate models, translations and loot tables.
- `./gradlew runClient`: launch the development client.

Dependencies: Create 6.0.10 and Actually Additions 1.3.26. The development runtime
also includes Patchouli, PatchouliProvider and Curios for Actually Additions.

Licensed under MIT; see [LICENSE](LICENSE).
