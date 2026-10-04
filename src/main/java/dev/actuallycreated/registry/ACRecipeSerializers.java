package dev.actuallycreated.registry;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ACRecipeSerializers {
    public static final RegistryEntry<RecipeSerializer<?>, StandardProcessingRecipe.Serializer<ReconstructingRecipe>> RECONSTRUCTING =
            ActuallyCreated.REGISTRATE.simple("reconstructing", Registries.RECIPE_SERIALIZER,
                    () -> new StandardProcessingRecipe.Serializer<>(ReconstructingRecipe::new));

    private ACRecipeSerializers() {}
    public static void register() {}
}
