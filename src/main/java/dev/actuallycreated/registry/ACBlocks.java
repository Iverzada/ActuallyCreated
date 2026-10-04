package dev.actuallycreated.registry;

import com.simibubi.create.api.stress.BlockStressValues;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.actuallycreated.ActuallyCreated;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlock;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public final class ACBlocks {
    public static final BlockEntry<KineticAtomicReconstructorBlock> KINETIC_ATOMIC_RECONSTRUCTOR =
            ActuallyCreated.REGISTRATE.block("kinetic_atomic_reconstructor", KineticAtomicReconstructorBlock::new)
                    .initialProperties(() -> Blocks.POLISHED_ANDESITE)
                    .properties(p -> p.noOcclusion())
                    .blockstate((context, provider) -> provider.horizontalBlock(context.get(),
                            provider.models().withExistingParent(context.getName(),
                                            ResourceLocation.withDefaultNamespace("block/orientable"))
                                    .texture("side", ResourceLocation.withDefaultNamespace("block/polished_andesite"))
                                    .texture("top", ResourceLocation.withDefaultNamespace("block/polished_andesite"))
                                    .texture("front", ResourceLocation.withDefaultNamespace("block/redstone_lamp"))))
                    .onRegister(block -> BlockStressValues.IMPACTS.register(block,
                            () -> KineticAtomicReconstructorBlockEntity.STRESS_IMPACT))
                    .item().build().register();

    private ACBlocks() {}
    public static void register() {}
}
