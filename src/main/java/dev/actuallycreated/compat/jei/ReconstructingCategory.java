package dev.actuallycreated.compat.jei;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

/** Small Create-style layout until the machine has final visuals. */
public class ReconstructingCategory extends CreateRecipeCategory<ReconstructingRecipe> {
    public ReconstructingCategory(Info<ReconstructingRecipe> info) {
        super(info);
    }

    @Override protected void setRecipe(IRecipeLayoutBuilder builder, ReconstructingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 51)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().getFirst());

        ProcessingOutput output = recipe.getRollableResults().getFirst();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 131, 50)
                .setBackground(getRenderedSlot(output), -1, -1)
                .addItemStack(output.getStack())
                .addRichTooltipCallback(addStochasticTooltip(output));
    }

    @Override protected void draw(ReconstructingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                                  double mouseX, double mouseY) {
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);
    }
}
