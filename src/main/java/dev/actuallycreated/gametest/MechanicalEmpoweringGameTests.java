package dev.actuallycreated.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.EmpowererRecipe;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.empowering.ClockworkDisplayStandBlockEntity;
import dev.actuallycreated.content.empowering.EmpoweringKinetics;
import dev.actuallycreated.content.empowering.MechanicalEmpoweringStandBlockEntity;
import dev.actuallycreated.registry.ACBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class MechanicalEmpoweringGameTests {
    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final BlockPos[] STANDS = {
            CENTER.north(3), CENTER.south(3), CENTER.west(3), CENTER.east(3)
    };

    @GameTest(template = "empowering_test")
    public static void emptyHandTakesDisplayedItems(GameTestHelper h) {
        var center = setup(h, 0, true);
        var stand = stands(h)[0];
        stand.getInventory().setStackInSlot(0, new ItemStack(Items.BRICK, 3));
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        h.useBlock(STANDS[0], player);
        h.assertTrue(stand.getDisplayedItem().isEmpty(), "Empty-hand click did not clear the display stand");
        h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BRICK)
                && player.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 3,
                "Empty-hand click did not return the displayed stack");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        center.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()));
        h.useBlock(CENTER, player);
        h.assertTrue(center.getDisplayedItem().isEmpty(), "Empty-hand click did not clear the center stand");
        h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).is(ActuallyItems.RESTONIA_CRYSTAL.get()),
                "Empty-hand click did not return the base item");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void rotationAndStructureGate(GameTestHelper h) {
        var center = setup(h, 0, true);
        restonia(center, stands(h));
        ticks(center, 60);
        h.assertTrue(center.getProgress() == 0, "Stopped center progressed");
        center.setSpeed(64);
        stands(h)[0].setSpeed(0);
        ticks(center, 60);
        h.assertTrue(center.getProgress() == 0, "Stopped display stand allowed progress");
        stands(h)[0].setSpeed(64);
        h.setBlock(STANDS[3], net.minecraft.world.level.block.Blocks.AIR);
        ticks(center, 60);
        h.assertTrue(center.getProgress() == 0, "Missing display stand allowed progress");
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.RESTONIA_CRYSTAL.get()), "Base was consumed");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void restoniaAndOrder(GameTestHelper h) {
        var center = setup(h, 64, true);
        var stands = stands(h);
        Item[] modifiers = {Items.BRICK, Items.REDSTONE, Items.RED_DYE, Items.NETHER_BRICK};
        center.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()));
        for (int i = 0; i < 4; i++) stands[i].getInventory().setStackInSlot(0, new ItemStack(modifiers[i], 2));
        ticks(center, 50);
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()), "Restonia result missing");
        for (var stand : stands)
            h.assertTrue(stand.getDisplayedItem().getCount() == 1, "Modifier count changed incorrectly");
        var output = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(CENTER), Direction.DOWN);
        h.assertTrue(output != null && output.extractItem(0, 1, false).is(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()),
                "Automation could not collect the result");
        h.assertTrue(center.getDisplayedItem().isEmpty(), "Output was duplicated after extraction");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void invalidAndRemovalAreSafe(GameTestHelper h) {
        var center = setup(h, 64, true);
        var stands = stands(h);
        restonia(center, stands);
        stands[0].getInventory().setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        ticks(center, 60);
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.RESTONIA_CRYSTAL.get()), "Invalid recipe changed base");
        h.assertTrue(stands[0].getDisplayedItem().is(Items.COBBLESTONE), "Invalid modifier consumed");
        stands[0].getInventory().setStackInSlot(0, new ItemStack(Items.RED_DYE));
        ticks(center, 20);
        stands[1].getInventory().setStackInSlot(0, ItemStack.EMPTY);
        ticks(center, 60);
        h.assertTrue(center.getProgress() == 0 && center.getDisplayedItem().is(ActuallyItems.RESTONIA_CRYSTAL.get()),
                "Removing a modifier duplicated or consumed the base");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void powerPauseAndResume(GameTestHelper h) {
        var center = setup(h, 64, true);
        restonia(center, stands(h));
        ticks(center, 20);
        int saved = center.getProgress();
        stands(h)[1].setSpeed(0);
        ticks(center, 50);
        h.assertTrue(center.getProgress() == saved, "Progress did not pause");
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.RESTONIA_CRYSTAL.get()), "Stopped recipe consumed base");
        stands(h)[1].setSpeed(64);
        ticks(center, 30);
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()), "Recipe did not resume");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void nativeRecipesAndTiming(GameTestHelper h) {
        var recipes = h.getLevel().getRecipeManager().getAllRecipesFor(ActuallyRecipes.Types.EMPOWERING.get());
        h.assertTrue(recipes.size() >= 2, "Native empowering recipes not loaded");
        h.assertTrue(MechanicalEmpoweringStandBlockEntity.processingTime(50, 16) == 200, "16 RPM timing");
        h.assertTrue(MechanicalEmpoweringStandBlockEntity.processingTime(50, 32) == 100, "32 RPM timing");
        h.assertTrue(MechanicalEmpoweringStandBlockEntity.processingTime(50, 64) == 50, "64 RPM timing");
        h.assertTrue(MechanicalEmpoweringStandBlockEntity.processingTime(50, 128) == 25, "128 RPM timing");
        h.assertTrue(MechanicalEmpoweringStandBlockEntity.processingTime(50, 256) == 13, "256 RPM timing");
        var center = setup(h, 64, true);
        center.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.DIAMATINE_CRYSTAL.get()));
        Item[] modifiers = {Items.CLAY_BALL, Items.CLAY, Items.LIGHT_BLUE_DYE, Items.CLAY_BALL};
        var stands = stands(h);
        for (int i = 0; i < 4; i++) stands[i].getInventory().setStackInSlot(0, new ItemStack(modifiers[i]));
        RecipeHolder<EmpowererRecipe> nativeRecipe = recipes.stream()
                .filter(holder -> holder.value().matches(center.getDisplayedItem(), stands[0].getDisplayedItem(),
                        stands[1].getDisplayedItem(), stands[2].getDisplayedItem(), stands[3].getDisplayedItem()))
                .findFirst().orElseThrow();
        ticks(center, MechanicalEmpoweringStandBlockEntity.processingTime(nativeRecipe.value().getTime(), 64));
        h.assertTrue(center.getDisplayedItem().is(ActuallyItems.EMPOWERED_DIAMATINE_CRYSTAL.get()),
                "Other native empowering recipe failed");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void stressCogAndAutomation(GameTestHelper h) {
        var center = setup(h, 0, true);
        h.assertTrue(BlockStressValues.getImpact(ACBlocks.MECHANICAL_EMPOWERING_STAND.get())
                == EmpoweringKinetics.CENTRAL_STRESS_IMPACT, "Center stress impact is not 16");
        h.assertTrue(BlockStressValues.getImpact(ACBlocks.CLOCKWORK_DISPLAY_STAND.get())
                == EmpoweringKinetics.DISPLAY_STRESS_IMPACT, "Stand stress impact is not 4");
        h.assertTrue(EmpoweringKinetics.CENTRAL_STRESS_IMPACT + 4 * EmpoweringKinetics.DISPLAY_STRESS_IMPACT == 32,
                "Full structure stress impact is not 32");
        h.assertTrue(ICogWheel.isSmallCog(ACBlocks.MECHANICAL_EMPOWERING_STAND.get())
                && ICogWheel.isSmallCog(ACBlocks.CLOCKWORK_DISPLAY_STAND.get()), "Horizontal cog interfaces missing");
        for (Direction direction : Direction.values())
            h.assertFalse(ACBlocks.MECHANICAL_EMPOWERING_STAND.get().hasShaftTowards(h.getLevel(),
                    h.absolutePos(CENTER), center.getBlockState(), direction), "Unexpected exposed shaft");
        var cap = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(STANDS[0]), Direction.UP);
        h.assertTrue(cap != null && cap.insertItem(0, new ItemStack(Items.BRICK), false).isEmpty(),
                "Stand item capability rejected insertion");
        h.assertTrue(cap.extractItem(0, 1, false).is(Items.BRICK), "Stand item capability rejected extraction");
        var centerCap = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(CENTER), Direction.UP);
        h.assertTrue(centerCap != null && centerCap.insertItem(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()), false).isEmpty(),
                "Center item capability rejected insertion");
        h.assertTrue(centerCap.extractItem(0, 1, false).isEmpty(), "Center exposed unprocessed base");
        h.succeed();
    }

    @GameTest(template = "empowering_test")
    public static void mechanicalArmsRecognizeBothBlocks(GameTestHelper h) {
        BlockPos armPos = new BlockPos(4, 1, 2);
        h.setBlock(armPos, AllBlocks.MECHANICAL_ARM.getDefaultState());
        ArmBlockEntity arm = (ArmBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(armPos));
        setup(h, 0, true);
        var standPoint = ArmInteractionPoint.create(h.getLevel(), h.absolutePos(STANDS[0]),
                h.getLevel().getBlockState(h.absolutePos(STANDS[0])));
        var centerPoint = ArmInteractionPoint.create(h.getLevel(), h.absolutePos(CENTER),
                h.getLevel().getBlockState(h.absolutePos(CENTER)));
        h.assertTrue(standPoint != null && centerPoint != null, "Arm point registration missing");
        h.assertTrue(standPoint.getSlotCount(arm) == 1 && centerPoint.getSlotCount(arm) == 1,
                "Arms cannot access stand inventories");
        h.assertTrue(standPoint.insert(arm, new ItemStack(Items.BRICK), false).isEmpty(),
                "Arm could not insert modifier");
        h.assertTrue(standPoint.extract(arm, 0, 1, false).is(Items.BRICK),
                "Arm could not extract modifier");
        h.succeed();
    }

    private static MechanicalEmpoweringStandBlockEntity setup(GameTestHelper h, float rpm, boolean allStands) {
        h.setBlock(CENTER, ACBlocks.MECHANICAL_EMPOWERING_STAND.get().defaultBlockState());
        var center = (MechanicalEmpoweringStandBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(CENTER));
        center.setSpeed(rpm);
        if (allStands) for (BlockPos pos : STANDS) {
            h.setBlock(pos, ACBlocks.CLOCKWORK_DISPLAY_STAND.get().defaultBlockState());
            ((ClockworkDisplayStandBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos))).setSpeed(rpm);
        }
        return center;
    }

    private static ClockworkDisplayStandBlockEntity[] stands(GameTestHelper h) {
        var result = new ClockworkDisplayStandBlockEntity[4];
        for (int i = 0; i < 4; i++)
            result[i] = (ClockworkDisplayStandBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(STANDS[i]));
        return result;
    }

    private static void restonia(MechanicalEmpoweringStandBlockEntity center, ClockworkDisplayStandBlockEntity[] stands) {
        center.getInventory().setStackInSlot(0, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()));
        Item[] modifiers = {Items.RED_DYE, Items.NETHER_BRICK, Items.REDSTONE, Items.BRICK};
        for (int i = 0; i < 4; i++) stands[i].getInventory().setStackInSlot(0, new ItemStack(modifiers[i]));
    }

    private static void ticks(MechanicalEmpoweringStandBlockEntity center, int count) {
        for (int i = 0; i < count; i++) center.tickProcessing();
    }

    private MechanicalEmpoweringGameTests() { }
}
