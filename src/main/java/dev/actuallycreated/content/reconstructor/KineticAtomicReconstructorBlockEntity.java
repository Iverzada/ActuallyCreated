package dev.actuallycreated.content.reconstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import dev.actuallycreated.recipe.reconstructing.ReconstructingRecipe;
import dev.actuallycreated.recipe.reconstructing.NativeReconstructionAdapter;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class KineticAtomicReconstructorBlockEntity extends KineticBlockEntity implements IAirCurrentSource {
    /** Stress impact in SU per RPM, matching the addon's Create stress registration. */
    public static final float STRESS_IMPACT = 16.0f;
    public static final int MAX_OPERATIONS_PER_SHOT = 4;
    private static final int COOLDOWN_SCALE = 2560;
    private static final NativeReconstructionAdapter NATIVE_RECIPES = NativeReconstructionAdapter.NONE;
    private int cooldown;

    public KineticAtomicReconstructorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static int cooldownForRpm(float rpm) {
        double speed = Math.abs((double) rpm);
        if (!Double.isFinite(speed) || speed <= 0) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, Math.ceil(COOLDOWN_SCALE / speed)));
    }

    @Override public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        if (cooldown > 0) cooldown--;
        if (getSpeed() == 0 || cooldown > 0) return;

        Direction front = getBlockState().getValue(KineticAtomicReconstructorBlock.HORIZONTAL_FACING);
        float reach = AirCurrent.getFlowLimit(level, worldPosition, getMaxDistance(), front);
        for (int distance = 1; distance <= Math.ceil(reach); distance++) {
            BlockPos target = worldPosition.relative(front, distance);
            if (!level.isLoaded(target)) break;
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(target),
                    item -> item.isAlive() && !item.getItem().isEmpty());
            items.sort(Comparator.comparingDouble(item -> item.position().distanceToSqr(worldPosition.getCenter())));
            for (ItemEntity item : items) {
                if (item.position().distanceTo(worldPosition.getCenter()) > reach + 0.5) continue;
                if (process(item)) {
                    cooldown = cooldownForRpm(getSpeed());
                    setChanged();
                    return;
                }
            }
        }
    }

    private boolean process(ItemEntity entity) {
        ItemStack input = entity.getItem();
        Optional<RecipeHolder<ReconstructingRecipe>> assembly =
                SequencedAssemblyRecipe.getRecipe(level, input, ACRecipeTypes.RECONSTRUCTING.getType(), ReconstructingRecipe.class);
        // A transitional stack must only be changed by its currently required assembly step.
        if (assembly.isEmpty() && input.has(AllDataComponents.SEQUENCED_ASSEMBLY)) return false;
        Optional<RecipeHolder<ReconstructingRecipe>> recipe = assembly.isPresent() ? assembly :
                level.getRecipeManager().getRecipeFor(ACRecipeTypes.RECONSTRUCTING.getType(), new SingleRecipeInput(input), level);
        if (recipe.isEmpty()) {
            return NATIVE_RECIPES.tryProcess(level, entity, MAX_OPERATIONS_PER_SHOT);
        }

        int operations = assembly.isPresent() ? 1 : Math.min(MAX_OPERATIONS_PER_SHOT, input.getCount());
        for (int i = 0; i < operations; i++) {
            List<ItemStack> outputs = recipe.get().value().rollResults(level.random);
            input.shrink(1);
            for (ItemStack output : outputs) {
                ItemEntity result = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), output);
                result.setDeltaMovement(entity.getDeltaMovement());
                level.addFreshEntity(result);
            }
        }
        if (input.isEmpty()) entity.discard();
        else entity.setItem(input);
        return true;
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("ReconstructingCooldown", cooldown);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        cooldown = Math.max(0, tag.getInt("ReconstructingCooldown"));
    }

    @Override @Nullable public AirCurrent getAirCurrent() { return null; }
    @Override @Nullable public Level getAirCurrentWorld() { return level; }
    @Override public BlockPos getAirCurrentPos() { return worldPosition; }
    @Override public Direction getAirflowOriginSide() {
        return getBlockState().getValue(KineticAtomicReconstructorBlock.HORIZONTAL_FACING);
    }
    @Override public Direction getAirFlowDirection() { return getAirflowOriginSide(); }
    @Override public boolean isSourceRemoved() { return isRemoved(); }
}
