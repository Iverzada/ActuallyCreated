package dev.actuallycreated.compat.jei;

import de.ellpeck.actuallyadditions.mod.jei.JEIActuallyAdditionsPlugin;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.registry.ACBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import dev.actuallycreated.recipe.coffee.CoffeeMakingRecipe;
import dev.actuallycreated.recipe.coffee.CoffeePressRecipeInput;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.minecraft.client.Minecraft;

@JeiPlugin
public class ActuallyCreatedJEI implements IModPlugin {
    @Override public ResourceLocation getPluginUid() {
        return ActuallyCreated.asResource("jei");
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get()),
                JEIActuallyAdditionsPlugin.LASER);
        registration.addRecipeCatalyst(new ItemStack(ACBlocks.COFFEE_PRESS.get()), CoffeeMakingCategory.TYPE);
    }

    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CoffeeMakingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var recipes = level.getRecipeManager().getAllRecipesFor(
                ACRecipeTypes.COFFEE_MAKING.<CoffeePressRecipeInput, CoffeeMakingRecipe>getType()).stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();
        registration.addRecipes(CoffeeMakingCategory.TYPE, recipes);
    }
}
