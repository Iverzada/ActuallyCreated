package dev.actuallycreated.content.reconstructor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import dev.actuallycreated.registry.ACBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.level.LevelReader;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.state.BlockState;

public class KineticAtomicReconstructorBlock
        extends DirectionalKineticBlock
        implements IBE<KineticAtomicReconstructorBlockEntity> {

    public KineticAtomicReconstructorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos pos,
            BlockState state,
            Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Class<KineticAtomicReconstructorBlockEntity> getBlockEntityClass() {
        return KineticAtomicReconstructorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticAtomicReconstructorBlockEntity> getBlockEntityType() {
        return ACBlockEntities.KINETIC_ATOMIC_RECONSTRUCTOR.get();
    }
}