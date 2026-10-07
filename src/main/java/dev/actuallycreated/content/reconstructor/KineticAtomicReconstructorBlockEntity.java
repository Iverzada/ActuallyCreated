package dev.actuallycreated.content.reconstructor;

import java.util.ArrayList;
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
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperServer;
import de.ellpeck.actuallyadditions.mod.AASounds;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.lens.ILensItem;
import de.ellpeck.actuallyadditions.api.lens.Lens;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class KineticAtomicReconstructorBlockEntity extends KineticBlockEntity implements IAirCurrentSource, IAtomicReconstructor {
    /**
     * Stress impact in SU per RPM, matching the addon's Create stress registration.
     */
    public static final float STRESS_IMPACT = 16.0f;
    public static final int MAX_OPERATIONS_PER_SHOT = 4;
    private static final int COOLDOWN_SCALE = 2560;
    private int cooldown;
    private int beamTtl;
    private final ItemStackHandler lensInventory = new ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof ILensItem; }
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) sendData();
        }
    };

    public KineticAtomicReconstructorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                dev.actuallycreated.registry.ACBlockEntities.KINETIC_ATOMIC_RECONSTRUCTOR.get(),
                (be, side) -> be.lensInventory);
    }

    public static int cooldownForRpm(float rpm) {
        double speed = Math.abs((double) rpm);
        if (!Double.isFinite(speed) || speed <= 0)
            return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, Math.ceil(COOLDOWN_SCALE / speed)));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;
        if (cooldown > 0)
            cooldown--;
        if (beamTtl > 0)
            beamTtl--;
        if (getSpeed() == 0 || cooldown > 0)
            return;

        if (getLens() != ActuallyAdditionsAPI.lensDefaultConversion) {
            if (ActuallyAdditionsAPI.methodHandler.invokeReconstructor(this)) {
                cooldown = cooldownForRpm(getSpeed());
                setChanged();
            }
            return;
        }

        // Scan live handlers every ready tick: insertion, belt movement and assembly
        // updates all trigger a shot without requiring a redstone/block update.
        Direction front = getBlockState().getValue(KineticAtomicReconstructorBlock.FACING);
        int reach = (int) Math.ceil(getMaxDistance());
        for (int distance = 1; distance <= reach; distance++) {
            BlockPos target = worldPosition.relative(front, distance);
            if (!level.isLoaded(target))
                break;

            // Belts and depots are valid beam targets rather than obstructions. Check
            // the beam block itself first (direct hit), then the transport one block
            // below a horizontal beam (the usual item height).
            boolean directTransport = hasTransportHandler(target);
            if (!level.getBlockState(target).isAir() && !directTransport)
                break;
            int transportOffsets = front.getAxis().isHorizontal() ? 1 : 0;
            for (int offset = 0; offset <= transportOffsets; offset++) {
                if (processTransported(target.below(offset), reach)) {
                    cooldown = cooldownForRpm(getSpeed());
                    setChanged();
                    return;
                }
            }
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(target),
                    item -> item.isAlive() && !item.getItem().isEmpty());
            items.sort(Comparator.comparingDouble(item -> item.position().distanceToSqr(worldPosition.getCenter())));
            for (ItemEntity item : items) {
                if (item.position().distanceTo(worldPosition.getCenter()) > reach + 0.5)
                    continue;
                if (process(item)) {
                    cooldown = cooldownForRpm(getSpeed());
                    setChanged();
                    return;
                }
            }
        }
    }

    private boolean hasTransportHandler(BlockPos pos) {
        return BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE) != null;
    }

    /**
     * An empty optional means no conversion; an empty output list is a valid
     * assembly failure.
     */
    private Optional<Conversion> convert(ItemStack input) {
        if (input.isEmpty())
            return Optional.empty();
        Optional<RecipeHolder<ReconstructingRecipe>> assembly = SequencedAssemblyRecipe.getRecipe(
                level, input, ACRecipeTypes.RECONSTRUCTING.getType(), ReconstructingRecipe.class);
        if (assembly.isPresent()) {
            return Optional.of(new Conversion(1, assembly.get().value().rollResults(level.random)));
        }
        // Never let a native recipe bypass the next required assembly step.
        if (input.has(AllDataComponents.SEQUENCED_ASSEMBLY))
            return Optional.empty();
        return NativeReconstructionAdapter.find(level, input).map(holder -> {
            int operations = Math.min(MAX_OPERATIONS_PER_SHOT, input.getCount());
            List<ItemStack> outputs = new ArrayList<>();
            for (int i = 0; i < operations; i++) {
                ItemStack result = holder.value().getResultItem(level.registryAccess()).copy();
                if (!result.isEmpty())
                    outputs.add(result);
            }
            return new Conversion(operations, outputs);
        });
    }

    private boolean processTransported(BlockPos pos, float reach) {
        if (!level.isLoaded(pos))
            return false;
        TransportedItemStackHandlerBehaviour handler = BlockEntityBehaviour.get(
                level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null)
            return false;
        boolean[] processed = { false };
        handler.handleProcessingOnAllItems(transported -> {
            if (processed[0] || transported.locked || transported.lockedExternally)
                return TransportedResult.doNothing();
            var position = handler.getWorldPositionOf(transported);
            Direction front = getAirflowOriginSide();
            double along = position.subtract(worldPosition.getCenter())
                    .dot(net.minecraft.world.phys.Vec3.atLowerCornerOf(front.getNormal()));
            if (along <= 0 || along > reach + .5)
                return TransportedResult.doNothing();
            Optional<Conversion> conversion = convert(transported.stack);
            if (conversion.isEmpty())
                return TransportedResult.doNothing();
            processed[0] = true;
            fireLaser(position);
            Conversion result = conversion.get();
            List<TransportedItemStack> outputs = new ArrayList<>();
            // Keep unprocessed input on the depot surface so subsequent shots can finish
            // it.
            if (transported.stack.getCount() > result.consumed()) {
                TransportedItemStack remainder = transported.copy();
                remainder.stack.shrink(result.consumed());
                outputs.add(remainder);
            }
            for (ItemStack output : result.outputs()) {
                TransportedItemStack converted = transported.copy();
                converted.stack = output;
                converted.clearFanProcessingData();
                outputs.add(converted);
            }
            return TransportedResult.convertTo(outputs);
        });
        return processed[0];
    }

    private boolean process(ItemEntity entity) {
        ItemStack input = entity.getItem();
        Optional<Conversion> conversion = convert(input);
        if (conversion.isEmpty())
            return false;
        fireLaser(entity.position());
        input.shrink(conversion.get().consumed());
        for (ItemStack output : conversion.get().outputs()) {
            ItemEntity result = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), output);
            result.setDeltaMovement(entity.getDeltaMovement());
            level.addFreshEntity(result);
        }
        if (input.isEmpty())
            entity.discard();
        else
            entity.setItem(input);
        return true;
    }

    private record Conversion(int consumed, List<ItemStack> outputs) {
    }

    private void fireLaser(Vec3 target) {
        if (!(level instanceof ServerLevel server))
            return;
        BlockPos end = laserEnd(worldPosition, getAirflowOriginSide(), target);
        // Match AA exactly: integer block coordinates at both ends. Its renderer
        // adds the center offset; neither endpoint tracks the item's height/offset.
        PacketHelperServer.spawnLaserWithTimeServer(server, worldPosition.getX(), worldPosition.getY(),
                worldPosition.getZ(),
                end.getX(), end.getY(), end.getZ(), 0x1b6fff, Math.min(10, cooldownForRpm(getSpeed())), 0, .2f, .8f);
        server.playSound(null, worldPosition, AASounds.RECONSTRUCTOR.get(), SoundSource.BLOCKS, .35f, 1f);
    }

    public static BlockPos laserEnd(BlockPos origin, Direction facing, Vec3 itemPosition) {
        double along = itemPosition.subtract(origin.getCenter()).dot(Vec3.atLowerCornerOf(facing.getNormal()));
        return origin.relative(facing, Math.max(1, (int) Math.round(along)));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("ReconstructingCooldown", cooldown);
        tag.put("Lens", lensInventory.serializeNBT(registries));
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        cooldown = Math.max(0, tag.getInt("ReconstructingCooldown"));
        lensInventory.deserializeNBT(registries, tag.getCompound("Lens"));
    }

    public ItemStackHandler getLensInventory() { return lensInventory; }
    @Override public Lens getLens() {
        ItemStack stack = lensInventory.getStackInSlot(0);
        return stack.getItem() instanceof ILensItem lensItem ? lensItem.getLens() : ActuallyAdditionsAPI.lensDefaultConversion;
    }
    @Override public Direction getOrientation() { return getAirflowOriginSide(); }
    @Override public void resetBeam(int ttl) { resetBeam(ttl, getLens().getColor()); }
    @Override public void resetBeam(int ttl, int color) { beamTtl = ttl; }
    @Override public int getTTL() { return beamTtl; }
    @Override public BlockPos getPosition() { return worldPosition; }
    @Override public int getX() { return worldPosition.getX(); }
    @Override public int getY() { return worldPosition.getY(); }
    @Override public int getZ() { return worldPosition.getZ(); }
    @Override public Level getWorldObject() { return level; }
    @Override public void extractEnergy(int amount) { }
    @Override public int getEnergy() { return Integer.MAX_VALUE; }

    @Override
    @Nullable
    public AirCurrent getAirCurrent() {
        return null;
    }

    @Override
    @Nullable
    public Level getAirCurrentWorld() {
        return level;
    }

    @Override
    public BlockPos getAirCurrentPos() {
        return worldPosition;
    }

    @Override
    public Direction getAirflowOriginSide() {
        return getBlockState().getValue(KineticAtomicReconstructorBlock.FACING);
    }

    @Override
    public Direction getAirFlowDirection() {
        return getAirflowOriginSide();
    }

    @Override
    public boolean isSourceRemoved() {
        return isRemoved();
    }
}
