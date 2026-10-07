package dev.actuallycreated.registry;

import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import dev.actuallycreated.recipe.coffee.CoffeeMakingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;

public final class ACRecipeSerializers {
    public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<CoffeeMakingRecipe>> COFFEE_MAKING =
            ActuallyCreated.REGISTRATE.simple("coffee_making", Registries.RECIPE_SERIALIZER,
                    () -> new RecipeSerializer<>() {
                        @Override public MapCodec<CoffeeMakingRecipe> codec() { return CoffeeMakingRecipe.CODEC; }
                        @Override public StreamCodec<RegistryFriendlyByteBuf, CoffeeMakingRecipe> streamCodec() { return CoffeeMakingRecipe.STREAM_CODEC; }
                    });
    public static final RegistryEntry<RecipeSerializer<?>, StandardProcessingRecipe.Serializer<ReconstructingRecipe>> RECONSTRUCTING =
            ActuallyCreated.REGISTRATE.simple("reconstructing", Registries.RECIPE_SERIALIZER,
                    () -> new StandardProcessingRecipe.Serializer<>(ReconstructingRecipe::new));

    private ACRecipeSerializers() {}
    public static void register() {}
}
