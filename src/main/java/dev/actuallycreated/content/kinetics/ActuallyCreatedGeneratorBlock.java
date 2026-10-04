package dev.actuallycreated.content.kinetics;

import dev.actuallycreated.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Example kinetic generator. It uses the same axle-style base as ActuallyCreatedKineticBlock;
 * the difference is entirely in the block entity, ActuallyCreatedGeneratorBlockEntity.
 */
public class ActuallyCreatedGeneratorBlock extends RotatedPillarKineticBlock implements IBE<ActuallyCreatedGeneratorBlockEntity> {

    public ActuallyCreatedGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<ActuallyCreatedGeneratorBlockEntity> getBlockEntityClass() {
        return ActuallyCreatedGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ActuallyCreatedGeneratorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.EXAMPLE_GENERATOR.get();
    }
}
