package dev.actuallycreated.content.empowering;

import com.simibubi.create.foundation.block.IBE;
import dev.actuallycreated.registry.ACBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Block;

public class ClockworkDisplayStandBlock extends AbstractEmpoweringStandBlock implements IBE<ClockworkDisplayStandBlockEntity> {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 13, 16);

    public ClockworkDisplayStandBlock(Properties properties) { super(properties); }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public Class<ClockworkDisplayStandBlockEntity> getBlockEntityClass() { return ClockworkDisplayStandBlockEntity.class; }

    @Override
    public BlockEntityType<? extends ClockworkDisplayStandBlockEntity> getBlockEntityType() {
        return ACBlockEntities.CLOCKWORK_DISPLAY_STAND.get();
    }
}
