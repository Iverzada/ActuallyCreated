package dev.actuallycreated.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;

public final class ACBlockEntities {
    public static final BlockEntityEntry<KineticAtomicReconstructorBlockEntity> KINETIC_ATOMIC_RECONSTRUCTOR =
            ActuallyCreated.REGISTRATE.blockEntity("kinetic_atomic_reconstructor",
                            KineticAtomicReconstructorBlockEntity::new)
                    .validBlock(ACBlocks.KINETIC_ATOMIC_RECONSTRUCTOR)
                    .register();

    private ACBlockEntities() {}
    public static void register() {}
}
