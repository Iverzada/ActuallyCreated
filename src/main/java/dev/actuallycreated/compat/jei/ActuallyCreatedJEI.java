package dev.actuallycreated.compat.jei;

import de.ellpeck.actuallyadditions.mod.jei.JEIActuallyAdditionsPlugin;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.registry.ACBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class ActuallyCreatedJEI implements IModPlugin {
    @Override public ResourceLocation getPluginUid() {
        return ActuallyCreated.asResource("jei");
    }

    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR.get()),
                JEIActuallyAdditionsPlugin.LASER);
    }
}
