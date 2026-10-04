package dev.actuallycreated.content.ponder;

import dev.actuallycreated.AllBlocks;
import dev.actuallycreated.ActuallyCreated;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/**
 * Ponder plugin for the addon, registered client-side in ActuallyCreated. registerScenes
 * associates a storyboard with one or more items. Each scene has two parts: a schematic
 * saved as an nbt file under assets/actuallycreated/ponder, whose name matches the id passed
 * to addStoryBoard, and the storyboard code in ActuallyCreatedPonderScenes.
 */
public class ActuallyCreatedPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return ActuallyCreated.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(AllBlocks.EXAMPLE_KINETIC_BLOCK.getId())
                .addStoryBoard("actuallycreated_ponder", ActuallyCreatedPonderScenes::examplePonder);

        helper.forComponents(com.simibubi.create.AllBlocks.DESK_BELL.getId())
                .addStoryBoard("desk_bell", DeskbellScenes::intro);
    }
}
