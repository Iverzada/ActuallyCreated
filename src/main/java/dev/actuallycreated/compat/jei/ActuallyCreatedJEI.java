package dev.actuallycreated.compat.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import dev.actuallycreated.registry.ACBlocks;
import dev.actuallycreated.registry.ACRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/** Optional JEI entry point, discovered only when JEI is installed. */
@JeiPlugin
public class ActuallyCreatedJEI implements IModPlugin {
    private final CreateRecipeCategory<ReconstructingRecipe> reconstructing =
            new CreateRecipeCategory.Builder<>(ReconstructingRecipe.class)
                    .addTypedRecipes(ACRecipeTypes.RECONSTRUCTING)
                    .catalyst(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR::get)
                    .itemIcon(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get())
                    .emptyBackground(177, 70)
                    .build(ACRecipeTypes.RECONSTRUCTING_ID, ReconstructingCategory::new);

    @Override public ResourceLocation getPluginUid() {
        return ActuallyCreated.asResource("jei_plugin");
    }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(reconstructing);
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        reconstructing.registerRecipes(registration);
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        reconstructing.registerCatalysts(registration);
    }
}
