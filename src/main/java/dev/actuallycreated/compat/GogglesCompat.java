package dev.actuallycreated.compat;

import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import net.minecraft.world.item.ItemStack;

public final class GogglesCompat {
    private GogglesCompat() { }

    public static boolean isActuallyAdditionsGoggles(ItemStack stack) {
        return stack.is(ActuallyItems.ENGINEERS_GOGGLES.get())
                || stack.is(ActuallyItems.ENGINEERS_GOGGLES_ADVANCED.get());
    }
}
