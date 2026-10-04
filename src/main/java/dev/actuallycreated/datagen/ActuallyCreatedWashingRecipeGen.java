package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.AllItems;
import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.WashingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

/**
 * Splashing recipe generator.
 */
public class ActuallyCreatedWashingRecipeGen extends WashingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_washing",
            b -> b.require(Items.DIRT).output(AllItems.EXAMPLE_ITEM.get()));

    public ActuallyCreatedWashingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
