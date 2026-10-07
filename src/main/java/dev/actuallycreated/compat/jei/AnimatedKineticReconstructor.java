package dev.actuallycreated.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;

import dev.actuallycreated.registry.ACBlocks;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AnimatedKineticReconstructor extends AnimatedKinetics {

    @Override
    public void draw(
            GuiGraphics graphics,
            int xOffset,
            int yOffset
    ) {
        PoseStack pose = graphics.pose();

        pose.pushPose();

        pose.translate(xOffset, yOffset, 100);
        pose.mulPose(Axis.XP.rotationDegrees(-15.5f));
        pose.mulPose(Axis.YP.rotationDegrees(22.5f));

        int scale = 20;

        BlockState reconstructorState =
                ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR
                        .getDefaultState()
                        .setValue(
                                DirectionalKineticBlock.FACING,
                                Direction.DOWN
                        );

        blockElement(
                shaft(Direction.Axis.Y)
        )
                .rotateBlock(
                        0,
                        getCurrentAngle(),
                        0
                )
                .scale(scale)
                .render(graphics);

        blockElement(reconstructorState)
                .scale(scale)
                .render(graphics);

        pose.popPose();
    }
}