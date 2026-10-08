package dev.actuallycreated.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;

import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import dev.actuallycreated.content.coffee.CoffeePressBlock;
import dev.actuallycreated.content.coffee.CoffeePressBlockEntity;
import dev.actuallycreated.content.dynamo.CrystallineDynamoBlock;
import dev.actuallycreated.content.dynamo.CrystallineDynamoBlockEntity;
import dev.actuallycreated.content.empowering.ClockworkDisplayStandBlock;
import dev.actuallycreated.content.empowering.MechanicalEmpoweringStandBlock;
import dev.actuallycreated.content.empowering.EmpoweringKinetics;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;

public final class ACBlocks {

        public static final BlockEntry<MechanicalEmpoweringStandBlock> MECHANICAL_EMPOWERING_STAND = ActuallyCreated.REGISTRATE
                .block("mechanical_empowering_stand", MechanicalEmpoweringStandBlock::new)
                .initialProperties(() -> Blocks.COPPER_BLOCK)
                .properties(properties -> properties.noOcclusion().strength(1.5f, 6.0f).requiresCorrectToolForDrops())
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockstate((context, provider) -> provider.simpleBlock(context.get(),
                        provider.models().getExistingFile(ActuallyCreated.asResource("block/mechanical_empowering_stand/mechanical_empowering_stand"))))
                .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> EmpoweringKinetics.CENTRAL_STRESS_IMPACT))
                .item().model((context, provider) -> provider.withExistingParent(context.getName(),
                        ActuallyCreated.asResource("block/mechanical_empowering_stand/mechanical_empowering_stand")))
                .build().register();

        public static final BlockEntry<ClockworkDisplayStandBlock> CLOCKWORK_DISPLAY_STAND = ActuallyCreated.REGISTRATE
                .block("clockwork_display_stand", ClockworkDisplayStandBlock::new)
                .initialProperties(() -> Blocks.COPPER_BLOCK)
                .properties(properties -> properties.noOcclusion().strength(1.5f, 6.0f).requiresCorrectToolForDrops())
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockstate((context, provider) -> provider.simpleBlock(context.get(),
                        provider.models().getExistingFile(ActuallyCreated.asResource("block/clockwork_display_stand/clockwork_display_stand"))))
                .onRegister(block -> BlockStressValues.IMPACTS.register(block, () -> EmpoweringKinetics.DISPLAY_STRESS_IMPACT))
                .item().model((context, provider) -> provider.withExistingParent(context.getName(),
                        ActuallyCreated.asResource("block/clockwork_display_stand/item")))
                .build().register();

        public static final BlockEntry<CrystallineDynamoBlock> CRYSTALLINE_DYNAMO = ActuallyCreated.REGISTRATE
                .block("crystalline_dynamo", CrystallineDynamoBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.noOcclusion().strength(1.5f, 6.0f).requiresCorrectToolForDrops())
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .blockstate((context, provider) -> provider.horizontalBlock(context.get(),
                        provider.models().getExistingFile(ActuallyCreated.asResource("block/crystalline_dynamo/crystalline_dynamo"))))
                .onRegister(block -> BlockStressValues.IMPACTS.register(block,
                        () -> CrystallineDynamoBlockEntity.STRESS_IMPACT))
                .item()
                .model((context, provider) -> provider.withExistingParent(context.getName(),
                        ActuallyCreated.asResource("block/crystalline_dynamo/crystalline_dynamo")))
                .build()
                .register();

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
