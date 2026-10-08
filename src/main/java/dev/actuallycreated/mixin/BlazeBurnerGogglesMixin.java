package dev.actuallycreated.mixin;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.actuallycreated.compat.GogglesCompat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlazeBurnerBlock.class, remap = false)
public class BlazeBurnerGogglesMixin {
    @Redirect(method = "useItemOn", at = @At(value = "INVOKE",
            target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean actuallycreated$acceptEngineerGoggles(ItemEntry<?> goggles, ItemStack stack) {
        return goggles.isIn(stack) || GogglesCompat.isActuallyAdditionsGoggles(stack);
    }
}
