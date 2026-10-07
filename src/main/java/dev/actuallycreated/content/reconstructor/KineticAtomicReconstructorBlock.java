package dev.actuallycreated.content.reconstructor;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import dev.actuallycreated.registry.ACBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.level.LevelReader;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.state.BlockState;
import de.ellpeck.actuallyadditions.api.lens.ILensItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof ILensItem)
                || !(level.getBlockEntity(pos) instanceof KineticAtomicReconstructorBlockEntity machine))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!machine.getLensInventory().getStackInSlot(0).isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            ItemStack lens = stack.copyWithCount(1);
            machine.getLensInventory().setStackInSlot(0, lens);
            if (!player.isCreative()) stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof KineticAtomicReconstructorBlockEntity machine)
                || machine.getLensInventory().getStackInSlot(0).isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack lens = machine.getLensInventory().extractItem(0, 1, false);
            if (!player.getInventory().add(lens)) player.drop(lens, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof KineticAtomicReconstructorBlockEntity machine)
            popResource(level, pos, machine.getLensInventory().getStackInSlot(0));
        super.onRemove(state, level, pos, newState, movedByPiston);
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
