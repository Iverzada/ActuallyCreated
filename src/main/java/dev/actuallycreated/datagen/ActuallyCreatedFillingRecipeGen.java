package dev.actuallycreated.datagen;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.ActuallyCreated;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider.GeneratedRecipe;
import com.simibubi.create.api.data.recipe.FillingRecipeGen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Filling recipe generator. Shows an item combined with a fluid ingredient; the amount
 * is in millibuckets (1000 mB is one bucket).
 */
public class ActuallyCreatedFillingRecipeGen extends FillingRecipeGen {

    GeneratedRecipe EXAMPLE = create("actuallycreated_filling", b -> b
            .require(Items.BUCKET)
            .require(Fluids.WATER, 1000)
            .output(Items.WATER_BUCKET));

    public ActuallyCreatedFillingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ActuallyCreated.ID);
    }
}
