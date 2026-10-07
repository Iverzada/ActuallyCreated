package dev.actuallycreated.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.content.coffee.CoffeePressBlock;
import dev.actuallycreated.content.coffee.CoffeePressBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;

public final class ACBlocks {

        public static final BlockEntry<CoffeePressBlock> COFFEE_PRESS =
        ActuallyCreated.REGISTRATE
                .block(
                        "coffee_press",
                        CoffeePressBlock::new
                )
                .initialProperties(
                        () -> Blocks.COPPER_BLOCK
                )
                .properties(
                        properties ->
                                properties.noOcclusion().strength(1.5f, 6.0f).requiresCorrectToolForDrops()
                )
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockstate(
                        (context, provider) ->
                                provider.horizontalBlock(
                                        context.get(),
                                        provider.models()
                                                .getExistingFile(
                                                        ActuallyCreated.asResource(
                                                                "block/coffee_press/coffee_press"
                                                        )
                                                )
                                )
                )
                .onRegister(
                        block ->
                                BlockStressValues.IMPACTS.register(
                                        block,
                                        () ->
                                                CoffeePressBlockEntity.STRESS_IMPACT
                                )
                )
                .item()
                .model(
                        (context, provider) ->
                                provider.withExistingParent(
                                        context.getName(),
                                        ActuallyCreated.asResource(
                                                "block/coffee_press/coffee_press"
                                        )
                                )
                )
                .build()
                .register();

        public static final BlockEntry<KineticAtomicReconstructorBlock> KINETIC_ATOMIC_RECONSTRUCTOR = ActuallyCreated.REGISTRATE
                        .block(
                                        "kinetic_atomic_reconstructor",
                                        KineticAtomicReconstructorBlock::new)

                        .initialProperties(() -> Blocks.POLISHED_ANDESITE)

                        .properties(properties -> properties.noOcclusion().strength(1.5f, 6.0f).requiresCorrectToolForDrops())

                        .tag(BlockTags.MINEABLE_WITH_PICKAXE)

                        .blockstate((context, provider) -> provider.directionalBlock(
                                        context.get(),
                                        provider.models().getExistingFile(
                                                        ActuallyCreated.asResource(
                                                                        "block/kinetic_atomic_reconstructor/kinetic_atomic_reconstructor"))))

                        .onRegister(block -> BlockStressValues.IMPACTS.register(
                                        block,
                                        () -> KineticAtomicReconstructorBlockEntity.STRESS_IMPACT))

                        .item()

                        .model((context, provider) -> provider.withExistingParent(
                                        context.getName(),
                                        ActuallyCreated.asResource(
                                                        "block/kinetic_atomic_reconstructor/kinetic_atomic_reconstructor")))

                        .build()
                        .register();

        private ACBlocks() {
        }

        public static void register() {
        }
}
