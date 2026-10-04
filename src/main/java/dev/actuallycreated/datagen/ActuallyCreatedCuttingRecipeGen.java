package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.CuttingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Cutting recipe generator.
 */
public class ActuallyCreatedCuttingRecipeGen extends CuttingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_cutting", b -> b
            .require(Items.OAK_LOG)
            .output(Items.OAK_PLANKS, 6)
            .duration(50));

    public ActuallyCreatedCuttingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
