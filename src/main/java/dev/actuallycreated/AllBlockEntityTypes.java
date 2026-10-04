package dev.actuallycreated;

import dev.actuallycreated.content.kinetics.ActuallyCreatedGeneratorBlockEntity;
import dev.actuallycreated.content.kinetics.ActuallyCreatedKineticBlockEntity;
import dev.actuallycreated.content.kinetics.ActuallyCreatedShaftRenderer;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

/**
 * Block entity type registration.
 */
public class AllBlockEntityTypes {

    /**
     * Block entity for EXAMPLE_KINETIC_BLOCK, rendered with ActuallyCreatedShaftRenderer so a
     * shaft visibly spins through the casing.
     */
    public static final BlockEntityEntry<ActuallyCreatedKineticBlockEntity> EXAMPLE_KINETIC = ActuallyCreated.REGISTRATE
            .blockEntity("actuallycreated_kinetic", ActuallyCreatedKineticBlockEntity::new)
            // visual for flywheel renderer
            .visual(() -> ShaftVisual::new)
            .validBlock(AllBlocks.EXAMPLE_KINETIC_BLOCK)
            // fallback renderer if flywheel is not available
            .renderer(() -> ActuallyCreatedShaftRenderer::new)
            .register();

    /**
     * Block entity for EXAMPLE_GENERATOR_BLOCK, also rendered with ActuallyCreatedShaftRenderer.
     */
    public static final BlockEntityEntry<ActuallyCreatedGeneratorBlockEntity> EXAMPLE_GENERATOR = ActuallyCreated.REGISTRATE
            .blockEntity("actuallycreated_generator", ActuallyCreatedGeneratorBlockEntity::new)
            .visual(() -> ShaftVisual::new)
            .validBlock(AllBlocks.EXAMPLE_GENERATOR_BLOCK)
            .renderer(() -> ActuallyCreatedShaftRenderer::new)
            .register();

    public static void register() {
        // Force class loading to trigger Registrate calls
    }
}
