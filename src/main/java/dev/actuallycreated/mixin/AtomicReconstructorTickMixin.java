package dev.actuallycreated.mixin;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import dev.actuallycreated.compat.NativeTransportedReconstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityAtomicReconstructor.class, remap = false)
public class AtomicReconstructorTickMixin {
    @Shadow private int currentTime;
    @Unique private long actuallycreated$nextTransportShot;

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void actuallycreated$wakeForTransport(Level level, BlockPos pos, BlockState state,
            BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof TileEntityAtomicReconstructor machine) || level.isClientSide
                || machine.isPulseMode || machine.isRedstonePowered
                || machine.getLens() != ActuallyAdditionsAPI.lensDefaultConversion) return;
        var self = (AtomicReconstructorTickMixin) (Object) machine;
        if (level.getGameTime() < self.actuallycreated$nextTransportShot) return;
        if (NativeTransportedReconstruction.hasTarget(machine)) {
            self.currentTime = 1; // Let AA fire normally, including its FE charge and beam.
            self.actuallycreated$nextTransportShot = level.getGameTime() + 10;
        }
    }
}
