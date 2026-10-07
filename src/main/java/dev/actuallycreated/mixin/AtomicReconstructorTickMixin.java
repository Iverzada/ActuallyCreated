package dev.actuallycreated.mixin;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.internal.IMethodHandler;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityBase;
import dev.actuallycreated.compat.CreatedModeAccess;
import dev.actuallycreated.compat.NativeTransportedReconstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityAtomicReconstructor.class, remap = false)
public class AtomicReconstructorTickMixin implements CreatedModeAccess {
    @Unique private boolean actuallycreated$createdMode;
    @Unique private long actuallycreated$nextTransportShot;

    @Override
    public boolean actuallycreated$isCreatedMode() {
        return actuallycreated$createdMode;
    }

    @Override
    public void actuallycreated$setCreatedMode(boolean enabled) {
        actuallycreated$createdMode = enabled;
        actuallycreated$nextTransportShot = 0;
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void actuallycreated$fireForTransport(Level level, BlockPos pos, BlockState state,
            BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof TileEntityAtomicReconstructor machine) || level.isClientSide) return;
        if (!((CreatedModeAccess) machine).actuallycreated$isCreatedMode()
                || machine.getLens() != ActuallyAdditionsAPI.lensDefaultConversion) return;
        var self = (AtomicReconstructorTickMixin) (Object) machine;
        if (level.getGameTime() < self.actuallycreated$nextTransportShot) return;
        if (NativeTransportedReconstruction.hasTarget(machine)) {
            ActuallyAdditionsAPI.methodHandler.invokeReconstructor(machine);
            self.actuallycreated$nextTransportShot = level.getGameTime() + 1;
        }
    }

    @Redirect(method = "serverTick", at = @At(value = "INVOKE",
            target = "Lde/ellpeck/actuallyadditions/api/internal/IMethodHandler;invokeReconstructor(Lde/ellpeck/actuallyadditions/api/internal/IAtomicReconstructor;)Z"))
    private static boolean actuallycreated$skipAutomaticShot(IMethodHandler handler, IAtomicReconstructor machine) {
        if (((CreatedModeAccess) machine).actuallycreated$isCreatedMode()) return false;
        return handler.invokeReconstructor(machine);
    }

    @Inject(method = "activateOnPulse", at = @At("HEAD"), cancellable = true)
    private void actuallycreated$skipPulse(CallbackInfo ci) {
        if (actuallycreated$createdMode) ci.cancel();
    }

    @Inject(method = "writeSyncableNBT", at = @At("TAIL"))
    private void actuallycreated$writeMode(CompoundTag tag, HolderLookup.Provider registries,
            TileEntityBase.NBTType type, CallbackInfo ci) {
        tag.putBoolean("ActuallyCreatedMode", actuallycreated$createdMode);
    }

    @Inject(method = "readSyncableNBT", at = @At("TAIL"))
    private void actuallycreated$readMode(CompoundTag tag, HolderLookup.Provider registries,
            TileEntityBase.NBTType type, CallbackInfo ci) {
        if (tag.contains("ActuallyCreatedMode"))
            actuallycreated$createdMode = tag.getBoolean("ActuallyCreatedMode");
    }
}
