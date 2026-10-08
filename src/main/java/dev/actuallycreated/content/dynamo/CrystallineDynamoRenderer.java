package dev.actuallycreated.content.dynamo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.actuallycreated.registry.ACPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class CrystallineDynamoRenderer extends KineticBlockEntityRenderer<CrystallineDynamoBlockEntity> {
    public CrystallineDynamoRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CrystallineDynamoBlockEntity be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        renderRotatingBuffer(be, getRotatedModel(be, be.getBlockState()), poseStack,
                buffer.getBuffer(RenderType.solid()), light);

        Direction facing = be.getBlockState().getValue(CrystallineDynamoBlock.FACING);
        int rotation = switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.translate(-0.5, 0, -0.5);

        double phase = (be.getLevel().getGameTime() + partialTicks) * 0.42;
        float travel = be.isProcessing() ? (float) ((Math.sin(phase) + 1) * 0.5 / 16) : 0;
        CachedBuffers.partial(ACPartialModels.CRYSTALLINE_DYNAMO_LEFT_JAW, be.getBlockState())
                .translate(travel, 0, 0).light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
        CachedBuffers.partial(ACPartialModels.CRYSTALLINE_DYNAMO_RIGHT_JAW, be.getBlockState())
                .translate(-travel, 0, 0).light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));

        if (be.isProcessing()) {
            var fire = be.isProcessingEmpowered()
                    ? ACPartialModels.CRYSTALLINE_DYNAMO_SUPERHEATED_FIRE
                    : ACPartialModels.CRYSTALLINE_DYNAMO_FIRE;
            CachedBuffers.partial(fire, be.getBlockState())
                    .light(0xF000F0)
                    .renderInto(poseStack, buffer.getBuffer(RenderType.cutout()));
        }
        poseStack.popPose();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(CrystallineDynamoBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(ACPartialModels.CRYSTALLINE_DYNAMO_SHAFT, state,
                state.getValue(CrystallineDynamoBlock.FACING).getOpposite());
    }
}
