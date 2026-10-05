package dev.actuallycreated.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;

import net.minecraft.world.level.block.Blocks;

public final class ACBlocks {

        public static final BlockEntry<KineticAtomicReconstructorBlock> KINETIC_ATOMIC_RECONSTRUCTOR = ActuallyCreated.REGISTRATE
                        .block(
                                        "kinetic_atomic_reconstructor",
                                        KineticAtomicReconstructorBlock::new)

                        .initialProperties(() -> Blocks.POLISHED_ANDESITE)

                        .properties(properties -> properties.noOcclusion())

                        .blockstate((context, provider) -> provider.directionalBlock(
                                        context.get(),
                                        provider.models().getExistingFile(
                                                        ActuallyCreated.asResource(
                                                                        "block/kinetic_atomic_reconstructor"))))

                        .onRegister(block -> BlockStressValues.IMPACTS.register(
                                        block,
                                        () -> KineticAtomicReconstructorBlockEntity.STRESS_IMPACT))

                        .item()

                        .model((context, provider) -> provider.withExistingParent(
                                        context.getName(),
                                        ActuallyCreated.asResource(
                                                        "block/kinetic_atomic_reconstructor")))

                        .build()
                        .register();

        private ACBlocks() {
        }

        public static void register() {
        }
}