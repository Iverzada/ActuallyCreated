package dev.actuallycreated.content.empowering;

import com.simibubi.create.foundation.block.IBE;
import dev.actuallycreated.registry.ACBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MechanicalEmpoweringStandBlock extends AbstractEmpoweringStandBlock implements IBE<MechanicalEmpoweringStandBlockEntity> {
    public MechanicalEmpoweringStandBlock(Properties properties) { super(properties); }

    @Override
    public Class<MechanicalEmpoweringStandBlockEntity> getBlockEntityClass() { return MechanicalEmpoweringStandBlockEntity.class; }

    @Override
    public BlockEntityType<? extends MechanicalEmpoweringStandBlockEntity> getBlockEntityType() {
        return ACBlockEntities.MECHANICAL_EMPOWERING_STAND.get();
    }
}
