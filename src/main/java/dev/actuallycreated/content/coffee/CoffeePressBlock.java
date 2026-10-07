package dev.actuallycreated.content.coffee;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;

import dev.actuallycreated.registry.ACBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.Level;

public class CoffeePressBlock
        extends KineticBlock
        implements IBE<CoffeePressBlockEntity> {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CoffeePressBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(
            LevelReader level,
            BlockPos pos,
            BlockState state,
            Direction face
    ) {
        return face == Direction.UP;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CoffeePressBlockEntity press) {
            for (int slot = 0; slot < press.getInventory().getSlots(); slot++)
                popResource(level, pos, press.getInventory().getStackInSlot(slot));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<CoffeePressBlockEntity> getBlockEntityClass() {
        return CoffeePressBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoffeePressBlockEntity> getBlockEntityType() {
        return ACBlockEntities.COFFEE_PRESS.get();
    }
}
