package dev.actuallycreated.recipe.coffee;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.actuallycreated.registry.ACRecipeSerializers;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public record CoffeeMakingRecipe(Ingredient coffee, int coffeeCount, Ingredient ingredient,
        ResourceLocation resultFluid, int amount, int duration) implements Recipe<CoffeePressRecipeInput> {
    public static final MapCodec<CoffeeMakingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("coffee").forGetter(CoffeeMakingRecipe::coffee),
            Codec.INT.optionalFieldOf("coffee_count", 1).forGetter(CoffeeMakingRecipe::coffeeCount),
            Ingredient.CODEC.optionalFieldOf("ingredient", Ingredient.EMPTY).forGetter(CoffeeMakingRecipe::ingredient),
            ResourceLocation.CODEC.fieldOf("result_fluid").forGetter(CoffeeMakingRecipe::resultFluid),
            Codec.INT.optionalFieldOf("amount", 250).forGetter(CoffeeMakingRecipe::amount),
            Codec.INT.optionalFieldOf("duration", 1280).forGetter(CoffeeMakingRecipe::duration)
    ).apply(i, CoffeeMakingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoffeeMakingRecipe> STREAM_CODEC =
            StreamCodec.of((b, r) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(b, r.coffee);
                b.writeVarInt(r.coffeeCount);
                Ingredient.CONTENTS_STREAM_CODEC.encode(b, r.ingredient);
                ResourceLocation.STREAM_CODEC.encode(b, r.resultFluid);
                b.writeVarInt(r.amount);
                b.writeVarInt(r.duration);
            }, b -> new CoffeeMakingRecipe(Ingredient.CONTENTS_STREAM_CODEC.decode(b), b.readVarInt(),
                    Ingredient.CONTENTS_STREAM_CODEC.decode(b), ResourceLocation.STREAM_CODEC.decode(b),
                    b.readVarInt(), b.readVarInt()));

    @Override public boolean matches(CoffeePressRecipeInput input, Level level) {
        return input.coffee().getCount() >= coffeeCount && coffee.test(input.coffee())
                && (ingredient.isEmpty() ? input.ingredient().isEmpty() : ingredient.test(input.ingredient()));
    }
    public FluidStack result() { return new FluidStack(BuiltInRegistries.FLUID.get(resultFluid), amount); }
    @Override public ItemStack assemble(CoffeePressRecipeInput input, HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public RecipeSerializer<?> getSerializer() { return ACRecipeSerializers.COFFEE_MAKING.get(); }
    @Override public RecipeType<?> getType() { return ACRecipeTypes.COFFEE_MAKING.getType(); }
}
