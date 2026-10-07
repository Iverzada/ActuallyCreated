package dev.actuallycreated.content.coffee;

import com.mojang.blaze3d.vertex.PoseStack;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;

import dev.actuallycreated.registry.ACPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;

import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import com.mojang.math.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

public class CoffeePressRenderer
        extends KineticBlockEntityRenderer<CoffeePressBlockEntity> {

    private static final float MAX_PRESS_TRAVEL =
            4.0f / 16.0f;
    private static final float ONE_PIXEL = 1.0f / 16.0f;
    private static final float CONTENT_SCALE = 0.25f;

    private final ItemRenderer itemRenderer;

    public CoffeePressRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    protected void renderSafe(
            CoffeePressBlockEntity be,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {

        super.renderSafe(
                be,
                partialTicks,
                poseStack,
                buffer,
                light,
                overlay
        );

        renderContents(be, poseStack, buffer, light, overlay);
        renderPipeConnection(be, poseStack, buffer, light);
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, poseStack, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }

        BlockState state =
                be.getBlockState();

        float progress =
                be.getPressProgress(partialTicks);

        float yOffset =
                progress * MAX_PRESS_TRAVEL;

        SuperByteBuffer plunger =
                CachedBuffers.partial(
                        ACPartialModels.COFFEE_PRESS_PLUNGER,
                        state
                );

        plunger
                .translate(
                        0,
                        ONE_PIXEL - yOffset,
                        0
                )
                .light(light)
                .renderInto(
                        poseStack,
                        buffer.getBuffer(RenderType.solid())
                );
    }

    private void renderPipeConnection(
            CoffeePressBlockEntity be,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        if (be.getLevel() == null)
            return;

        BlockState below = be.getLevel().getBlockState(be.getBlockPos().below());
        if (!(below.getBlock() instanceof FluidPipeBlock) && !(below.getBlock() instanceof PumpBlock))
            return;

        if (be.getLevel().getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                be.getBlockPos().below(),
                Direction.UP
        ) == null)
            return;

        CachedBuffers.partial(ACPartialModels.COFFEE_PRESS_PIPE_CONNECTION, be.getBlockState())
                .light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
    }

    private void renderContents(
            CoffeePressBlockEntity be,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        renderCoffee(be, poseStack, buffer, light);
        renderItem(be, be.getInventory().getStackInSlot(0), 5.0f / 16.0f,
                poseStack, buffer, light, overlay);
        renderItem(be, be.getInventory().getStackInSlot(1), 11.0f / 16.0f,
                poseStack, buffer, light, overlay);
    }

    private void renderCoffee(
            CoffeePressBlockEntity be,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        FluidStack fluid = be.getTank().getFluid();
        if (fluid.isEmpty())
            return;

        float fill = fluid.getAmount() / (float) CoffeePressBlockEntity.TANK_CAPACITY;
        float minY = 4.05f / 16.0f;
        float maxY = minY + fill * (7.2f / 16.0f);

        NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
                fluid,
                3.1f / 16.0f, minY, 2.3f / 16.0f,
                12.9f / 16.0f, maxY, 13.7f / 16.0f,
                buffer, poseStack, light, false, true
        );
    }

    private void renderItem(
            CoffeePressBlockEntity be,
            ItemStack stack,
            float x,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay
    ) {
        if (stack.isEmpty())
            return;

        poseStack.pushPose();
        poseStack.translate(x, 4.45f / 16.0f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(CONTENT_SCALE, CONTENT_SCALE, CONTENT_SCALE);
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                light,
                overlay,
                poseStack,
                buffer,
                be.getLevel(),
                (int) be.getBlockPos().asLong()
        );
        poseStack.popPose();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(
            CoffeePressBlockEntity be,
            BlockState state
    ) {
        return CachedBuffers.partialFacing(
                ACPartialModels.COFFEE_PRESS_SHAFT,
                state,
                Direction.UP
        ).translate(0, -ONE_PIXEL, 0);
    }
}
