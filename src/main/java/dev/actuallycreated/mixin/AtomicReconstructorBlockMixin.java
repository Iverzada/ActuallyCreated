package dev.actuallycreated.mixin;

import de.ellpeck.actuallyadditions.mod.blocks.BlockAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import dev.actuallycreated.compat.CreatedModeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockAtomicReconstructor.class, remap = false)
public class AtomicReconstructorBlockMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void actuallycreated$cycleMode(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND
                || player.getMainHandItem().getItem() != CommonConfig.Other.redstoneConfigureItem) return;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileEntityAtomicReconstructor machine) {
            var mode = (CreatedModeAccess) machine;
            String label;
            if (mode.actuallycreated$isCreatedMode()) {
                mode.actuallycreated$setCreatedMode(false);
                machine.isPulseMode = false;
                label = "automatic";
            } else if (machine.isPulseMode) {
                machine.isPulseMode = false;
                mode.actuallycreated$setCreatedMode(true);
                label = "created";
            } else {
                machine.isPulseMode = true;
                label = "pulse";
            }
            machine.setChanged();
            machine.sendUpdate();
            player.displayClientMessage(Component.translatable(
                    "message.actuallycreated.reconstructor.mode." + label), true);
        }
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}
