package dev.actuallycreated.gametest;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.registry.ACBlocks;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(ActuallyCreated.ID)
public class ReconstructorGameTests {
    @GameTest(template = "reconstructor_test")
    public static void recipeAndShaftRegistration(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(ACRecipeTypes.RECONSTRUCTING.getType(),
                new SingleRecipeInput(new ItemStack(Items.COAL)), level);
        if (recipe.isEmpty() || !recipe.get().value().getResultItem(level.registryAccess()).is(Items.DIAMOND)) {
            helper.fail("Reconstructing datapack recipe was not decoded");
            return;
        }

        RecipeType<SequencedAssemblyRecipe> assemblyType = AllRecipeTypes.SEQUENCED_ASSEMBLY.getType();
        boolean assemblyStepLoaded = level.getRecipeManager()
                .getAllRecipesFor(assemblyType).stream()
                .map(holder -> holder.value())
                .flatMap(assembly -> assembly.getSequence().stream())
                .anyMatch(step -> step.getRecipe().getType() == ACRecipeTypes.RECONSTRUCTING.getType());
        if (!assemblyStepLoaded) {
            helper.fail("Reconstructing assembly step was not decoded");
            return;
        }

        BlockState state = ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get().defaultBlockState()
                .setValue(KineticAtomicReconstructorBlock.HORIZONTAL_FACING, Direction.NORTH);
        var block = ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get();
        if (!block.hasShaftTowards(level, helper.absolutePos(net.minecraft.core.BlockPos.ZERO), state, Direction.SOUTH)
                || block.hasShaftTowards(level, helper.absolutePos(net.minecraft.core.BlockPos.ZERO), state, Direction.NORTH)
                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(16) != 160
                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(256) != 10
                || KineticAtomicReconstructorBlockEntity.cooldownForRpm(0) != Integer.MAX_VALUE) {
            helper.fail("Reconstructor shaft or cooldown invariant failed");
            return;
        }
        helper.succeed();
    }
}
