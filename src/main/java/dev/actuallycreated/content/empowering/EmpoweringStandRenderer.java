package dev.actuallycreated.content.empowering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.AllPartialModels;
import dev.actuallycreated.registry.ACPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class EmpoweringStandRenderer<T extends AbstractEmpoweringStandBlockEntity> extends KineticBlockEntityRenderer<T> {
    private final ItemRenderer itemRenderer;

    public EmpoweringStandRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        renderRotatingBuffer(be, getRotatedModel(be, be.getBlockState()), poseStack,
                buffer.getBuffer(RenderType.solid()), light);
        if (be.getDisplayedItem().isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(.5, be instanceof ClockworkDisplayStandBlockEntity ? 1.05 : .78, .5);
        poseStack.mulPose(Axis.YP.rotationDegrees((be.getLevel().getGameTime() + partialTicks) * 2));
        poseStack.scale(.45f, .45f, .45f);
        itemRenderer.renderStatic(be.getDisplayedItem(), ItemDisplayContext.FIXED, light, overlay,
                poseStack, buffer, be.getLevel(), (int) be.getBlockPos().asLong());
        poseStack.popPose();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
        return CachedBuffers.partial(be instanceof ClockworkDisplayStandBlockEntity
                ? AllPartialModels.ARM_COG : ACPartialModels.EMPOWERING_STAND_GEAR, state);
    }
}
