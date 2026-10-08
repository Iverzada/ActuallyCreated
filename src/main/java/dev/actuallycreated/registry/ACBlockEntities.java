package dev.actuallycreated.registry;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorRenderer;
import dev.actuallycreated.content.coffee.CoffeePressBlockEntity;
import dev.actuallycreated.content.coffee.CoffeePressRenderer;
import dev.actuallycreated.content.coffee.CoffeePressVisual;
import dev.actuallycreated.content.dynamo.CrystallineDynamoBlockEntity;
import dev.actuallycreated.content.dynamo.CrystallineDynamoRenderer;
import dev.actuallycreated.content.empowering.ClockworkDisplayStandBlockEntity;
import dev.actuallycreated.content.empowering.MechanicalEmpoweringStandBlockEntity;
import dev.actuallycreated.content.empowering.EmpoweringStandRenderer;

public final class ACBlockEntities {

    public static final BlockEntityEntry<MechanicalEmpoweringStandBlockEntity> MECHANICAL_EMPOWERING_STAND =
            ActuallyCreated.REGISTRATE.blockEntity("mechanical_empowering_stand", MechanicalEmpoweringStandBlockEntity::new)
                    .validBlock(ACBlocks.MECHANICAL_EMPOWERING_STAND)
                    .renderer(() -> EmpoweringStandRenderer::new)
                    .register();

    public static final BlockEntityEntry<ClockworkDisplayStandBlockEntity> CLOCKWORK_DISPLAY_STAND =
            ActuallyCreated.REGISTRATE.blockEntity("clockwork_display_stand", ClockworkDisplayStandBlockEntity::new)
                    .validBlock(ACBlocks.CLOCKWORK_DISPLAY_STAND)
                    .renderer(() -> EmpoweringStandRenderer::new)
                    .register();

    public static final BlockEntityEntry<CrystallineDynamoBlockEntity> CRYSTALLINE_DYNAMO =
            ActuallyCreated.REGISTRATE
                    .blockEntity("crystalline_dynamo", CrystallineDynamoBlockEntity::new)
                    .validBlock(ACBlocks.CRYSTALLINE_DYNAMO)
                    .renderer(() -> CrystallineDynamoRenderer::new)
                    .register();

    public static final BlockEntityEntry<KineticAtomicReconstructorBlockEntity>
            KINETIC_ATOMIC_RECONSTRUCTOR =
            ActuallyCreated.REGISTRATE
                    .blockEntity(
                            "kinetic_atomic_reconstructor",
                            KineticAtomicReconstructorBlockEntity::new
                    )

                    // Flywheel
                    .visual(
                            () -> OrientedRotatingVisual.of(
                                    ACPartialModels.KINETIC_RECONSTRUCTOR_SHAFT
                            ),
                            false
                    )

                    .validBlock(
                            ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR
                    )

                    .renderer(
                            () -> KineticAtomicReconstructorRenderer::new
                    )

                    .register();

    public static final BlockEntityEntry<CoffeePressBlockEntity> COFFEE_PRESS =
        ActuallyCreated.REGISTRATE
                .blockEntity(
                        "coffee_press",
                        CoffeePressBlockEntity::new
                )
                .visual(
                        () -> CoffeePressVisual::new
                )
                .validBlock(
                        ACBlocks.COFFEE_PRESS
                )
                .renderer(
                        () -> CoffeePressRenderer::new
                )
                .register();

    private ACBlockEntities() {
    }

    public static void register() {
    }
}
