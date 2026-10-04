package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.MillingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Milling recipe generator.
 */
public class ActuallyCreatedMillingRecipeGen extends MillingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_milling", b -> b
            .require(Items.COBBLESTONE)
            .output(Items.SAND)
            .duration(100));

    public ActuallyCreatedMillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
