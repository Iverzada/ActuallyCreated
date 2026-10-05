package dev.actuallycreated.recipe.reconstructing;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import dev.actuallycreated.registry.ACRecipeTypes;
import dev.actuallycreated.registry.ACBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

/** Laser operation embedded in Create sequenced assemblies; not a standalone conversion. */
public class ReconstructingRecipe extends StandardProcessingRecipe<SingleRecipeInput> implements IAssemblyRecipe {
    public ReconstructingRecipe(ProcessingRecipeParams params) {
        super(ACRecipeTypes.RECONSTRUCTING, params);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override protected int getMaxInputCount() { return 1; }
    @Override protected int getMaxOutputCount() { return 1; }

    @Override public void addAssemblyIngredients(List<Ingredient> ingredients) {}
    @Override public void addRequiredMachines(Set<ItemLike> machines) {
        machines.add(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get());
    }
    @Override public Component getDescriptionForAssembly() {
        return Component.translatable("recipe.actuallycreated.assembly.reconstructing");
    }
    @Override public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> dev.actuallycreated.compat.jei.ReconstructingAssemblyStep::new;
    }
}
