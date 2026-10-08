package dev.actuallycreated.gametest;

import dev.actuallycreated.content.coffee.CoffeePressBlock;
import dev.actuallycreated.content.coffee.CoffeePressBlockEntity;
import dev.actuallycreated.registry.ACBlocks;
import dev.actuallycreated.registry.ACFluids;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import dev.actuallycreated.ActuallyCreated;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class CoffeePressGameTests {
    private static final BlockPos POS = new BlockPos(1, 2, 1);

    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void stoppedDoesNotProcess(GameTestHelper h) {
        var m = machine(h, 0); beans(m, 1); ticks(m, 20); h.assertTrue(m.getTank().isEmpty(), "Stopped press produced coffee"); h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void beansMakeBlackCoffee(GameTestHelper h) {
        var m = machine(h, 256); beans(m, 1); ticks(m, 20); amount(h, m, 250); h.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(m.getTank().getFluid().getFluid()).getPath().equals("black_coffee"), "Wrong black coffee output"); h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void sugarMakesSweetCoffee(GameTestHelper h) { ingredientRecipe(h, Items.SUGAR, "sweet_coffee"); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void magmaMakesMagmaCoffee(GameTestHelper h) { ingredientRecipe(h, Items.MAGMA_CREAM, "magma_coffee"); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void milkBucketIsNotPressed(GameTestHelper h) {
        h.assertTrue(h.getLevel().getRecipeManager()
                        .byKey(ResourceLocation.fromNamespaceAndPath(ActuallyCreated.ID, "coffee_making/milk_bucket_coffee"))
                        .isEmpty(), "Milk bucket coffee recipe still exists");
        var press = machine(h, 256);
        var remainder = press.getInventory().insertItem(1, new ItemStack(Items.MILK_BUCKET), false);
        h.assertTrue(remainder.is(Items.MILK_BUCKET), "Coffee Press accepted a milk bucket");
        beans(press, 1);
        ticks(press, 20);
        h.assertTrue(press.getTank().getFluid().getFluid() == ACFluids.BLACK_COFFEE.getSource(),
                "Coffee Press should make only black coffee without another ingredient");
        h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void outputIsAlways250Mb(GameTestHelper h) { var m=machine(h,256); beans(m,1); ticks(m,20); amount(h,m,250); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void tankNeverExceedsCapacity(GameTestHelper h) { var m=machine(h,256); fillTank(m); beans(m,1); ticks(m,20); amount(h,m,CoffeePressBlockEntity.TANK_CAPACITY); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void differentCoffeeDoesNotMix(GameTestHelper h) { var m=machine(h,256); beans(m,2); ticks(m,20); m.getInventory().setStackInSlot(1,new ItemStack(Items.SUGAR)); ticks(m,20); amount(h,m,250); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void fullTankDoesNotConsume(GameTestHelper h) { var m=machine(h,256); fillTank(m); beans(m,1); ticks(m,20); h.assertTrue(m.getInventory().getStackInSlot(0).getCount()==1,"Bean consumed with full tank"); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void rpmChangesDuration(GameTestHelper h) { var m=machine(h,16); beans(m,1); m.tickProcessing(); h.assertTrue(m.getProcessingTicks()==80,"16 RPM duration wrong"); m.setSpeed(256); m.tickProcessing(); h.assertTrue(m.getProcessingTicks()==20,"256 RPM duration wrong"); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void shaftOnlyConnectsAbove(GameTestHelper h) { var s=ACBlocks.COFFEE_PRESS.get().defaultBlockState(); var b=ACBlocks.COFFEE_PRESS.get(); h.assertTrue(b.hasShaftTowards(h.getLevel(),h.absolutePos(POS),s,Direction.UP),"Missing top shaft"); for(var d:Direction.values()) if(d!=Direction.UP) h.assertFalse(b.hasShaftTowards(h.getLevel(),h.absolutePos(POS),s,d),"Shaft exposed on "+d); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void fluidCapabilityExtracts(GameTestHelper h) { var m=machine(h,256); beans(m,1); ticks(m,20); var cap=h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,h.absolutePos(POS),Direction.DOWN); h.assertTrue(cap.drain(100,IFluidHandler.FluidAction.EXECUTE).getAmount()==100,"Fluid extraction failed"); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void automationUsesOnlyConfiguredFaces(GameTestHelper h) {
        machine(h, 0);
        var pos = h.absolutePos(POS);
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.EAST) != null,
                "Missing item input on east side");
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.WEST) != null,
                "Missing item input on west side");
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.NORTH) == null,
                "Items exposed through north glass");
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.SOUTH) == null,
                "Items exposed through south glass");
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP) == null,
                "Items exposed on top");
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN) == null,
                "Items exposed on bottom");
        h.assertTrue(h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.DOWN) != null,
                "Coffee output missing on bottom");
        for (var side : Direction.Plane.HORIZONTAL)
            h.assertTrue(h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, side) == null,
                    "Coffee exposed on " + side);
        h.assertTrue(h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP) == null,
                "Coffee exposed on top");
        h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void sideItemCapabilityIsInsertOnly(GameTestHelper h) {
        var m = machine(h, 0);
        var cap = h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, h.absolutePos(POS), Direction.EAST);
        var beans = new ItemStack(de.ellpeck.actuallyadditions.mod.items.ActuallyItems.COFFEE_BEANS.get(), 2);
        h.assertTrue(cap.insertItem(0, beans, false).isEmpty(), "Side rejected coffee beans");
        h.assertTrue(m.getInventory().getStackInSlot(0).getCount() == 2, "Side did not insert coffee beans");
        h.assertTrue(cap.extractItem(0, 1, false).isEmpty(), "Side allowed item extraction");
        h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void fluidCapabilityRejectsInsertion(GameTestHelper h) { var m=machine(h,256); beans(m,1); ticks(m,20); var cap=h.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,h.absolutePos(POS),Direction.DOWN); h.assertTrue(cap.fill(m.getTank().getFluid().copy(),IFluidHandler.FluidAction.EXECUTE)==0,"Output tank accepted fluid"); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void automationRejectsInvalidIngredient(GameTestHelper h) { var m=machine(h,0); var remainder=m.getInventory().insertItem(1,new ItemStack(Items.COBBLESTONE),false); h.assertTrue(remainder.is(Items.COBBLESTONE),"Invalid ingredient accepted"); h.succeed(); }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void stackedIngredientConsumesOnePerOperation(GameTestHelper h) {
        var m = machine(h, 256);
        beans(m, 64);
        m.getInventory().setStackInSlot(1, new ItemStack(Items.SUGAR, 64));
        ticks(m, 20);
        amount(h, m, 250);
        h.assertTrue(m.getInventory().getStackInSlot(1).getCount() == 63,
                "First operation did not consume exactly one sugar");
        ticks(m, 60);
        amount(h, m, 1000);
        h.assertTrue(m.getInventory().getStackInSlot(1).getCount() == 60,
                "Four operations did not consume exactly four sugar");
        h.succeed();
    }
    @GameTest(template = "reconstructorgametests.reconstructor_test") public static void sweetCoffeeCupHasSpeed(GameTestHelper h) {
        var recipe = h.getLevel().getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(ActuallyCreated.ID, "filling/sweet_coffee_cup"))
                .orElseThrow(() -> new AssertionError("Sweet coffee filling recipe was not loaded"));
        ItemStack cup = recipe.value().getResultItem(h.getLevel().registryAccess());
        var effects = ActuallyAdditionsAPI.methodHandler.getEffectsFromStack(cup);
        h.assertTrue(effects != null && effects.length == 1, "Sweet coffee cup has no single effect");
        h.assertTrue(effects[0].getEffect().equals(MobEffects.MOVEMENT_SPEED), "Sweet coffee cup does not grant Speed");
        h.assertTrue(effects[0].getAmplifier() == 0, "Sweet coffee cup does not grant Speed I");
        h.assertTrue(effects[0].getDuration() == 30, "Sweet coffee cup has the wrong AA duration");
        h.succeed();
    }

    private static void ingredientRecipe(GameTestHelper h, net.minecraft.world.item.Item item, String expected) { var m=machine(h,256); beans(m,1); m.getInventory().setStackInSlot(1,new ItemStack(item)); ticks(m,20); amount(h,m,250); h.assertTrue(net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(m.getTank().getFluid().getFluid()).getPath().equals(expected),"Wrong coffee variant"); h.succeed(); }
    private static CoffeePressBlockEntity machine(GameTestHelper h, float speed) { h.setBlock(POS,ACBlocks.COFFEE_PRESS.get().defaultBlockState()); var m=(CoffeePressBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(POS)); m.tick(); m.setSpeed(speed); return m; }
    private static void beans(CoffeePressBlockEntity m,int count) { m.getInventory().setStackInSlot(0,new ItemStack(de.ellpeck.actuallyadditions.mod.items.ActuallyItems.COFFEE_BEANS.get(),count)); }
    private static void ticks(CoffeePressBlockEntity m,int count) { for(int i=0;i<count;i++) m.tickProcessing(); }
    private static void fillTank(CoffeePressBlockEntity m) { for(int i=0;i<64 && m.getTank().getFluidAmount()<CoffeePressBlockEntity.TANK_CAPACITY;i++){ m.setSpeed(256); beans(m,1); ticks(m,20); } }
    private static void amount(GameTestHelper h,CoffeePressBlockEntity m,int expected) { h.assertTrue(m.getTank().getFluidAmount()==expected,"Expected "+expected+" mB, got "+m.getTank().getFluidAmount()); }
}
