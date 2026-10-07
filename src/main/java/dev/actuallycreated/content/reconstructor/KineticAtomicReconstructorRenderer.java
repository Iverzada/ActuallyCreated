package dev.actuallycreated.content.reconstructor;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.actuallycreated.registry.ACPartialModels;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class KineticAtomicReconstructorRenderer
        extends KineticBlockEntityRenderer<KineticAtomicReconstructorBlockEntity> {

    public KineticAtomicReconstructorRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(
            KineticAtomicReconstructorBlockEntity be,
            BlockState state
    ) {
        return CachedBuffers.partialFacing(
                ACPartialModels.KINETIC_RECONSTRUCTOR_SHAFT,
                state
        );
    }
}