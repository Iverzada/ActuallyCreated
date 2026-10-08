package dev.actuallycreated.content.dynamo;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.actuallycreated.registry.ACBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class CrystallineDynamoBlock extends KineticBlock implements IBE<CrystallineDynamoBlockEntity> {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public CrystallineDynamoBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!CrystallineDynamoBlockEntity.isCrystal(stack)
                || !(level.getBlockEntity(pos) instanceof CrystallineDynamoBlockEntity dynamo))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!dynamo.getInventory().insertItem(0, stack.copyWithCount(1), true).isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            ItemStack remainder = dynamo.getInventory().insertItem(0, stack.copyWithCount(1), false);
            if (remainder.isEmpty() && !player.isCreative())
                stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CrystallineDynamoBlockEntity dynamo)
                || dynamo.getInventory().getStackInSlot(0).isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack extracted = dynamo.getInventory().extractItem(0,
                    dynamo.getInventory().getStackInSlot(0).getCount(), false);
            if (!player.getInventory().add(extracted)) player.drop(extracted, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter world, Entity entity) {
        super.updateEntityAfterFallOn(world, entity);
        if (entity.level().isClientSide || !(entity instanceof ItemEntity item) || !entity.isAlive()) return;

        CrystallineDynamoBlockEntity dynamo = getBlockEntity(world, entity.blockPosition());
        if (dynamo == null) dynamo = getBlockEntity(world, entity.blockPosition().below());
        if (dynamo == null) return;

        ItemStack remainder = dynamo.getInventory().insertItem(0, item.getItem(), false);
        if (remainder.isEmpty()) item.discard();
        else if (remainder.getCount() < item.getItem().getCount()) item.setItem(remainder);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CrystallineDynamoBlockEntity dynamo)
            popResource(level, pos, dynamo.getInventory().getStackInSlot(0));
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public Class<CrystallineDynamoBlockEntity> getBlockEntityClass() {
        return CrystallineDynamoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CrystallineDynamoBlockEntity> getBlockEntityType() {
        return ACBlockEntities.CRYSTALLINE_DYNAMO.get();
    }
}
