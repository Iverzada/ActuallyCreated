package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.CompactingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Compacting recipe generator. Shows several inputs producing a single output.
 */
public class ActuallyCreatedCompactingRecipeGen extends CompactingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_compacting", b -> b
            .require(Items.CLAY_BALL)
            .require(Items.CLAY_BALL)
            .require(Items.CLAY_BALL)
            .require(Items.CLAY_BALL)
            .output(Items.CLAY));

    public ActuallyCreatedCompactingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
