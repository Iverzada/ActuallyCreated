package dev.actuallycreated.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphics;

public class ReconstructingAssemblyStep
        extends SequencedAssemblySubCategory {

    private static final int LASER_BLUE = 0x1B6DFF;

    private final AnimatedKineticReconstructor reconstructor;

    public ReconstructingAssemblyStep() {
        super(25);
        reconstructor = new AnimatedKineticReconstructor();
    }

    @Override
    public void draw(
            SequencedRecipe<?> recipe,
            GuiGraphics graphics,
            double mouseX,
            double mouseY,
            int index
    ) {
        drawLaser(graphics, index);

        PoseStack pose = graphics.pose();

        reconstructor.offset = index;

        pose.pushPose();
        pose.translate(-7, 50, 0);
        pose.scale(0.75f, 0.75f, 0.75f);

        reconstructor.draw(
                graphics,
                getWidth() / 2,
                0
        );

        pose.popPose();
    }

    private void drawLaser(
            GuiGraphics graphics,
            int index
    ) {
        float cycle =
                (AnimationTickHolder.getRenderTime()
                        - index * 8)
                        % 25.0f;

        if (cycle < 0) {
            cycle += 25.0f;
        }

        float progress = cycle / 25.0f;

        float pulse =
                1.0f
                        - Math.abs(progress * 2.0f - 1.0f);

        float rotation =
                (AnimationTickHolder.getRenderTime() * 12.0f
                        + index * 45.0f);

        double radians = Math.toRadians(rotation);

        int orbitX =
                (int) Math.round(
                        Math.sin(radians) * 2.0
                );

        float orbitBrightness =
                (float) (
                        0.5
                                + 0.5 * Math.cos(radians)
                );

        int centerX = getWidth() / 2;

        int startY = 43;
        int endY = 79;

        int bodyEnd = endY - 6;

        int outerAlpha =
                (int) (70 + pulse * 55);

        int middleAlpha =
                (int) (125 + pulse * 65);

        int coreAlpha =
                (int) (205 + pulse * 50);

        int outer =
                (outerAlpha << 24)
                        | LASER_BLUE;

        int middle =
                (middleAlpha << 24)
                        | 0x55A0FF;

        int core =
                (coreAlpha << 24)
                        | 0xD8F4FF;

        graphics.fill(
                centerX - 2,
                startY,
                centerX + 3,
                bodyEnd,
                outer
        );

        graphics.fill(
                centerX - 1,
                startY,
                centerX + 2,
                bodyEnd,
                middle
        );

        graphics.fill(
                centerX,
                startY,
                centerX + 1,
                bodyEnd,
                core
        );

        int rotatingAlpha =
                (int) (
                        (100 + pulse * 100)
                                * (0.55f + orbitBrightness * 0.45f)
                );

        int rotatingFront =
                (rotatingAlpha << 24)
                        | 0xA8DDFF;

        int rotatingBack =
                ((int) (rotatingAlpha * 0.65f) << 24)
                        | 0x4B91FF;

        graphics.fill(
                centerX + orbitX,
                startY,
                centerX + orbitX + 1,
                bodyEnd,
                rotatingFront
        );

        graphics.fill(
                centerX - orbitX,
                startY,
                centerX - orbitX + 1,
                bodyEnd,
                rotatingBack
        );

        graphics.fill(
                centerX - 2,
                bodyEnd,
                centerX + 3,
                bodyEnd + 2,
                ((int) (outerAlpha * 0.55f) << 24) | LASER_BLUE
        );

        graphics.fill(
                centerX - 1,
                bodyEnd,
                centerX + 2,
                bodyEnd + 2,
                ((int) (middleAlpha * 0.60f) << 24) | 0x55A0FF
        );

        graphics.fill(
                centerX,
                bodyEnd,
                centerX + 1,
                bodyEnd + 2,
                ((int) (coreAlpha * 0.65f) << 24) | 0xD8F4FF
        );

        graphics.fill(
                centerX - 1,
                bodyEnd + 2,
                centerX + 2,
                bodyEnd + 4,
                ((int) (outerAlpha * 0.35f) << 24) | LASER_BLUE
        );

        graphics.fill(
                centerX - 1,
                bodyEnd + 2,
                centerX + 2,
                bodyEnd + 4,
                ((int) (middleAlpha * 0.40f) << 24) | 0x55A0FF
        );

        graphics.fill(
                centerX,
                bodyEnd + 2,
                centerX + 1,
                bodyEnd + 4,
                ((int) (coreAlpha * 0.45f) << 24) | 0xD8F4FF
        );

        graphics.fill(
                centerX - 1,
                bodyEnd + 4,
                centerX + 2,
                bodyEnd + 5,
                ((int) (outerAlpha * 0.20f) << 24) | LASER_BLUE
        );

        graphics.fill(
                centerX,
                bodyEnd + 4,
                centerX + 1,
                bodyEnd + 5,
                ((int) (middleAlpha * 0.25f) << 24) | 0x55A0FF
        );

        graphics.fill(
                centerX,
                bodyEnd + 5,
                centerX + 1,
                endY,
                ((int) (coreAlpha * 0.15f) << 24) | 0xD8F4FF
        );
    }
}