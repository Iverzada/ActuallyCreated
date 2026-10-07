package dev.actuallycreated.registry;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import dev.actuallycreated.recipe.coffee.CoffeeMakingRecipe;

public final class ACRecipeTypes {
    public static final ResourceLocation COFFEE_MAKING_ID = ActuallyCreated.asResource("coffee_making");
    private static final RegistryEntry<RecipeType<?>, RecipeType<CoffeeMakingRecipe>> COFFEE_MAKING_ENTRY =
            ActuallyCreated.REGISTRATE.simple("coffee_making", Registries.RECIPE_TYPE,
                    () -> RecipeType.simple(COFFEE_MAKING_ID));
    public static final IRecipeTypeInfo COFFEE_MAKING = new IRecipeTypeInfo() {
        @Override public ResourceLocation getId() { return COFFEE_MAKING_ID; }
        @Override @SuppressWarnings("unchecked") public <T extends RecipeSerializer<?>> T getSerializer() { return (T) ACRecipeSerializers.COFFEE_MAKING.get(); }
        @Override @SuppressWarnings("unchecked") public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() { return (RecipeType<R>) COFFEE_MAKING_ENTRY.get(); }
    };
    public static final ResourceLocation RECONSTRUCTING_ID = ActuallyCreated.asResource("reconstructing");
    private static final RegistryEntry<RecipeType<?>, RecipeType<ReconstructingRecipe>> RECONSTRUCTING_ENTRY =
            ActuallyCreated.REGISTRATE.simple("reconstructing", Registries.RECIPE_TYPE,
                    () -> RecipeType.simple(RECONSTRUCTING_ID));

    public static final IRecipeTypeInfo RECONSTRUCTING = new IRecipeTypeInfo() {
        @Override public ResourceLocation getId() { return RECONSTRUCTING_ID; }
        @Override @SuppressWarnings("unchecked")
        public <T extends RecipeSerializer<?>> T getSerializer() { return (T) ACRecipeSerializers.RECONSTRUCTING.get(); }
        @Override @SuppressWarnings("unchecked")
        public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
            return (RecipeType<R>) RECONSTRUCTING_ENTRY.get();
        }
    };

    private ACRecipeTypes() {}
    public static void register() {}
}
