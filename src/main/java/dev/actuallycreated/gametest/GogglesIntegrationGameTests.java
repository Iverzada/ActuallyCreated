package dev.actuallycreated.gametest;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.items.ItemEngineerGoggles;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ActuallyCreated.ID)
@PrefixGameTestTemplate(false)
public final class GogglesIntegrationGameTests {
    private static final BlockPos POS = new BlockPos(1, 2, 1);

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void createGogglesProvideActuallyAdditionsFeatures(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(AllItems.GOGGLES.get()));
        h.assertTrue(ItemEngineerGoggles.isWearing(player), "AA does not recognize Create goggles");
        h.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IGoggles goggles
                && !goggles.displaySpectralMobs(), "Create goggles should not reveal spectral mobs");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void actuallyAdditionsGogglesProvideCreateFeatures(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ActuallyItems.ENGINEERS_GOGGLES.get()));
        h.assertTrue(GogglesItem.isWearingGoggles(player), "Create does not recognize Engineer's Goggles");
        h.assertTrue(!((IGoggles) player.getItemBySlot(EquipmentSlot.HEAD).getItem()).displaySpectralMobs(),
                "Basic Engineer's Goggles should not reveal spectral mobs");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ActuallyItems.ENGINEERS_GOGGLES_ADVANCED.get()));
        h.assertTrue(GogglesItem.isWearingGoggles(player), "Create does not recognize advanced Engineer's Goggles");
        h.assertTrue(((IGoggles) player.getItemBySlot(EquipmentSlot.HEAD).getItem()).displaySpectralMobs(),
                "Only advanced Engineer's Goggles should reveal spectral mobs");
        h.succeed();
    }

    @GameTest(template = "reconstructorgametests.reconstructor_test")
    public static void engineerGogglesCanBeInstalledOnCreateBlocks(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ActuallyItems.ENGINEERS_GOGGLES.get()));

        h.setBlock(POS, AllBlocks.BLAZE_BURNER.getDefaultState()
                .setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING));
        h.useBlock(POS, player);
        var burner = (BlazeBurnerBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(POS));
        h.assertTrue(burner.goggles, "Blaze Burner rejected Engineer's Goggles");

        h.setBlock(POS, AllBlocks.PACKAGE_FROGPORT.getDefaultState());
        h.useBlock(POS, player);
        var frogport = (FrogportBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(POS));
        h.assertTrue(frogport.goggles, "Frogport rejected Engineer's Goggles");

        h.setBlock(POS, AllBlocks.MECHANICAL_ARM.getDefaultState());
        h.useBlock(POS, player);
        var arm = h.getLevel().getBlockEntity(h.absolutePos(POS));
        h.assertTrue(arm.saveWithoutMetadata(h.getLevel().registryAccess()).getBoolean("Goggles"),
                "Mechanical Arm rejected Engineer's Goggles");
        h.succeed();
    }
}
