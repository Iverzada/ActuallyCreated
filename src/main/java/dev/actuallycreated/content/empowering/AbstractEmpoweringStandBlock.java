package dev.actuallycreated.content.empowering;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractEmpoweringStandBlock extends KineticBlock implements ICogWheel {
    protected AbstractEmpoweringStandBlock(Properties properties) { super(properties); }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AbstractEmpoweringStandBlockEntity stand))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!stand.getDisplayedItem().isEmpty() && (stack.isEmpty() || player.isShiftKeyDown())) {
            if (!level.isClientSide) {
                ItemStack extracted = stand.getInventory().extractItem(0, stand.getDisplayedItem().getCount(), false);
                if (stack.isEmpty()) player.setItemInHand(hand, extracted);
                else if (!player.getInventory().add(extracted)) player.drop(extracted, false);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (stand instanceof MechanicalEmpoweringStandBlockEntity && !stand.getDisplayedItem().isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!stand.getInventory().insertItem(0, stack.copyWithCount(1), true).isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            if (stand.getInventory().insertItem(0, stack.copyWithCount(1), false).isEmpty() && !player.isCreative())
                stack.shrink(1);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AbstractEmpoweringStandBlockEntity stand)
                || stand.getDisplayedItem().isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack extracted = stand.getInventory().extractItem(0, stand.getDisplayedItem().getCount(), false);
            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                player.setItemInHand(InteractionHand.MAIN_HAND, extracted);
            else if (!player.getInventory().add(extracted))
                player.drop(extracted, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AbstractEmpoweringStandBlockEntity stand)
            popResource(level, pos, stand.getDisplayedItem());
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
