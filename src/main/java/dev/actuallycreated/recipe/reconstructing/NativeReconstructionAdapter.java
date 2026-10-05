package dev.actuallycreated.recipe.reconstructing;

import java.util.Optional;
import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.LaserRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/** Uses the world's native laser recipes, including datapack reloads. */
public final class NativeReconstructionAdapter {
    private NativeReconstructionAdapter() {}

    public static Optional<RecipeHolder<LaserRecipe>> find(Level level, ItemStack input) {
        // LaserRecipe.matches(RecipeInput, Level) always returns false in AA 1.3.26.
        return level.getRecipeManager().getAllRecipesFor(ActuallyRecipes.Types.LASER.get()).stream()
                .filter(holder -> holder.value().matches(input))
                .findFirst();
    }
}
