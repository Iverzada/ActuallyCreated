package dev.actuallycreated.registry;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ACArmInteractionPoints {
    private static final DeferredRegister<ArmInteractionPointType> POINTS =
            DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, ActuallyCreated.ID);

    static {
        POINTS.register("display_stand", () -> new TopItemPointType(false));
        POINTS.register("empowerer", () -> new TopItemPointType(true));
        POINTS.register("clockwork_display_stand", () -> new KineticTopItemPointType(false));
        POINTS.register("mechanical_empowering_stand", () -> new KineticTopItemPointType(true));
    }

    private static class KineticTopItemPointType extends ArmInteractionPointType {
        private final boolean center;

        private KineticTopItemPointType(boolean center) { this.center = center; }

        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(center ? ACBlocks.MECHANICAL_EMPOWERING_STAND.get()
                    : ACBlocks.CLOCKWORK_DISPLAY_STAND.get());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new AllArmInteractionPointTypes.TopFaceArmInteractionPoint(this, level, pos, state);
        }
    }

    public static void register(IEventBus modBus) { POINTS.register(modBus); }

    private static class TopItemPointType extends ArmInteractionPointType {
        private final boolean empowerer;

        private TopItemPointType(boolean empowerer) {
            this.empowerer = empowerer;
        }

        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(empowerer ? ActuallyBlocks.EMPOWERER.getBlock()
                    : ActuallyBlocks.DISPLAY_STAND.getBlock());
        }

        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new AllArmInteractionPointTypes.TopFaceArmInteractionPoint(this, level, pos, state);
        }
    }

    private ACArmInteractionPoints() { }
}
