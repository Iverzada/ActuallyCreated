package dev.actuallycreated.content.coffee;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.mojang.blaze3d.vertex.PoseStack;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.actuallycreated.recipe.coffee.CoffeeMakingRecipe;
import dev.actuallycreated.recipe.coffee.CoffeePressRecipeInput;
import dev.actuallycreated.registry.ACBlockEntities;
import dev.actuallycreated.registry.ACRecipeTypes;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CoffeePressBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {
    public static final float STRESS_IMPACT = 4.0f;
    public static final int TANK_CAPACITY = 4000;
    private static final int MIN_PROCESSING_TICKS = 20;

    private int progress;
    private int processingTicks;
    private FilteringBehaviour filter;
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0)
                return stack.is(ActuallyTags.Items.COFFEE_BEANS);
            if (level == null || stack.isEmpty())
                return false;
            return level.getRecipeManager().getAllRecipesFor(ACRecipeTypes.COFFEE_MAKING.getType()).stream()
                    .anyMatch(holder -> {
                        CoffeeMakingRecipe recipe = (CoffeeMakingRecipe) (Object) holder.value();
                        return !recipe.ingredient().isEmpty() && recipe.ingredient().test(stack);
                    });
        }

        @Override
        protected void onContentsChanged(int slot) {
            changed();
        }
    };
    private final IItemHandler inputInventory = new IItemHandler() {
        @Override
        public int getSlots() { return inventory.getSlots(); }

        @Override
        public ItemStack getStackInSlot(int slot) { return inventory.getStackInSlot(slot); }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }

        @Override
        public int getSlotLimit(int slot) { return inventory.getSlotLimit(slot); }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return inventory.isItemValid(slot, stack); }
    };
    private final OutputTank tank = new OutputTank();

    public CoffeePressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filter = new FilteringBehaviour(this, new CoffeeFilterSlot())
                .forFluids()
                .withCallback($ -> changed());
        behaviours.add(filter);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ACBlockEntities.COFFEE_PRESS.get(),
                (be, side) -> side != null
                        && side.getAxis().isHorizontal()
                        && side.getAxis() != be.getBlockState().getValue(CoffeePressBlock.FACING).getAxis()
                                ? be.inputInventory : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ACBlockEntities.COFFEE_PRESS.get(),
                (be, side) -> side == Direction.DOWN ? be.tank : null);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide)
            sendData();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null)
            return;

        if (Math.abs(getSpeed()) < 1.0f) {
            progress = 0;
            processingTicks = 0;
            return;
        }

        var found = level.getRecipeManager().getRecipeFor(ACRecipeTypes.COFFEE_MAKING.getType(),
                new CoffeePressRecipeInput(inventory.getStackInSlot(0), inventory.getStackInSlot(1)), level);
        if (found.isEmpty() || !canAccept((CoffeeMakingRecipe) found.get().value())) {
            progress = 0;
            processingTicks = 0;
            return;
        }

        CoffeeMakingRecipe recipe = (CoffeeMakingRecipe) found.get().value();
        int wantedTicks = Math.max(
                MIN_PROCESSING_TICKS,
                (int) Math.ceil(
                    (double) recipe.duration() / Math.abs(getSpeed())
                )
        );

        if (processingTicks != wantedTicks) {
            processingTicks = wantedTicks;
            progress = 0;
        }
        if (++progress < processingTicks) {
            setChanged();
            return;
        }
        if (!canAccept(recipe)) {
            progress = 0;
            return;
        }

        inventory.extractItem(0, recipe.coffeeCount(), false);
        if (!recipe.ingredient().isEmpty()) {
            ItemStack used = inventory.extractItem(1, 1, false);
            ItemStack remainder = used.getCraftingRemainingItem();
            if (!remainder.isEmpty())
                inventory.setStackInSlot(1, remainder);
        }
        tank.fillInternal(recipe.result(), FluidAction.EXECUTE);
        progress = 0;
        changed();
    }

    private boolean canAccept(CoffeeMakingRecipe recipe) {
        FluidStack output = recipe.result();
        FluidStack stored = tank.getFluid();
        return output.getAmount() > 0 && tank.getSpace() >= output.getAmount()
                && (stored.isEmpty() || FluidStack.isSameFluidSameComponents(stored, output))
                && (filter == null || filter.getFilter().isEmpty() || filter.test(output));
    }

    public float getPressProgress(float partialTicks) {
        if (!isProcessing())
            return 0;

        float cycle = Math.min(
            1.0f,
            (progress + partialTicks) / (float) processingTicks
    );

        if (cycle < 0.30f) {
            return smoothStep(cycle / 0.30f);
    }

        if (cycle < 0.70f) {
            return 1.0f;
    }

        return 1.0f
            - smoothStep(
                    (cycle - 0.70f) / 0.30f
            );
    }

        private static float smoothStep(float value) {
            value = Math.max(
                0.0f,
                Math.min(1.0f, value)
        );

            return value
                * value
                * (3.0f - 2.0f * value);
    }

    public ItemStackHandler getInventory() { return inventory; }
    public FluidTank getTank() { return tank; }
    public int getProgress() { return progress; }
    public int getProcessingTicks() { return processingTicks; }
    public boolean isProcessing() { return processingTicks > 0 && progress > 0; }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, sneaking);
        boolean hasItems = false;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            if (!hasItems) {
                tooltip.add(Component.translatable("gui.actuallycreated.coffee_press.items").withStyle(ChatFormatting.GRAY));
                hasItems = true;
            }
            CreateLang.text("").add(stack.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                    .add(CreateLang.text(" x" + stack.getCount()).style(ChatFormatting.GREEN)).forGoggles(tooltip, 1);
        }
        return containedFluidTooltip(tooltip, sneaking, tank) || hasItems || added;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
        tag.putInt("ProcessingTicks", processingTicks);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        progress = tag.getInt("Progress");
        processingTicks = tag.getInt("ProcessingTicks");
    }

    private static class CoffeeFilterSlot extends ValueBoxTransform {
        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return rotateHorizontally(state, VecHelper.voxelSpace(8, 14, 15.5));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack poseStack) {
            Direction side = state.getValue(CoffeePressBlock.FACING);
            TransformStack.of(poseStack)
                    .rotateYDegrees(AngleHelper.horizontalAngle(side) + 180);
        }
    }

    private class OutputTank extends FluidTank {
        OutputTank() { super(TANK_CAPACITY); }

        @Override
        public int fill(FluidStack resource, FluidAction action) { return 0; }

        int fillInternal(FluidStack resource, FluidAction action) { return super.fill(resource, action); }

        @Override
        protected void onContentsChanged() { changed(); }
    }
}
