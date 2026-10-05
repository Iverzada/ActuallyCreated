package dev.actuallycreated.gametest;

import java.util.ArrayList;
import java.util.List;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.actuallycreated.recipe.reconstructing.NativeReconstructionAdapter;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.AllCreativeModeTabs;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.registry.ACBlocks;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(ActuallyCreated.ID)
public class ReconstructorGameTests {
        @GameTest(template = "reconstructor_test")
        public static void recipeAndShaftRegistration(GameTestHelper helper) {
                var level = helper.getLevel();
                if (level.getRecipeManager().byKey(ActuallyCreated.asResource("sequenced_assembly/laser_press_test"))
                                .isEmpty()) {
                        helper.fail("Laser / press / press / laser demonstration recipe is missing");
                        return;
                }
                if (NativeReconstructionAdapter.find(level, new ItemStack(Items.IRON_INGOT)).isEmpty()) {
                        helper.fail("Native Actually Additions laser recipes were not found");
                        return;
                }

                BlockState state = ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get().defaultBlockState()
                                .setValue(KineticAtomicReconstructorBlock.FACING, Direction.NORTH);
                var block = ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get();
                if (!block.hasShaftTowards(level, helper.absolutePos(net.minecraft.core.BlockPos.ZERO), state,
                                Direction.SOUTH)
                                || block.hasShaftTowards(level, helper.absolutePos(net.minecraft.core.BlockPos.ZERO),
                                                state, Direction.NORTH)
                                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(16) != 160
                                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(256) != 10
                                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(0) != Integer.MAX_VALUE) {
                        helper.fail("Reconstructor shaft or cooldown invariant failed");
                        return;
                }

                CreativeModeTab tab = AllCreativeModeTabs.MAIN_TAB.get();
                tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                                FeatureFlags.DEFAULT_FLAGS, true, level.registryAccess()));
                if (!tab.contains(new ItemStack(block))) {
                        helper.fail("Reconstructor block item is missing from the creative tab");
                        return;
                }
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void nativeDepotAndBelt(GameTestHelper helper) {
                var level = helper.getLevel();
                BlockPos machinePos = new BlockPos(1, 2, 1);
                BlockPos targetPos = new BlockPos(3, 1, 1);
                helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                helper.setBlock(targetPos.above(), Blocks.AIR);
                helper.setBlock(targetPos, AllBlocks.DEPOT.get());
                var depot = BlockEntityBehaviour.get(level, helper.absolutePos(targetPos), DepotBehaviour.TYPE);
                ItemStack expected = NativeReconstructionAdapter.find(level, new ItemStack(Items.IRON_INGOT))
                                .orElseThrow().value().getResultItem(level.registryAccess());
                depot.setCenteredHeldItem(new TransportedItemStack(new ItemStack(Items.IRON_INGOT, 6)));
                var machine = machine(helper, machinePos);
                machine.setSpeed(0);
                machine.tick();
                helper.assertTrue(depot.getHeldItemStack().getCount() == 6, "Stopped machine consumed input");
                machine.setSpeed(256);
                helper.setBlock(new BlockPos(2, 2, 1), Blocks.STONE);
                machine.tick();
                helper.assertTrue(depot.getHeldItemStack().getCount() == 6, "Laser passed through a wall");
                helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                machine.tick();
                helper.assertTrue(depot.getHeldItemStack().is(Items.IRON_INGOT)
                                && depot.getHeldItemStack().getCount() == 2,
                                "Depot did not preserve unprocessed input");
                helper.assertTrue(countDepot(depot, expected) == 4 * expected.getCount(),
                                "Depot output differs from native recipe");
                machine.tick();
                helper.assertTrue(depot.getHeldItemStack().getCount() == 2, "Cooldown was ignored");
                for (int i = 0; i < 9; i++)
                        machine.tick();
                helper.assertTrue(countDepot(depot, expected) == 6 * expected.getCount(),
                                "Depot lost items across shots");
                depot.clearContent();

                helper.setBlock(targetPos, AllBlocks.BELT.get().defaultBlockState()
                                .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL));
                var belt = (BeltBlockEntity) level.getBlockEntity(helper.absolutePos(targetPos));
                belt.setController(belt.getBlockPos());
                belt.beltLength = 1;
                belt.index = 0;
                var transported = new TransportedItemStack(new ItemStack(Items.IRON_INGOT, 6));
                transported.beltPosition = transported.prevBeltPosition = .5f;
                belt.getInventory().addItem(transported);
                belt.getInventory().write(level.registryAccess());
                for (int i = 0; i < 10; i++)
                        machine.tick();
                belt.getInventory().write(level.registryAccess());
                int remaining = belt.getInventory().getTransportedItems().stream()
                                .filter(t -> t.stack.is(Items.IRON_INGOT)).mapToInt(t -> t.stack.getCount()).sum();
                int converted = belt.getInventory().getTransportedItems().stream()
                                .filter(t -> ItemStack.isSameItemSameComponents(t.stack, expected))
                                .mapToInt(t -> t.stack.getCount()).sum();
                helper.assertTrue(remaining == 2 && converted == 4 * expected.getCount(),
                                "Belt conversion lost or duplicated items");
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void assemblyOnDepotAndBelt(GameTestHelper helper) {
                var level = helper.getLevel();
                var manager = level.getRecipeManager();
                List<RecipeHolder<?>> original = new ArrayList<>(manager.getRecipes());
                var assembly = new SequencedAssemblyRecipeBuilder(ActuallyCreated.asResource("test_only"))
                                .require(Items.IRON_INGOT).transitionTo(Items.IRON_INGOT).addOutput(Items.GOLD_INGOT, 1)
                                .loops(1).addStep(ReconstructingRecipe::new, b -> b)
                                .addStep(PressingRecipe::new, b -> b).build();
                // Exercise the real recipe codec without distributing a demonstration datapack.
                var ops = level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
                Recipe<?> decoded = Recipe.CODEC.parse(ops,
                                Recipe.CODEC.encodeStart(ops, assembly.value()).getOrThrow()).getOrThrow();
                List<RecipeHolder<?>> recipes = new ArrayList<>(original);
                recipes.add(new RecipeHolder<>(assembly.id(), decoded));
                try {
                        manager.replaceRecipes(recipes);
                        BlockPos targetPos = new BlockPos(3, 1, 1);
                        helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                        helper.setBlock(targetPos.above(), Blocks.AIR);
                        helper.setBlock(targetPos, AllBlocks.DEPOT.get());
                        var depot = BlockEntityBehaviour.get(level, helper.absolutePos(targetPos), DepotBehaviour.TYPE);
                        depot.setCenteredHeldItem(new TransportedItemStack(new ItemStack(Items.IRON_INGOT)));
                        var machine = machine(helper, new BlockPos(1, 2, 1));
                        machine.tick();
                        ItemStack intermediate = depot.getHeldItemStack().copy();
                        helper.assertTrue(intermediate.has(AllDataComponents.SEQUENCED_ASSEMBLY)
                                        && intermediate.get(AllDataComponents.SEQUENCED_ASSEMBLY).step() == 1,
                                        "Laser did not advance the depot assembly");
                        for (int i = 0; i < 10; i++)
                                machine.tick();
                        helper.assertTrue(ItemStack.matches(intermediate, depot.getHeldItemStack()),
                                        "Native conversion bypassed the required pressing step");
                        depot.clearContent();
                        helper.setBlock(targetPos, AllBlocks.BELT.get().defaultBlockState()
                                        .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL));
                        var belt = (BeltBlockEntity) level.getBlockEntity(helper.absolutePos(targetPos));
                        belt.setController(belt.getBlockPos());
                        belt.beltLength = 1;
                        var transported = new TransportedItemStack(new ItemStack(Items.IRON_INGOT));
                        transported.beltPosition = transported.prevBeltPosition = .5f;
                        belt.getInventory().addItem(transported);
                        belt.getInventory().write(level.registryAccess());
                        machine.tick();
                        belt.getInventory().write(level.registryAccess());
                        var items = belt.getInventory().getTransportedItems();
                        helper.assertTrue(items.size() == 1 && ItemStack.matches(items.getFirst().stack, intermediate),
                                        "Belt did not advance the assembly exactly once");
                } finally {
                        manager.replaceRecipes(original);
                }
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void shippedFourStepAssembly(GameTestHelper helper) {
                var level = helper.getLevel();
                BlockPos targetPos = new BlockPos(3, 1, 1);
                helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                helper.setBlock(targetPos.above(), Blocks.AIR);
                for (boolean onBelt : new boolean[] { false, true }) {
                        helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
                        helper.setBlock(targetPos, onBelt ? AllBlocks.BELT.get().defaultBlockState()
                                        .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                                        : AllBlocks.DEPOT.get().defaultBlockState());
                        var machine = machine(helper, new BlockPos(1, 2, 1));
                        // Ready but empty: the machine must detect the later insertion automatically.
                        machine.tick();
                        putTransported(helper, targetPos, new ItemStack(Items.AMETHYST_SHARD), onBelt);
                        machine.tick();
                        ItemStack current = getTransported(helper, targetPos, onBelt);
                        helper.assertTrue(current.has(AllDataComponents.SEQUENCED_ASSEMBLY)
                                        && current.get(AllDataComponents.SEQUENCED_ASSEMBLY).step() == 1,
                                        "First laser did not start assembly (belt=" + onBelt + ")");
                        for (int step = 1; step <= 2; step++) {
                                for (int tick = 0; tick < 10; tick++)
                                        machine.tick();
                                helper.assertTrue(ItemStack.matches(current, getTransported(helper, targetPos, onBelt)),
                                                "Laser skipped a pressing operation");
                                var press = SequencedAssemblyRecipe.getRecipe(level, current,
                                                com.simibubi.create.AllRecipeTypes.PRESSING.getType(),
                                                PressingRecipe.class).orElseThrow();
                                current = press.value().rollResults(level.random).getFirst();
                                helper.assertTrue(current.get(AllDataComponents.SEQUENCED_ASSEMBLY).step() == step + 1,
                                                "Press did not advance exactly one step");
                                putTransported(helper, targetPos, current, onBelt);
                        }
                        // No neighbour notification: changing the transported stack is enough.
                        machine.tick();
                        ItemStack result = getTransported(helper, targetPos, onBelt);
                        helper.assertTrue(result.is(dev.actuallycreated.AllItems.EXAMPLE_RESULT.get())
                                        && result.getCount() == 1 && !result.has(AllDataComponents.SEQUENCED_ASSEMBLY),
                                        "Final laser did not finish the four-step assembly");
                }
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void originalReconstructorOnTransports(GameTestHelper helper) {
                var level = helper.getLevel();
                BlockPos machinePos = new BlockPos(1, 2, 1);
                BlockPos targetPos = new BlockPos(3, 1, 1);
                helper.setBlock(machinePos, de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks.ATOMIC_RECONSTRUCTOR
                                .get()
                                .defaultBlockState()
                                .setValue(de.ellpeck.actuallyadditions.mod.blocks.BlockAtomicReconstructor.FACING,
                                                Direction.EAST));
                var machine = (de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor) level
                                .getBlockEntity(helper.absolutePos(machinePos));
                var api = de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI.methodHandler;
                var recipe = NativeReconstructionAdapter.find(level, new ItemStack(Items.IRON_INGOT)).orElseThrow()
                                .value();
                var expected = recipe.getResultItem(level.registryAccess());
                for (boolean onBelt : new boolean[] { false, true }) {
                        helper.setBlock(targetPos, onBelt ? AllBlocks.BELT.get().defaultBlockState()
                                        .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                                        : AllBlocks.DEPOT.get().defaultBlockState());
                        helper.setBlock(targetPos.above(), Blocks.AIR);
                        helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                        putTransported(helper, targetPos, new ItemStack(Items.IRON_INGOT, 6), onBelt);
                        machine.storage.setEnergyStored(0);
                        api.invokeReconstructor(machine);
                        helper.assertTrue(getTransported(helper, targetPos, onBelt).getCount() == 6,
                                        "Native laser fired without FE");
                        machine.storage.setEnergyStored(1000 + recipe.getEnergy() * 2);
                        helper.setBlock(new BlockPos(2, 2, 1), Blocks.STONE);
                        api.invokeReconstructor(machine);
                        helper.assertTrue(getTransported(helper, targetPos, onBelt).getCount() == 6,
                                        "Native laser passed through wall");
                        helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                        machine.storage.setEnergyStored(1000 + recipe.getEnergy() * 2);
                        api.invokeReconstructor(machine);
                        helper.assertTrue(machine.getEnergy() == 0, "Native laser charged wrong FE cost");
                        int remaining;
                        int converted;
                        if (onBelt) {
                                var belt = (BeltBlockEntity) level.getBlockEntity(helper.absolutePos(targetPos));
                                belt.getInventory().write(level.registryAccess());
                                remaining = belt.getInventory().getTransportedItems().stream()
                                                .filter(t -> t.stack.is(Items.IRON_INGOT))
                                                .mapToInt(t -> t.stack.getCount()).sum();
                                converted = belt.getInventory().getTransportedItems().stream()
                                                .filter(t -> ItemStack.isSameItemSameComponents(t.stack, expected))
                                                .mapToInt(t -> t.stack.getCount()).sum();
                        } else {
                                var depot = BlockEntityBehaviour.get(level, helper.absolutePos(targetPos),
                                                DepotBehaviour.TYPE);
                                remaining = countDepot(depot, new ItemStack(Items.IRON_INGOT));
                                converted = countDepot(depot, expected);
                        }
                        helper.assertTrue(remaining == 4 && converted == 2 * expected.getCount(),
                                        "Native transport conversion lost items");
                        putTransported(helper, targetPos, new ItemStack(Items.AMETHYST_SHARD), onBelt);
                        machine.storage.setEnergyStored(20000);
                        api.invokeReconstructor(machine);
                        ItemStack current = getTransported(helper, targetPos, onBelt);
                        helper.assertTrue(current.has(AllDataComponents.SEQUENCED_ASSEMBLY)
                                        && current.get(AllDataComponents.SEQUENCED_ASSEMBLY).step() == 1,
                                        "Native laser did not start assembly");
                        api.invokeReconstructor(machine);
                        helper.assertTrue(ItemStack.matches(current, getTransported(helper, targetPos, onBelt)),
                                        "Native laser skipped press");
                        for (int i = 0; i < 2; i++) {
                                current = SequencedAssemblyRecipe.getRecipe(level, current,
                                                com.simibubi.create.AllRecipeTypes.PRESSING.getType(),
                                                PressingRecipe.class).orElseThrow()
                                                .value().rollResults(level.random).getFirst();
                        }
                        putTransported(helper, targetPos, current, onBelt);
                        api.invokeReconstructor(machine);
                        helper.assertTrue(
                                        getTransported(helper, targetPos, onBelt)
                                                        .is(dev.actuallycreated.AllItems.EXAMPLE_RESULT.get()),
                                        "Native laser did not finish assembly");
                }
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void straightBeamInEveryFacing(GameTestHelper helper) {
                BlockPos origin = new BlockPos(4, 5, 6);
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                        var axis = net.minecraft.world.phys.Vec3.atLowerCornerOf(facing.getNormal());
                        // Include both transport heights and a belt's lateral item offset.
                        for (double height : new double[] { -0.75, 0.25 }) {
                                var target = origin.getCenter().add(axis.scale(3)).add(0, height, 0)
                                                .add(net.minecraft.world.phys.Vec3
                                                                .atLowerCornerOf(facing.getClockWise().getNormal())
                                                                .scale(.3));
                                var end = KineticAtomicReconstructorBlockEntity.laserEnd(origin, facing, target);
                                helper.assertTrue(end.equals(origin.relative(facing, 3)),
                                                "Beam is not on the facing axis");
                                helper.assertTrue(end.getY() == origin.getY(), "Horizontal beam tilts vertically");
                        }
                }
                helper.succeed();
        }

        @GameTest(template = "reconstructor_test")
        public static void originalAutomaticTransportUpdates(GameTestHelper helper) {
                var level = helper.getLevel();
                var api = de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI.methodHandler;
                var nativeBlock = de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks.ATOMIC_RECONSTRUCTOR.get();
                var expected = NativeReconstructionAdapter.find(level, new ItemStack(Items.IRON_INGOT)).orElseThrow()
                                .value().getResultItem(level.registryAccess());
                for (boolean onBelt : new boolean[] { false, true }) {
                        for (int machineY : new int[] { 1, 2 }) {
                                // A fresh block entity avoids carrying the previous case's shot cooldown.
                                helper.setBlock(new BlockPos(1, 1, 1), Blocks.AIR);
                                helper.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
                                BlockPos pos = new BlockPos(1, machineY, 1);
                                BlockPos target = new BlockPos(3, 1, 1);
                                helper.setBlock(new BlockPos(2, 2, 1), Blocks.AIR);
                                helper.setBlock(target.above(), Blocks.AIR);
                                var transportState = onBelt ? AllBlocks.BELT.get().defaultBlockState()
                                                .setValue(BeltBlock.SLOPE, BeltSlope.HORIZONTAL)
                                                : AllBlocks.DEPOT.get().defaultBlockState();
                                helper.setBlock(new BlockPos(2, 1, 1), transportState);
                                helper.setBlock(target, transportState);
                                if (onBelt) {
                                        var empty = (BeltBlockEntity) level
                                                        .getBlockEntity(helper.absolutePos(new BlockPos(2, 1, 1)));
                                        empty.setController(empty.getBlockPos());
                                        empty.beltLength = 1;
                                        empty.index = 0;
                                }
                                helper.setBlock(pos, nativeBlock.defaultBlockState()
                                                .setValue(de.ellpeck.actuallyadditions.mod.blocks.BlockAtomicReconstructor.FACING,
                                                                Direction.EAST));
                                var machine = (de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor) level
                                                .getBlockEntity(helper.absolutePos(pos));
                                machine.storage.setEnergyStored(20000);
                                // Run the real block ticker, initially with empty transports.
                                de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor.serverTick(
                                                level, machine.getBlockPos(), machine.getBlockState(), machine);
                                putTransported(helper, target, new ItemStack(Items.IRON_INGOT), onBelt);
                                machine.isPulseMode = true;
                                de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor.serverTick(
                                                level, machine.getBlockPos(), machine.getBlockState(), machine);
                                helper.assertTrue(getTransported(helper, target, onBelt).is(Items.IRON_INGOT),
                                                "Automatic shot ignored pulse mode");
                                machine.isPulseMode = false;
                                machine.isRedstonePowered = true;
                                de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor.serverTick(
                                                level, machine.getBlockPos(), machine.getBlockState(), machine);
                                helper.assertTrue(getTransported(helper, target, onBelt).is(Items.IRON_INGOT),
                                                "Automatic shot ignored redstone disable");
                                machine.isRedstonePowered = false;
                                de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor.serverTick(
                                                level, machine.getBlockPos(), machine.getBlockState(), machine);
                                helper.assertTrue(
                                                ItemStack.isSameItemSameComponents(
                                                                getTransported(helper, target, onBelt), expected),
                                                "Native automatic shot failed behind empty transport: belt=" + onBelt
                                                                + ", height=" + machineY);
                                putTransported(helper, target, new ItemStack(Items.IRON_INGOT), onBelt);
                                de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor.serverTick(
                                                level, machine.getBlockPos(), machine.getBlockState(), machine);
                                helper.assertTrue(getTransported(helper, target, onBelt).is(Items.IRON_INGOT),
                                                "Automatic cooldown was ignored");
                                // Explicit shots use the same path and still reach the item past an empty
                                // segment.
                                api.invokeReconstructor(machine);
                                helper.assertTrue(
                                                ItemStack.isSameItemSameComponents(
                                                                getTransported(helper, target, onBelt), expected),
                                                "Explicit shot failed behind empty transport");
                        }
                }
                helper.succeed();
        }

        private static void putTransported(GameTestHelper helper, BlockPos pos, ItemStack stack, boolean onBelt) {
                var level = helper.getLevel();
                if (!onBelt) {
                        var depot = BlockEntityBehaviour.get(level, helper.absolutePos(pos), DepotBehaviour.TYPE);
                        depot.clearContent();
                        depot.setCenteredHeldItem(new TransportedItemStack(stack.copy()));
                        return;
                }
                var belt = (BeltBlockEntity) level.getBlockEntity(helper.absolutePos(pos));
                belt.setController(belt.getBlockPos());
                belt.beltLength = 1;
                belt.index = 0;
                belt.getInventory().getTransportedItems().clear();
                var item = new TransportedItemStack(stack.copy());
                item.beltPosition = item.prevBeltPosition = .5f;
                belt.getInventory().addItem(item);
                belt.getInventory().write(level.registryAccess());
        }

        private static ItemStack getTransported(GameTestHelper helper, BlockPos pos, boolean onBelt) {
                var level = helper.getLevel();
                if (!onBelt)
                        return BlockEntityBehaviour.get(level, helper.absolutePos(pos), DepotBehaviour.TYPE)
                                        .getHeldItemStack().copy();
                var belt = (BeltBlockEntity) level.getBlockEntity(helper.absolutePos(pos));
                belt.getInventory().write(level.registryAccess());
                return belt.getInventory().getTransportedItems().getFirst().stack.copy();
        }

        private static KineticAtomicReconstructorBlockEntity machine(GameTestHelper helper, BlockPos pos) {
                helper.setBlock(pos, ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get().defaultBlockState()
                                .setValue(KineticAtomicReconstructorBlock.FACING, Direction.EAST));
                var machine = (KineticAtomicReconstructorBlockEntity) helper.getLevel()
                                .getBlockEntity(helper.absolutePos(pos));
                machine.tick(); // Initialize Create kinetics before supplying the test rotation.
                machine.setSpeed(256);
                return machine;
        }

        private static int countDepot(DepotBehaviour depot, ItemStack expected) {
                int count = 0;
                for (int i = 0; i < depot.itemHandler.getSlots(); i++) {
                        ItemStack stack = depot.itemHandler.getStackInSlot(i);
                        if (ItemStack.isSameItemSameComponents(stack, expected))
                                count += stack.getCount();
                }
                return count;
        }

}
