package dev.actuallycreated.mixin;

import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.lens.LensConversion;
import dev.actuallycreated.compat.NativeTransportedReconstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LensConversion.class, remap = false)
public class ConversionLensMixin {
    @Inject(method = "invoke", at = @At("HEAD"), cancellable = true)
    private void actuallycreated$processTransported(BlockState state, BlockPos pos,
            IAtomicReconstructor source, CallbackInfoReturnable<Boolean> cir) {
        if (NativeTransportedReconstruction.process(pos, source)) cir.setReturnValue(true);
    }
}
