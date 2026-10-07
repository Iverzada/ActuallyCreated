package dev.actuallycreated.registry;

import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorRenderer;
import dev.actuallycreated.content.coffee.CoffeePressBlockEntity;
import dev.actuallycreated.content.coffee.CoffeePressRenderer;
import dev.actuallycreated.content.coffee.CoffeePressVisual;

public final class ACBlockEntities {

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
