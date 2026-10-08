package dev.actuallycreated.content.dynamo;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.tile.CustomEnergyStorage;
import de.ellpeck.actuallyadditions.mod.tile.IEnergyDisplay;
import dev.actuallycreated.registry.ACBlockEntities;
import dev.actuallycreated.registry.ACItems;
import dev.actuallycreated.content.reconstructor.KineticAtomicReconstructorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CrystallineDynamoBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, IEnergyDisplay {
    public static final float STRESS_IMPACT = KineticAtomicReconstructorBlockEntity.STRESS_IMPACT;
    public static final int ENERGY_CAPACITY = 500_000;
    public static final double WORK_PER_CRYSTAL = 64.0 * 1600.0;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && isCrystal(stack); }
        @Override protected void onContentsChanged(int slot) { changed(); refreshStress(); }
    };
    private final IItemHandler topInput = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(slot, stack, simulate);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return inventory.isItemValid(slot, stack); }
    };
    private final CustomEnergyStorage energy = new CustomEnergyStorage(ENERGY_CAPACITY, 0, ENERGY_CAPACITY) {
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) changed();
            return extracted;
        }
    };
    private final IEnergyStorage output = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return energy.extractEnergy(maxExtract, simulate); }
        @Override public int getEnergyStored() { return energy.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return ENERGY_CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    };

    private double work;
    private int activeEnergy;
    private boolean activeEmpowered;
    private ItemStack activeCrystal = ItemStack.EMPTY;
    private int generated;
    private int lastProduced;
    private boolean processing;

    public CrystallineDynamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ACBlockEntities.CRYSTALLINE_DYNAMO.get(),
                (be, side) -> side == Direction.UP ? be.topInput : null);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ACBlockEntities.CRYSTALLINE_DYNAMO.get(),
                (be, side) -> side != Direction.UP && side != be.getBlockState().getValue(CrystallineDynamoBlock.FACING).getOpposite()
                        ? be.output : null);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) sendData();
    }

    private void refreshStress() {
        float previous = lastStressApplied;
        float current = calculateStressApplied();
        if (previous != current && level != null && !level.isClientSide && hasNetwork())
            getOrCreateNetwork().updateStressFor(this, current);
    }

    @Override
    public float calculateStressApplied() {
        float base = super.calculateStressApplied();
        boolean empowered = activeEnergy > 0 ? activeEmpowered : isEmpoweredCrystal(inventory.getStackInSlot(0));
        lastStressApplied = empowered ? base * 2 : base;
        return lastStressApplied;
    }

    @Override
    public void tick() {
        super.tick();
        if (level != null && level.isClientSide) {
            tickCrystalParticles();
            tickFireParticles();
        }
        else tickEnergy();
    }

    private void tickFireParticles() {
        if (!processing || level.random.nextInt(3) != 0) return;
        Direction front = getBlockState().getValue(CrystallineDynamoBlock.FACING);
        double sideways = (level.random.nextDouble() - 0.5) * 0.32;
        double forward = 0.28 + (level.random.nextDouble() - 0.5) * 0.06;
        double x = worldPosition.getX() + 0.5 + front.getStepX() * forward + front.getStepZ() * sideways;
        double z = worldPosition.getZ() + 0.5 + front.getStepZ() * forward - front.getStepX() * sideways;
        double y = worldPosition.getY() + 0.29 + level.random.nextDouble() * 0.15;
        double dx = (level.random.nextDouble() - 0.5) * 0.015;
        double dz = (level.random.nextDouble() - 0.5) * 0.015;

        level.addParticle(activeEmpowered ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                x, y, z, dx, 0.015 + level.random.nextDouble() * 0.02, dz);
        if (!activeEmpowered && level.random.nextInt(3) == 0)
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.08, z, 0, 0.025, 0);
    }

    private void tickCrystalParticles() {
        if (!processing || activeCrystal.isEmpty() || level.random.nextInt(3) != 0) return;
        Direction front = getBlockState().getValue(CrystallineDynamoBlock.FACING);
        double sideways = (level.random.nextDouble() - 0.5) * 0.35;
        double towardFront = (level.random.nextDouble() - 0.5) * 0.25;
        double x = worldPosition.getX() + 0.5 + front.getStepX() * towardFront
                + front.getStepZ() * sideways;
        double z = worldPosition.getZ() + 0.5 + front.getStepZ() * towardFront
                - front.getStepX() * sideways;
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, activeCrystal),
                x, worldPosition.getY() + 0.77, z,
                (level.random.nextDouble() - 0.5) * 0.04, 0.035,
                (level.random.nextDouble() - 0.5) * 0.04);
    }

    public void tickEnergy() {
        if (level == null || level.isClientSide) return;

        boolean wasProcessing = processing;
        processing = false;
        distributeEnergy();
        int produced = 0;
        double speed = Math.abs((double) getSpeed());
        if (Double.isFinite(speed) && speed > 0) {
            if (activeEnergy == 0) {
                ItemStack fuelStack = inventory.getStackInSlot(0);
                int fuel = energyFor(fuelStack);
                if (fuel > 0 && energy.getEnergyStored() < ENERGY_CAPACITY) {
                    activeEnergy = fuel;
                    activeEmpowered = isEmpoweredCrystal(fuelStack);
                    activeCrystal = fuelStack.copyWithCount(1);
                    work = 0;
                    generated = 0;
                    inventory.extractItem(0, 1, false);
                    refreshStress();
                }
            }
            if (activeEnergy > 0) {
                double nextWork = Math.min(WORK_PER_CRYSTAL, work + speed);
                int target = (int) Math.floor(activeEnergy * nextWork / WORK_PER_CRYSTAL + 1e-9);
                if (nextWork >= WORK_PER_CRYSTAL) target = activeEnergy;
                int wanted = target - generated;
                if (wanted <= ENERGY_CAPACITY - energy.getEnergyStored()) {
                    produced = energy.receiveEnergyInternal(wanted, false);
                    work = nextWork;
                    generated += produced;
                    processing = true;
                    if (work >= WORK_PER_CRYSTAL) {
                        activeEnergy = 0;
                        activeEmpowered = false;
                        activeCrystal = ItemStack.EMPTY;
                        generated = 0;
                        work = 0;
                        processing = false;
                        refreshStress();
                    }
                }
            }
        }
        if (produced != 0 || lastProduced != produced || wasProcessing != processing) {
            lastProduced = produced;
            changed();
        }
        distributeEnergy();
    }

    private void distributeEnergy() {
        if (energy.getEnergyStored() == 0) return;
        Direction rear = getBlockState().getValue(CrystallineDynamoBlock.FACING).getOpposite();
        for (Direction side : Direction.values()) {
            if (side == Direction.UP || side == rear) continue;
            IEnergyStorage receiver = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    worldPosition.relative(side), side.getOpposite());
            if (receiver == null || !receiver.canReceive()) continue;
            int offered = Math.min(energy.getEnergyStored(), receiver.receiveEnergy(energy.getEnergyStored(), true));
            if (offered > 0) {
                int accepted = receiver.receiveEnergy(offered, false);
                energy.extractEnergy(accepted, false);
            }
        }
    }

    public static boolean isCrystal(ItemStack stack) { return energyFor(stack) > 0; }

    public static boolean isEmpoweredCrystal(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_PALIS_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_VOID_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_ENORI_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_DIAMATINE_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_EMERADIC_CRYSTAL.get();
    }

    public static int energyFor(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (item == ACItems.KINEK_CRYSTAL.get()) return 128_000;
        if (item == ACItems.MECHA_CRYSTAL.get()) return 256_000;
        if (item == ActuallyItems.RESTONIA_CRYSTAL.get() || item == ActuallyItems.PALIS_CRYSTAL.get()
                || item == ActuallyItems.VOID_CRYSTAL.get()) return 96_000;
        if (item == ActuallyItems.ENORI_CRYSTAL.get()) return 204_800;
        if (item == ActuallyItems.DIAMATINE_CRYSTAL.get()) return 307_200;
        if (item == ActuallyItems.EMERADIC_CRYSTAL.get()) return 409_600;
        if (item == ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_PALIS_CRYSTAL.get()
                || item == ActuallyItems.EMPOWERED_VOID_CRYSTAL.get()) return 288_000;
        if (item == ActuallyItems.EMPOWERED_ENORI_CRYSTAL.get()) return 614_400;
        if (item == ActuallyItems.EMPOWERED_DIAMATINE_CRYSTAL.get()) return 921_600;
        if (item == ActuallyItems.EMPOWERED_EMERADIC_CRYSTAL.get()) return 1_228_800;
        return 0;
    }

    public ItemStackHandler getInventory() { return inventory; }
    public int getLastProduced() { return lastProduced; }
    public int getActiveEnergy() { return activeEnergy; }
    public double getWork() { return work; }
    public int getStoredCrystalCount() { return inventory.getStackInSlot(0).getCount() + (activeEnergy > 0 ? 1 : 0); }
    public ItemStack getActiveCrystal() { return activeCrystal.copy(); }
    public boolean isProcessing() { return processing; }
    public boolean isProcessingEmpowered() { return processing && activeEmpowered; }

    @Override public CustomEnergyStorage getEnergyStorage() { return energy; }
    @Override public boolean needsHoldShift() { return false; }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, sneaking);
        tooltip.add(Component.translatable("gui.actuallycreated.crystalline_dynamo.energy",
                energy.getEnergyStored(), ENERGY_CAPACITY).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gui.actuallycreated.crystalline_dynamo.rate",
                lastProduced)
                .withStyle(ChatFormatting.GREEN));
        ItemStack queued = inventory.getStackInSlot(0);
        ItemStack processing = activeEnergy > 0 ? activeCrystal : ItemStack.EMPTY;
        if (!queued.isEmpty() || !processing.isEmpty()) {
            tooltip.add(Component.translatable("gui.actuallycreated.crystalline_dynamo.items")
                    .withStyle(ChatFormatting.GRAY));
            if (!processing.isEmpty() && ItemStack.isSameItemSameComponents(processing, queued)) {
                addCrystalTooltip(tooltip, queued.copyWithCount(queued.getCount() + 1));
            } else {
                if (!processing.isEmpty()) addCrystalTooltip(tooltip, processing);
                if (!queued.isEmpty()) addCrystalTooltip(tooltip, queued);
            }
        }
        return true;
    }

    private static void addCrystalTooltip(List<Component> tooltip, ItemStack stack) {
        CreateLang.text("").add(stack.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                .add(CreateLang.text(" x" + stack.getCount()).style(ChatFormatting.GREEN)).forGoggles(tooltip, 1);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", inventory.serializeNBT(registries));
        energy.writeToNBT(tag);
        tag.putDouble("Work", work);
        tag.putInt("ActiveEnergy", activeEnergy);
        tag.putBoolean("ActiveEmpowered", activeEmpowered);
        tag.put("ActiveCrystal", activeCrystal.saveOptional(registries));
        tag.putInt("Generated", generated);
        tag.putInt("LastProduced", lastProduced);
        tag.putBoolean("Processing", processing);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        work = tag.getDouble("Work");
        activeEnergy = tag.getInt("ActiveEnergy");
        activeEmpowered = tag.contains("ActiveEmpowered") ? tag.getBoolean("ActiveEmpowered")
                : activeEnergy == 288_000 || activeEnergy == 614_400
                || activeEnergy == 921_600 || activeEnergy == 1_228_800;
        activeCrystal = ItemStack.parseOptional(registries, tag.getCompound("ActiveCrystal"));
        generated = tag.getInt("Generated");
        lastProduced = tag.getInt("LastProduced");
        processing = tag.getBoolean("Processing");
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        energy.readFromNBT(tag);
    }
}
