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
 * Example kinetic block. Extends RotatedPillarKineticBlock, which gives it an axis
 * property and shaft connections on both ends of that axis, and implements IBE to
 * bind it to ActuallyCreatedKineticBlockEntity.
 */
public class ActuallyCreatedKineticBlock extends RotatedPillarKineticBlock implements IBE<ActuallyCreatedKineticBlockEntity> {

    public ActuallyCreatedKineticBlock(Properties properties) {
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
    public Class<ActuallyCreatedKineticBlockEntity> getBlockEntityClass() {
        return ActuallyCreatedKineticBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ActuallyCreatedKineticBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.EXAMPLE_KINETIC.get();
    }
}
