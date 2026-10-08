package dev.actuallycreated.gametest;

import com.simibubi.create.AllItems;
import de.ellpeck.actuallyadditions.mod.crafting.LaserRecipe;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.registry.ACItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class CrystalLaserGameTests {
    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void andesiteAlloyBecomesKinekCrystal(GameTestHelper h) {
        assertLaserRecipe(h, "kinek_crystal", AllItems.ANDESITE_ALLOY.get(), ACItems.KINEK_CRYSTAL.get(), 80);
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void brassIngotBecomesMechaCrystal(GameTestHelper h) {
        assertLaserRecipe(h, "mecha_crystal", AllItems.BRASS_INGOT.get(), ACItems.MECHA_CRYSTAL.get(), 100);
        h.succeed();
    }

    private static void assertLaserRecipe(GameTestHelper h, String id, Item input, Item output, int energy) {
        var holder = h.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(ActuallyCreated.ID, "laser/" + id))
                .orElseThrow(() -> new AssertionError("Laser recipe not loaded: " + id));
        h.assertTrue(holder.value() instanceof LaserRecipe, "Recipe is not an Actually Additions laser recipe: " + id);
        var recipe = (LaserRecipe) holder.value();
        h.assertTrue(recipe.getInput().test(new ItemStack(input)), "Wrong crystal input: " + id);
        h.assertTrue(recipe.getResultItem(h.getLevel().registryAccess()).is(output), "Wrong crystal output: " + id);
        h.assertTrue(recipe.getEnergy() == energy, "Wrong laser energy: " + id);
    }

    private CrystalLaserGameTests() { }
}
