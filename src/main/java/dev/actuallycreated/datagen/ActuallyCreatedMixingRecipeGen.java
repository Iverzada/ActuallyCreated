package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.AllItems;
import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Mixing recipe generator. This one requires heat; drop the requiresHeat call for a
 * recipe with no heat requirement.
 */
public class ActuallyCreatedMixingRecipeGen extends MixingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_mixing", b -> b
            .require(Items.STONE)
            .require(Items.CLAY_BALL)
            .output(AllItems.EXAMPLE_RESULT.get())
            .requiresHeat(HeatCondition.HEATED));

    public ActuallyCreatedMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
