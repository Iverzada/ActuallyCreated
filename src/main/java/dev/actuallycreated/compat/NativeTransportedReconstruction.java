package dev.actuallycreated.compat;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import dev.actuallycreated.recipe.reconstructing.NativeReconstructionAdapter;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Runs inside the native conversion lens, after AA has paid the firing cost. */
public final class NativeTransportedReconstruction {
    private NativeTransportedReconstruction() {}

    public static boolean process(BlockPos beamPos, IAtomicReconstructor source) {
        var level = source.getWorldObject();
        if (level == null || level.isClientSide || beamPos == null) return false;
        // A horizontal beam can reach items resting on the transport block below it.
        int offsets = source.getOrientation().getAxis().isHorizontal() ? 1 : 0;
        for (int offset = 0; offset <= offsets; offset++) {
            BlockPos pos = beamPos.below(offset);
            if (!level.isLoaded(pos)) continue;
            var handler = BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
            if (handler == null) continue;
            boolean[] processed = {false};
            handler.handleProcessingOnAllItems(item -> {
                if (processed[0] || item.locked || item.lockedExternally || item.stack.isEmpty())
                    return TransportedResult.doNothing();
                List<ItemStack> results = new ArrayList<>();
                int count;
                int energy;
                var assembly = SequencedAssemblyRecipe.getRecipe(level, item.stack,
                        ACRecipeTypes.RECONSTRUCTING.getType(), ReconstructingRecipe.class);
                if (assembly.isPresent()) {
                    energy = 1000;
                    if (source.getEnergy() < energy) return TransportedResult.doNothing();
                    count = 1;
                    results.addAll(assembly.get().value().rollResults(level.random));
                } else {
                    if (item.stack.has(AllDataComponents.SEQUENCED_ASSEMBLY)) return TransportedResult.doNothing();
                    var recipe = NativeReconstructionAdapter.find(level, item.stack);
                    if (recipe.isEmpty()) return TransportedResult.doNothing();
                    int cost = Math.max(0, recipe.get().value().getEnergy());
                    count = cost == 0 ? item.stack.getCount() : Math.min(item.stack.getCount(), source.getEnergy() / cost);
                    if (count <= 0) return TransportedResult.doNothing();
                    energy = count * cost;
                    for (int i = 0; i < count; i++)
                        results.add(recipe.get().value().getResultItem(level.registryAccess()).copy());
                }
                List<TransportedItemStack> outputs = new ArrayList<>();
                if (count < item.stack.getCount()) {
                    var remainder = item.copy();
                    remainder.stack.shrink(count);
                    outputs.add(remainder);
                }
                for (var result : results) {
                    if (result.isEmpty()) continue;
                    var output = item.copy();
                    output.stack = result;
                    output.clearFanProcessingData();
                    outputs.add(output);
                }
                source.extractEnergy(energy);
                processed[0] = true;
                return TransportedResult.convertTo(outputs);
            });
            if (processed[0]) return true;
        }
        return false;
    }
}
