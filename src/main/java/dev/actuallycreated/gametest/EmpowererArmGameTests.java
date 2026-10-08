package dev.actuallycreated.gametest;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityEmpowerer;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class EmpowererArmGameTests {
    private static final BlockPos ARM_POS = new BlockPos(1, 2, 1);
    private static final BlockPos TARGET_POS = new BlockPos(3, 2, 1);

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void armCanInsertAndExtractFromDisplayStand(GameTestHelper h) {
        var arm = arm(h);
        h.setBlock(TARGET_POS, ActuallyBlocks.DISPLAY_STAND.getBlock().defaultBlockState());
        var point = point(h, "display_stand");

        h.assertTrue(point.getSlotCount(arm) == 1, "Display Stand has no arm-accessible slot");
        h.assertTrue(point.insert(arm, new ItemStack(Items.BRICK), false).isEmpty(),
                "Mechanical Arm could not insert into Display Stand");
        h.assertTrue(point.extract(arm, 0, 1, false).is(Items.BRICK),
                "Mechanical Arm could not extract from Display Stand");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void armOnlyExtractsFinishedEmpowererOutput(GameTestHelper h) {
        var arm = arm(h);
        h.setBlock(TARGET_POS, ActuallyBlocks.EMPOWERER.getBlock().defaultBlockState());
        var point = point(h, "empowerer");

        h.assertTrue(point.getSlotCount(arm) == 1, "Empowerer has no arm-accessible slot");
        h.assertTrue(point.insert(arm, new ItemStack(ActuallyItems.RESTONIA_CRYSTAL.get()), false).isEmpty(),
                "Mechanical Arm could not insert the base crystal");
        h.assertTrue(point.extract(arm, 0, 1, false).isEmpty(),
                "Mechanical Arm removed the base crystal before empowering");

        var empowerer = (TileEntityEmpowerer) h.getLevel().getBlockEntity(h.absolutePos(TARGET_POS));
        empowerer.inv.setStackInSlot(0, new ItemStack(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()));
        h.assertTrue(point.extract(arm, 0, 1, false).is(ActuallyItems.EMPOWERED_RESTONIA_CRYSTAL.get()),
                "Mechanical Arm could not collect the empowered crystal");
        h.succeed();
    }

    private static ArmBlockEntity arm(GameTestHelper h) {
        h.setBlock(ARM_POS, AllBlocks.MECHANICAL_ARM.get().defaultBlockState());
        return (ArmBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(ARM_POS));
    }

    private static ArmInteractionPoint point(GameTestHelper h, String id) {
        var pos = h.absolutePos(TARGET_POS);
        var point = ArmInteractionPoint.create(h.getLevel(), pos, h.getLevel().getBlockState(pos));
        h.assertTrue(point != null, "Mechanical Arm cannot select " + id);
        h.assertTrue(point.serialize(h.absolutePos(ARM_POS)).getString("Type").equals(ActuallyCreated.ID + ":" + id),
                "Mechanical Arm selected the wrong target type for " + id);
        return point;
    }

    private EmpowererArmGameTests() { }
}
