package dev.actuallycreated.content.empowering;

import dev.actuallycreated.registry.ACBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ClockworkDisplayStandBlockEntity extends AbstractEmpoweringStandBlockEntity {
    public ClockworkDisplayStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ACBlockEntities.CLOCKWORK_DISPLAY_STAND.get(),
                (be, side) -> be.inventory);
    }
}
