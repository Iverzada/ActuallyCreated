package dev.actuallycreated.registry;

import dev.actuallycreated.ActuallyCreated;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public final class ACPartialModels {

    public static final PartialModel EMPOWERING_STAND_GEAR =
            PartialModel.of(ActuallyCreated.asResource("block/mechanical_empowering_stand/gear"));

    public static final PartialModel CRYSTALLINE_DYNAMO_SHAFT =
            PartialModel.of(ActuallyCreated.asResource("block/crystalline_dynamo/shaft"));
    public static final PartialModel CRYSTALLINE_DYNAMO_LEFT_JAW =
            PartialModel.of(ActuallyCreated.asResource("block/crystalline_dynamo/left_jaw"));
    public static final PartialModel CRYSTALLINE_DYNAMO_RIGHT_JAW =
            PartialModel.of(ActuallyCreated.asResource("block/crystalline_dynamo/right_jaw"));
    public static final PartialModel CRYSTALLINE_DYNAMO_FIRE =
            PartialModel.of(ActuallyCreated.asResource("block/crystalline_dynamo/fire"));
    public static final PartialModel CRYSTALLINE_DYNAMO_SUPERHEATED_FIRE =
            PartialModel.of(ActuallyCreated.asResource("block/crystalline_dynamo/superheated_fire"));

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
