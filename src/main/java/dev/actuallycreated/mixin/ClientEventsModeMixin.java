package dev.actuallycreated.mixin;

import de.ellpeck.actuallyadditions.mod.event.ClientEvents;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import dev.actuallycreated.compat.CreatedModeAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ClientEvents.class, remap = false)
public class ClientEventsModeMixin {
    // First drawString(Component) in this overlay renders the redstone mode.
    @ModifyArg(method = "onGameOverlay", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I",
            ordinal = 0), index = 1)
    private Component actuallycreated$showCreatedMode(Component original) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hit)) return original;
        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof TileEntityAtomicReconstructor machine)
                || !((CreatedModeAccess) machine).actuallycreated$isCreatedMode()) return original;
        return Component.translatable("info.actuallyadditions.redstoneMode").append(": ")
                .append(Component.translatable("info.actuallycreated.redstoneMode.created")
                        .withStyle(ChatFormatting.DARK_RED));
    }
}
