package dev.actuallycreated.recipe.coffee;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record CoffeePressRecipeInput(ItemStack coffee, ItemStack ingredient) implements RecipeInput {
    @Override public ItemStack getItem(int index) { return index == 0 ? coffee : ingredient; }
    @Override public int size() { return 2; }
}
