package dev.actuallycreated.registry;

import dev.actuallycreated.ActuallyCreated;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public final class ACPartialModels {

    public static final PartialModel KINETIC_RECONSTRUCTOR_SHAFT =
            PartialModel.of(
                    ActuallyCreated.asResource(
                            "block/kinetic_atomic_reconstructor/shaft"
                    )
            );

    public static final PartialModel COFFEE_PRESS_PLUNGER =
            PartialModel.of(
                    ActuallyCreated.asResource(
                            "block/coffee_press/plunger"
                    )
            );

    public static final PartialModel COFFEE_PRESS_SHAFT =
            PartialModel.of(
                    ActuallyCreated.asResource(
                            "block/coffee_press/shaft"
                    )
            );

    public static final PartialModel COFFEE_PRESS_PIPE_CONNECTION =
            PartialModel.of(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "create",
                            "block/fluid_pipe/rim_connector/down"
                    )
            );

    private ACPartialModels() {}
}
