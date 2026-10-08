package dev.actuallycreated.gametest;

import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.dynamo.CrystallineDynamoBlock;
import dev.actuallycreated.content.dynamo.CrystallineDynamoBlockEntity;
import dev.actuallycreated.registry.ACBlocks;
import dev.actuallycreated.registry.ACItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;


@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class CrystallineDynamoGameTests {
    private static final BlockPos POS = new BlockPos(1, 2, 1);

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void everyCrystalHasSpecifiedYield(GameTestHelper h) {
        assertYield(h, ACItems.KINEK_CRYSTAL.get(), 128_000);
        assertYield(h, ACItems.MECHA_CRYSTAL.get(), 256_000);
        assertYield(h, ActuallyItems.RESTONIA_CRYSTAL.get(), 96_000);
        assertYield(h, ActuallyItems.PALIS_CRYSTAL.get(), 96_000);
        assertYield(h, ActuallyItems.VOID_CRYSTAL.get(), 96_000);
        assertYield(h, ActuallyItems.ENORI_CRYSTAL.get(), 204_800);
        assertYield(h, ActuallyItems.DIAMATINE_CRYSTAL.get(), 307_200);
        assertYield(h, ActuallyItems.EMERADIC_CRYSTAL.get(), 409_600);
        assertYield(h, ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get(), 288_000);
        assertYield(h, ActuallyItems.EMPOWERED_PALIS_CRYSTAL.get(), 288_000);
        assertYield(h, ActuallyItems.EMPOWERED_VOID_CRYSTAL.get(), 288_000);
        assertYield(h, ActuallyItems.EMPOWERED_ENORI_CRYSTAL.get(), 614_400);
        assertYield(h, ActuallyItems.EMPOWERED_DIAMATINE_CRYSTAL.get(), 921_600);
        assertYield(h, ActuallyItems.EMPOWERED_EMERADIC_CRYSTAL.get(), 1_228_800);
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void newCrystalsProduce80And160FePerTickAt64Rpm(GameTestHelper h) {
        var dynamo = machine(h, 64);
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ACItems.KINEK_CRYSTAL.get()));
        dynamo.setSpeed(64);
        dynamo.tickEnergy();
        h.assertTrue(dynamo.getLastProduced() == 80, "Kinek Crystal did not produce 80 FE/t");
        h.assertTrue(dynamo.isProcessing() && !dynamo.isProcessingEmpowered(),
                "Normal crystal did not activate the normal grinding effect");
        ticks(dynamo, 1599, 64);
        h.assertTrue(!dynamo.isProcessing(), "Grinder remained active after the crystal finished");
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() == 128_000,
                "Kinek Crystal total yield is not 128k FE");

        dynamo.getInventory().setStackInSlot(0, new ItemStack(ACItems.MECHA_CRYSTAL.get()));
        dynamo.setSpeed(64);
        dynamo.tickEnergy();
        h.assertTrue(dynamo.getLastProduced() == 160, "Mecha Crystal did not produce 160 FE/t");
        ticks(dynamo, 1599, 64);
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() == 384_000,
                "Mecha Crystal total yield is not 256k FE");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void restoniaAt64RpmTakes1600Ticks(GameTestHelper h) {
        var dynamo = machine(h, 64);
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()));
        ticks(dynamo, 1599, 64);
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() < 96_000, "Restonia finished before 1600 ticks");
        dynamo.setSpeed(64);
        dynamo.tickEnergy();
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() == 96_000, "Restonia did not produce 96k FE: got "
                + dynamo.getEnergyStorage().getEnergyStored() + ", speed " + dynamo.getSpeed() + ", work " + dynamo.getWork());
        h.assertTrue(dynamo.getActiveEnergy() == 0, "Crystal still active after 1600 ticks");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void empoweredAndSpeedScaleWithoutChangingYield(GameTestHelper h) {
        var dynamo = machine(h, 256);
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()));
        ticks(dynamo, 399, 256);
        h.assertTrue(dynamo.isProcessingEmpowered(), "Empowered crystal did not activate superheated effects");
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() < 288_000, "Empowered crystal finished too early");
        dynamo.setSpeed(256);
        dynamo.tickEnergy();
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() == 288_000, "Empowered Restonia did not produce 288k FE: got "
                + dynamo.getEnergyStorage().getEnergyStored() + ", speed " + dynamo.getSpeed() + ", work " + dynamo.getWork());
        h.assertTrue(dynamo.getActiveEnergy() == 0, "Empowered crystal still active");
        h.assertTrue(!dynamo.isProcessing(), "Superheated effects remained active after completion");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void stoppedAndFullDynamoDoNotWasteCrystal(GameTestHelper h) {
        var dynamo = machine(h, 0);
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.EMPOWERED_ENORI_CRYSTAL.get()));
        ticks(dynamo, 20, 0);
        h.assertTrue(dynamo.getInventory().getStackInSlot(0).getCount() == 1, "Stopped dynamo consumed crystal");
        h.assertTrue(!dynamo.isProcessing(), "Stopped dynamo displayed active grinding effects");
        dynamo.setSpeed(64);
        ticks(dynamo, 4000, 64);
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() >= 499_617
                && dynamo.getEnergyStorage().getEnergyStored() <= 500_000, "Buffer exceeded or missed 500k FE: got "
                + dynamo.getEnergyStorage().getEnergyStored() + ", speed " + dynamo.getSpeed() + ", work " + dynamo.getWork());
        h.assertTrue(dynamo.getActiveEnergy() == 614_400, "Crystal was lost when buffer filled");
        h.assertTrue(!dynamo.isProcessing(), "Full buffer kept the grinding effects active");
        dynamo.getEnergyStorage().extractEnergy(200_000, false);
        ticks(dynamo, 1600, 64);
        h.assertTrue(dynamo.getEnergyStorage().getEnergyStored() == 414_400, "Remaining crystal energy was lost");
        h.assertTrue(dynamo.getActiveEnergy() == 0, "Crystal never completed");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void inputAndShaftUseRequiredFaces(GameTestHelper h) {
        var dynamo = machine(h, 0);
        var state = dynamo.getBlockState().setValue(CrystallineDynamoBlock.FACING, Direction.NORTH);
        h.setBlock(POS, state);
        var block = ACBlocks.CRYSTALLINE_DYNAMO.get();
        h.assertTrue(block.hasShaftTowards(h.getLevel(), h.absolutePos(POS), state, Direction.SOUTH), "Shaft missing at rear");
        for (Direction side : Direction.values()) {
            if (side != Direction.SOUTH)
                h.assertFalse(block.hasShaftTowards(h.getLevel(), h.absolutePos(POS), state, side), "Shaft on " + side);
            var items = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(POS), side);
            h.assertTrue((items != null) == (side == Direction.UP), "Item access on wrong face: " + side);
        }
        var input = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(POS), Direction.UP);
        h.assertTrue(input.insertItem(0, new ItemStack(Items.COBBLESTONE), false).is(Items.COBBLESTONE), "Invalid fuel accepted");
        h.assertTrue(input.insertItem(0, new ItemStack(ActuallyItems.ENORI_CRYSTAL.get()), false).isEmpty(), "Crystal rejected");
        h.assertTrue(input.extractItem(0, 1, false).isEmpty(), "Automation extracted input crystal");
        h.assertTrue(h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, h.absolutePos(POS), Direction.UP) == null,
                "FE exposed through item input");
        h.assertTrue(h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, h.absolutePos(POS), Direction.SOUTH) == null,
                "FE exposed through rear shaft");
        var output = h.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, h.absolutePos(POS), Direction.NORTH);
        h.assertTrue(output != null && output.canExtract() && !output.canReceive(), "Front FE output is not extract only");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void droppedCrystalsEnterFromAboveWithoutLosingRemainder(GameTestHelper h) {
        var dynamo = machine(h, 0);
        var pos = h.absolutePos(POS);
        var block = ACBlocks.CRYSTALLINE_DYNAMO.get();
        var dropped = new ItemEntity(h.getLevel(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get(), 3));
        block.updateEntityAfterFallOn(h.getLevel(), dropped);
        h.assertTrue(!dropped.isAlive(), "Accepted dropped stack was not removed");
        h.assertTrue(dynamo.getStoredCrystalCount() == 3, "Dropped crystals did not enter the dynamo");

        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get(), 63));
        var excess = new ItemEntity(h.getLevel(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get(), 3));
        block.updateEntityAfterFallOn(h.getLevel(), excess);
        h.assertTrue(dynamo.getStoredCrystalCount() == 64, "Dynamo did not accept the available slot");
        h.assertTrue(excess.isAlive() && excess.getItem().getCount() == 2, "Excess dropped crystals were lost");

        var invalid = new ItemEntity(h.getLevel(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                new ItemStack(Items.COBBLESTONE));
        block.updateEntityAfterFallOn(h.getLevel(), invalid);
        h.assertTrue(invalid.isAlive() && invalid.getItem().is(Items.COBBLESTONE), "Invalid drop was consumed");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void emptyHandTakesWholeCrystalStack(GameTestHelper h) {
        var dynamo = machine(h, 0);
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.ENORI_CRYSTAL.get(), 12));
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var pos = h.absolutePos(POS);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        dynamo.getBlockState().useWithoutItem(h.getLevel(), player, hit);
        h.assertTrue(dynamo.getInventory().getStackInSlot(0).isEmpty(), "Right-click left crystals in the dynamo");
        h.assertTrue(player.getInventory().countItem(ActuallyItems.ENORI_CRYSTAL.get()) == 12,
                "Right-click did not return the full crystal stack");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void empoweredCrystalDoublesStressAndGogglesCountActiveFuel(GameTestHelper h) {
        var dynamo = machine(h, 64);
        float baseStress = dynamo.calculateStressApplied();
        h.assertTrue(baseStress > 0, "Base stress impact is missing");
        dynamo.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get(), 2));
        h.assertTrue(dynamo.calculateStressApplied() == baseStress * 2, "Queued empowered crystal did not double SU");
        dynamo.setSpeed(64);
        dynamo.tickEnergy();
        h.assertTrue(dynamo.getStoredCrystalCount() == 2, "Active crystal disappeared from the goggles count");
        h.assertTrue(dynamo.getActiveCrystal().is(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()),
                "Active crystal type was lost when grinding started");
        h.assertTrue(dynamo.calculateStressApplied() == baseStress * 2, "Active empowered crystal did not double SU");

        dynamo.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        ticks(dynamo, 1599, 64);
        h.assertTrue(dynamo.getStoredCrystalCount() == 0, "Completed crystal still counted in goggles");
        h.assertTrue(dynamo.getActiveCrystal().isEmpty(), "Completed crystal still appears in goggles");
        h.assertTrue(dynamo.calculateStressApplied() == baseStress, "SU did not return to baseline after grinding");
        h.succeed();
    }

    private static CrystallineDynamoBlockEntity machine(GameTestHelper h, float speed) {
        h.setBlock(POS, ACBlocks.CRYSTALLINE_DYNAMO.get().defaultBlockState());
        var dynamo = (CrystallineDynamoBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(POS));
        dynamo.tick();
        dynamo.setSpeed(speed);
        return dynamo;
    }

    private static void ticks(CrystallineDynamoBlockEntity dynamo, int count, float speed) {
        for (int i = 0; i < count; i++) {
            dynamo.setSpeed(speed);
            dynamo.tickEnergy();
        }
    }

    private static void assertYield(GameTestHelper h, net.minecraft.world.item.Item crystal, int expected) {
        int actual = CrystallineDynamoBlockEntity.energyFor(new ItemStack(crystal));
        h.assertTrue(actual == expected, "Crystal yield mismatch: expected " + expected + ", got " + actual);
    }
}
