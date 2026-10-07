package dev.actuallycreated.compat.jei;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.recipe.coffee.CoffeeMakingRecipe;
import dev.actuallycreated.registry.ACBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public class CoffeeMakingCategory implements IRecipeCategory<CoffeeMakingRecipe> {
    public static final RecipeType<CoffeeMakingRecipe> TYPE = RecipeType.create(ActuallyCreated.ID, "coffee_making", CoffeeMakingRecipe.class);
    private final IDrawable icon;
    public CoffeeMakingCategory(IGuiHelper gui) { icon = gui.createDrawableItemLike(ACBlocks.COFFEE_PRESS.get()); }
    @Override public RecipeType<CoffeeMakingRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.actuallycreated.coffee_making"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return 120; }
    @Override public int getHeight() { return 44; }
    @Override public void setRecipe(IRecipeLayoutBuilder builder, CoffeeMakingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 14).addIngredients(recipe.coffee());
        if (!recipe.ingredient().isEmpty()) builder.addSlot(RecipeIngredientRole.INPUT, 29, 14).addIngredients(recipe.ingredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 10).addIngredient(NeoForgeTypes.FLUID_STACK, recipe.result())
                .setFluidRenderer(recipe.amount(), false, 16, 24);
    }
}
