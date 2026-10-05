package dev.actuallycreated.compat.jei;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import dev.actuallycreated.registry.ACBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;

/** Drawn inside Create's existing assembly category; registers no JEI category or recipes. */
public class ReconstructingAssemblyStep extends SequencedAssemblySubCategory {
    public ReconstructingAssemblyStep() { super(25); }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        // Index the machines on this recipe, rather than showing every unrelated assembly.
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
                .addItemStack(new ItemStack(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get()))
                .addItemStack(new ItemStack(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.get()));
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        graphics.renderItem(new ItemStack(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get()), 4, 45);
        graphics.fill(11, 63, 14, 77, 0xff1b6fff);
    }
}
