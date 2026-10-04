package dev.actuallycreated.content.ponder;

import dev.actuallycreated.content.ponder.util.PonderScene;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class DeskbellScenes extends PonderScene {

    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        var scene = new CreateSceneBuilder(builder);
        scene.title("desk_bell_intro", "The Desk Bell");
        setupScene(5, scene);

        scene.idle(10);

        var bellPos = util.grid().at(2, 1, 2);
        var bellSelection = util.select().position(bellPos);

        reveal(scene, bellSelection, Direction.DOWN);

        narrateAbove(scene, util, "This is the desk bell.", bellPos);
        waitDefaultDelay(scene);

        scene.overlay().showControls(util.vector().topOf(bellPos), Pointing.DOWN, 40)
                .rightClick();
        scene.idle(7);

        ring(scene, bellPos);

        scene.idle(45);

        // REDSTONE SECTION

        var wiringSelection = util.select().fromTo(2, 1, 3, 0, 1, 4);

        wiringSelection.forEach(pos -> {
            reveal(scene, util.select().position(pos), Direction.DOWN);
            scene.idle(1);
        });

        var redstoneLamp = util.select().position(0, 1, 2);
        reveal(scene, redstoneLamp, Direction.DOWN);

        scene.idle(DEFAULT_DELAY);

        highlightBox(scene, wiringSelection);
        narrateAbove(scene, util, "The desk bell will produce a redstone signal when pressed.", util.grid().at(1, 1, 3));

        scene.idle(7);

        scene.world().toggleRedstonePower(wiringSelection);
        scene.world().toggleRedstonePower(redstoneLamp);

        ring(scene, bellPos);
        scene.idle(20);

        scene.world().toggleRedstonePower(wiringSelection);
        scene.world().toggleRedstonePower(redstoneLamp);

        scene.idle(40);

        // DEPLOYER SECTION

        var contraption = util.select().fromTo(4, 1, 1, 4, 1, 2);
        var deployerPos = util.grid().at(4, 1, 2);
        reveal(scene, contraption, Direction.DOWN);

        scene.addKeyframe();
        scene.idle(40);


        showTextAbove(scene, util, "The bell can also be rang using a deployer", deployerPos);

        applyKineticSpeedAt(scene, util, contraption, 16f);

        scene.world().moveDeployer(deployerPos, 0.8f, 16);

        scene.idle(16);

        scene.world().moveDeployer(deployerPos, -0.8f, 16);

        scene.world().toggleRedstonePower(wiringSelection);
        scene.world().toggleRedstonePower(redstoneLamp);

        ring(scene, bellPos);

        scene.idle(16);
        applyKineticSpeedAt(scene, util, contraption, 0f);
        scene.idle(4);

        scene.world().toggleRedstonePower(wiringSelection);
        scene.world().toggleRedstonePower(redstoneLamp);

        scene.idle(35);
    }

    public static void ring(SceneBuilder scene, BlockPos bellPos) {
        scene.world().modifyBlockEntity(bellPos, DeskBellBlockEntity.class, DeskBellBlockEntity::ding);
        playSound(scene, AllSoundEvents.DESK_BELL_USE);
    }
}
