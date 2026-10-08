package dev.actuallycreated.gametest;

import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.registry.ACFluids;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class CoffeeMixingGameTests {
    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void basinMixesBlackCoffeeAndMilkIntoMilkCoffee(GameTestHelper h) {
        var holder = h.getLevel().getRecipeManager()
                .byKey(ActuallyCreated.asResource("mixing/milk_coffee"))
                .orElseThrow(() -> new AssertionError("Milk coffee mixing recipe was not loaded"));
        h.assertTrue(holder.value() instanceof MixingRecipe, "Milk coffee is not a Create mixing recipe");
        var recipe = (MixingRecipe) holder.value();
        var ingredients = recipe.getFluidIngredients();
        h.assertTrue(ingredients.size() == 2, "Milk coffee requires exactly two fluids");
        h.assertTrue(ingredients.stream().anyMatch(ingredient -> ingredient.amount() == 250
                && ingredient.test(new FluidStack((Fluid) ACFluids.BLACK_COFFEE.getSource(), 250))),
                "Recipe does not accept 250 mB of black coffee");
        h.assertTrue(ingredients.stream().anyMatch(ingredient -> ingredient.amount() == 250
                && ingredient.test(new FluidStack(NeoForgeMod.MILK.get(), 250))),
                "Recipe does not accept 250 mB of minecraft:milk");
        h.assertTrue(recipe.getFluidResults().size() == 1, "Recipe has an unexpected number of outputs");
        var result = recipe.getFluidResults().getFirst();
        h.assertTrue(result.getFluid() == ACFluids.MILK_COFFEE.getSource() && result.getAmount() == 500,
                "Recipe must produce 500 mB of milk coffee");
        h.succeed();
    }

    private CoffeeMixingGameTests() { }
}
