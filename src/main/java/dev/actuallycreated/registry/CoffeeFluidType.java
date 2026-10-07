package dev.actuallycreated.registry;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

public class CoffeeFluidType extends FluidType {
    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final int tint;

    public CoffeeFluidType(Properties properties, ResourceLocation still, ResourceLocation flowing, int tint) {
        super(properties);
        this.still = still;
        this.flowing = flowing;
        this.tint = tint;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override public ResourceLocation getStillTexture() { return still; }
            @Override public ResourceLocation getFlowingTexture() { return flowing; }
            @Override public int getTintColor() { return tint; }
        });
    }
}
