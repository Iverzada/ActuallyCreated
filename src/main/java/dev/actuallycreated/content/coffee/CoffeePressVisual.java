package dev.actuallycreated.content.coffee;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;

import dev.actuallycreated.registry.ACPartialModels;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

import net.minecraft.core.Direction;

public class CoffeePressVisual
        extends SingleAxisRotatingVisual<CoffeePressBlockEntity>
        implements SimpleDynamicVisual {

    private static final float MAX_PRESS_TRAVEL =
            4.0f / 16.0f;
    private static final float ONE_PIXEL = 1.0f / 16.0f;

    private final OrientedInstance plunger;

    public CoffeePressVisual(
            VisualizationContext context,
            CoffeePressBlockEntity blockEntity,
            float partialTick
    ) {

        super(
                context,
                blockEntity,
                partialTick,
                Direction.SOUTH,
                Models.partial(
                        ACPartialModels.COFFEE_PRESS_SHAFT
                )
        );

        /*
         * Cria a instância do plunger.
         */
        plunger =
                instancerProvider()
                        .instancer(
                                InstanceTypes.ORIENTED,
                                Models.partial(
                                        ACPartialModels.COFFEE_PRESS_PLUNGER
                                )
                        )
                        .createInstance();

        updatePlunger(partialTick);
    }

    @Override
    public void beginFrame(
            DynamicVisual.Context context
    ) {
        updatePlunger(
                context.partialTick()
        );
    }

    private void updatePlunger(float partialTick) {
        float progress =
                blockEntity.getPressProgress(
                        partialTick
                );

        float yOffset =
                progress * MAX_PRESS_TRAVEL;

        plunger
                .position(
                        getVisualPosition()
                )
                .translatePosition(
                        0,
                        ONE_PIXEL - yOffset,
                        0
                )
                .setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(plunger);
    }

    @Override
    protected void _delete() {
        super._delete();

        plunger.delete();
    }

    @Override
    public void collectCrumblingInstances(
            Consumer<Instance> consumer
    ) {
        super.collectCrumblingInstances(
                consumer
        );

        consumer.accept(plunger);
    }
}
