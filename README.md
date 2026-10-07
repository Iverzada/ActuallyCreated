# Actually Created

Ever looked at Create and Actually Additions and thought, “yeah, these two should
probably talk to each other”? That is pretty much the idea here.

Actually Created is a small integration addon for **Minecraft 1.21.1**, running on
**NeoForge**. It lets Create's kinetic machinery work with Actually Additions'
Atomic Reconstructor recipes, adds support for reconstruction during sequenced
assembly, and throws in a Coffee Press because no factory should run uncaffeinated.

## What does it add?

### Kinetic Atomic Reconstructor

This is the Atomic Reconstructor's rotation-powered cousin. Hook a shaft up to the
back, point the front at whatever deserves to be lasered and give it some RPM.
No FE required—Create has already convinced us that spinning things solves most
engineering problems.

It uses Actually Additions' own `actuallyadditions:laser` recipes straight from
the recipe manager, so recipes added or changed by datapacks and KubeJS work
automatically. In JEI, hover over the block and press `U` to see the usual Atomic
Reconstructor recipes; the addon does not add a second, duplicate category.

A few useful numbers:

- The machine uses **16 SU per RPM**.
- Its cooldown is `ceil(2560 / abs(RPM))` ticks.
- One shot converts up to **four items** from a single stack.
- With no rotation, it does exactly what you would expect: absolutely nothing.
- Its range follows Create's fan range setting, and solid obstacles block the beam.

Dropped items, Create depots and belts are all supported. When using a depot or
belt, aim through the space directly above it—the reconstructor sits one block
higher than the transport block. It checks for items whenever it is ready to fire,
so new items and moving belts are picked up automatically. Items it cannot process
are left alone, and depot results use Create's output buffer.

### The original Atomic Reconstructor gets invited too

Actually Additions' regular `actuallyadditions:atomic_reconstructor` can also hit
items on Create depots and belts, including items going through sequenced assembly.
Use the Actually Additions redstone configuration item to cycle through:

`Automatic → Pulse → Created → Automatic`

In **Created Mode**, it only fires when a depot or belt has an item with a valid
native laser recipe or an `actuallycreated:reconstructing` assembly step. Recipes
loaded from datapacks or KubeJS are found through the recipe manager, so you do not
need to teach the machine any new tricks by hand.

The machine checks every tick and may fire once per tick while a valid target is
still there. Redstone neither triggers nor disables Created Mode—it has politely
stepped aside for once. The normal range and **1,000 FE** firing cost still apply.
Native conversions also use the recipe's FE cost per item, while an assembly laser
step costs **1,000 FE**. If the machine runs out of energy, the remaining items stay
on the transport instead of vanishing into the void. Other lenses keep their normal
Actually Additions behaviour.

One small visual detail: the kinetic beam stays on the facing axis while accounting
for Actually Additions' particle-rendering offset, so the laser goes where the
machine is pointing instead of developing artistic ambitions.

### Sequenced assembly with lasers

Datapacks can place an `actuallycreated:reconstructing` operation inside a standard
Create `create:sequenced_assembly` recipe:

```json
{
  "type": "actuallycreated:reconstructing",
  "ingredients": [{ "item": "yourpack:incomplete_item" }],
  "results": [{ "id": "yourpack:incomplete_item" }]
}
```

This is an assembly operation, not a whole new family of conversion recipes. The
outer Create recipe still controls the transitional item, loop count and final
result. Each shot advances one item by one laser step. If an item is waiting for a
different step, native laser recipes leave it alone. JEI shows the laser step in
Create's regular sequenced assembly category.

### Coffee Press

The Coffee Press turns Actually Additions coffee beans—and an optional extra
ingredient—into drinkable coffee fluids using rotational power. It has a **4,000 mB**
internal tank, accepts fluid filters and outputs from the bottom. Put the finished
coffee into an Actually Additions cup with a Create spout, then send it to whoever
forgot to lubricate the gearbox.

The faster the press spins, the faster it brews, down to a minimum processing time
of 20 ticks. Its recipes are data-driven too, because hard-coded coffee menus are
how workplace arguments begin.

## License

Actually Created is licensed under the [MIT License](LICENSE).
