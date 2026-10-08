package dev.actuallycreated.content.empowering;

import java.util.List;

import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.EmpowererRecipe;
import dev.actuallycreated.registry.ACBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.joml.Vector3f;
import net.minecraft.core.particles.DustParticleOptions;

public class MechanicalEmpoweringStandBlockEntity extends AbstractEmpoweringStandBlockEntity {
    private int progress;
    private int processingTicks;
    private RecipeHolder<EmpowererRecipe> activeRecipe;
    private boolean readyOutput;
    private final IItemHandler automation = new IItemHandler() {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || readyOutput || !inventory.getStackInSlot(0).isEmpty()) return stack;
            ItemStack remainder = stack.copy();
            ItemStack one = stack.copyWithCount(1);
            if (!inventory.insertItem(0, one, simulate).isEmpty()) return stack;
            remainder.shrink(1);
            return remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || !readyOutput) return ItemStack.EMPTY;
            ItemStack extracted = inventory.extractItem(0, amount, simulate);
            if (!simulate && !extracted.isEmpty() && !inventory.getStackInSlot(0).isEmpty()) {
                readyOutput = true;
                setChanged();
                if (level != null && !level.isClientSide) sendData();
            }
            return extracted;
        }
        @Override public int getSlotLimit(int slot) { return slot == 0 ? inventory.getSlotLimit(0) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0; }
    };

    public MechanicalEmpoweringStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ACBlockEntities.MECHANICAL_EMPOWERING_STAND.get(),
                (be, side) -> be.automation);
    }

    @Override
    protected void inventoryChanged() {
        readyOutput = false;
        resetProgress();
        super.inventoryChanged();
    }

    @Override
    public void tick() {
        super.tick();
        tickProcessing();
    }

    public void tickProcessing() {
        if (!(level instanceof ServerLevel server)) return;
        ClockworkDisplayStandBlockEntity[] stands = nearbyStands();
        if (stands == null) { resetProgress(); return; }
        if (readyOutput) return;

        RecipeHolder<EmpowererRecipe> found = findRecipe(stands);
        if (found == null) { resetProgress(); return; }
        if (activeRecipe == null || activeRecipe.value() != found.value()) {
            progress = 0;
            activeRecipe = found;
        }

        processingTicks = processingTime(found.value().getTime(), getSpeed());
        if (getSpeed() == 0 || !allRotating(stands)) return;
        if (++progress < processingTicks) {
            if (progress % 5 == 0) {
                sendData();
                spawnParticles(server, stands, found.value());
            }
            setChanged();
            return;
        }

        // Recheck the complete recipe and structure immediately before changing any inventory.
        ClockworkDisplayStandBlockEntity[] currentStands = nearbyStands();
        if (currentStands == null || !allRotating(currentStands)
                || findRecipe(currentStands) != found) {
            resetProgress();
            return;
        }
        ItemStack result = found.value().getOutput().copy();
        if (result.isEmpty() || result.getCount() > inventory.getSlotLimit(0)) {
            resetProgress();
            return;
        }
        for (ClockworkDisplayStandBlockEntity stand : currentStands)
            stand.getInventory().extractItem(0, 1, false);
        inventory.setStackInSlot(0, result);
        readyOutput = true;
        setChanged();
        sendData();
        spawnParticles(server, currentStands, found.value());
        resetProgress();
    }

    private ClockworkDisplayStandBlockEntity[] nearbyStands() {
        ClockworkDisplayStandBlockEntity[] stands = new ClockworkDisplayStandBlockEntity[4];
        for (int i = 0; i < stands.length; i++) {
            BlockPos pos = worldPosition.relative(Direction.from2DDataValue(i), EmpoweringKinetics.STAND_DISTANCE);
            if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof ClockworkDisplayStandBlockEntity stand))
                return null;
            stands[i] = stand;
        }
        return stands;
    }

    private static boolean allRotating(ClockworkDisplayStandBlockEntity[] stands) {
        for (ClockworkDisplayStandBlockEntity stand : stands)
            if (stand.getSpeed() == 0) return false;
        return true;
    }

    private RecipeHolder<EmpowererRecipe> findRecipe(ClockworkDisplayStandBlockEntity[] stands) {
        ItemStack base = inventory.getStackInSlot(0);
        if (base.getCount() != 1) return null;
        List<RecipeHolder<EmpowererRecipe>> recipes =
                level.getRecipeManager().getAllRecipesFor(ActuallyRecipes.Types.EMPOWERING.get());
        for (RecipeHolder<EmpowererRecipe> holder : recipes) {
            EmpowererRecipe recipe = holder.value();
            if (recipe.matches(base, stands[0].getDisplayedItem(), stands[1].getDisplayedItem(),
                    stands[2].getDisplayedItem(), stands[3].getDisplayedItem()))
                return holder;
        }
        return null;
    }

    public static int processingTime(int recipeTime, float rpm) {
        double speed = Math.abs((double) rpm);
        if (!Double.isFinite(speed) || speed <= 0) return Integer.MAX_VALUE;
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(1, Math.ceil((double) recipeTime * EmpoweringKinetics.BASELINE_RPM / speed)));
    }

    private void resetProgress() {
        if (progress == 0 && processingTicks == 0 && activeRecipe == null) return;
        progress = 0;
        processingTicks = 0;
        activeRecipe = null;
        setChanged();
        if (level != null && !level.isClientSide) sendData();
    }

    private void spawnParticles(ServerLevel server, ClockworkDisplayStandBlockEntity[] stands, EmpowererRecipe recipe) {
        int color = recipe.getParticleColors();
        // Keep a hint of the recipe color, with a warm brass/steam tint.
        float red = Math.min(1, ((color >> 16 & 255) / 255f) * .25f + .75f);
        float green = Math.min(1, ((color >> 8 & 255) / 255f) * .25f + .67f);
        float blue = Math.min(1, ((color & 255) / 255f) * .20f + .60f);
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(red, green, blue), .85f);
        for (ClockworkDisplayStandBlockEntity stand : stands) {
            BlockPos pos = stand.getBlockPos();
            double dx = (worldPosition.getX() - pos.getX()) / 3.0;
            double dz = (worldPosition.getZ() - pos.getZ()) / 3.0;
            server.sendParticles(dust, pos.getX() + .5, pos.getY() + .85, pos.getZ() + .5,
                    0, dx * .18, .08, dz * .18, 1);
        }
    }

    public int getProgress() { return progress; }
    public int getProcessingTicks() { return processingTicks; }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Progress", progress);
        tag.putInt("ProcessingTicks", processingTicks);
        tag.putBoolean("ReadyOutput", readyOutput);
        if (activeRecipe != null) tag.putString("ActiveRecipe", activeRecipe.id().toString());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getInt("Progress");
        processingTicks = tag.getInt("ProcessingTicks");
        readyOutput = tag.getBoolean("ReadyOutput");
        activeRecipe = null;
    }
}
