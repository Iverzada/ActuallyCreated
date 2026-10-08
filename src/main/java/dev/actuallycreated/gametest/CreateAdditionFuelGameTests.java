package dev.actuallycreated.gametest;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.mod.crafting.LiquidFuelRecipe;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class CreateAdditionFuelGameTests {
    private static final String RECIPE_ROOT = "compat/createaddition/";

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void optionalFuelRecipes(GameTestHelper h) {
        boolean installed = ModList.get().isLoaded("createaddition");

        checkGenerator(h, installed, "seed_oil", 240, 20);
        checkGenerator(h, installed, "bioethanol", 1200, 500);

        checkBurner(h, installed, "canola_oil", 7200, false);
        checkBurner(h, installed, "refined_canola_oil", 12000, false);
        checkBurner(h, installed, "crystallized_oil", 18000, false);
        checkBurner(h, installed, "empowered_oil", 12000, true);
        h.succeed();
    }

    private static void checkGenerator(GameTestHelper h, boolean installed, String fluidName, int burnTime, int energyPerTick) {
        Recipe<?> recipe = recipe(h, "liquid_fuel/" + fluidName, installed);
        if (!installed) return;

        h.assertTrue(recipe instanceof LiquidFuelRecipe, fluidName + " is not an AA liquid fuel recipe");
        LiquidFuelRecipe fuel = (LiquidFuelRecipe) recipe;
        h.assertTrue(fuel.getFuelAmount() == 50, fluidName + " must use 50 mB per cycle");
        h.assertTrue(fuel.getBurnTime() == burnTime, fluidName + " has the wrong burn time");
        h.assertTrue(fuel.getTotalEnergy() / fuel.getBurnTime() == energyPerTick,
                fluidName + " has the wrong CF/t rate");
        h.assertTrue(fuel.matches(fluid("createaddition:" + fluidName, 50)),
                fluidName + " does not match its registered fluid");
        h.assertTrue(ActuallyAdditionsAPI.LIQUID_FUEL_RECIPES.stream().anyMatch(holder -> holder.value() == fuel),
                fluidName + " was not added to the Oil Generator's fuel list");
    }

    private static void checkBurner(GameTestHelper h, boolean installed, String fluidName, int burnTime, boolean superheated) {
        Recipe<?> recipe = recipe(h, "liquid_burning/" + fluidName, installed);
        if (!installed) return;

        h.assertTrue(recipe instanceof ProcessingRecipe<?, ?>, fluidName + " is not a processing recipe");
        ProcessingRecipe<?, ?> burning = (ProcessingRecipe<?, ?>) recipe;
        h.assertTrue(burning.getFluidIngredients().size() == 1, fluidName + " must have one fluid input");
        h.assertTrue(burning.getFluidIngredients().getFirst().amount() == 1000,
                fluidName + " must be specified per bucket");
        h.assertTrue(burning.getFluidIngredients().getFirst().test(fluid("actuallyadditions:" + fluidName, 1000)),
                fluidName + " does not match its registered fluid");
        try {
            h.assertTrue((int) recipe.getClass().getMethod("getBurnTime").invoke(recipe) == burnTime,
                    fluidName + " has the wrong burn time");
            h.assertTrue((boolean) recipe.getClass().getMethod("isSuperheated").invoke(recipe) == superheated,
                    fluidName + " has the wrong heat level");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not read Create Crafts & Additions liquid burning recipe", e);
        }
    }

    private static Recipe<?> recipe(GameTestHelper h, String path, boolean installed) {
        var holder = h.getLevel().getRecipeManager().byKey(ActuallyCreated.asResource(RECIPE_ROOT + path));
        h.assertTrue(holder.isPresent() == installed, "Optional fuel recipe " + path + " has the wrong load state");
        return holder.map(value -> value.value()).orElse(null);
    }

    private static FluidStack fluid(String id, int amount) {
        return new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(id)), amount);
    }

    private CreateAdditionFuelGameTests() { }
}
