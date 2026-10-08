package dev.actuallycreated.mixin;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GogglesItem.class)
public abstract class CreateGogglesMixin implements IGoggles {
    @Override
    public boolean displaySpectralMobs() {
        return false;
    }
}
