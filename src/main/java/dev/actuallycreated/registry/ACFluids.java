package dev.actuallycreated.registry;

import com.tterrag.registrate.util.entry.FluidEntry;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public final class ACFluids {
    private static final ResourceLocation STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    private static final int BUCKET_COFFEE_COLOR = 0xFF17100C;

    public static final FluidEntry<BaseFlowingFluid.Flowing> BLACK_COFFEE = coffee("black_coffee", "Black Coffee", 0xFF3A1F12);
    public static final FluidEntry<BaseFlowingFluid.Flowing> MILK_COFFEE = coffee("milk_coffee", "Milk Coffee", 0xFFA97850);
    public static final FluidEntry<BaseFlowingFluid.Flowing> SWEET_COFFEE = coffee("sweet_coffee", "Sweet Coffee", 0xFF57301B);
    public static final FluidEntry<BaseFlowingFluid.Flowing> MAGMA_COFFEE = coffee("magma_coffee", "Magma Coffee", 0xFFD05020);
    public static final FluidEntry<BaseFlowingFluid.Flowing> PUFFER_COFFEE = coffee("puffer_coffee", "Puffer Coffee", 0xFFC99540);
    public static final FluidEntry<BaseFlowingFluid.Flowing> NIGHT_VISION_COFFEE = coffee("night_vision_coffee", "Night Vision Coffee", 0xFF76613B);
    public static final FluidEntry<BaseFlowingFluid.Flowing> REGENERATION_COFFEE = coffee("regeneration_coffee", "Regeneration Coffee", 0xFFA04455);
    public static final FluidEntry<BaseFlowingFluid.Flowing> STRENGTH_COFFEE = coffee("strength_coffee", "Strength Coffee", 0xFF6A251F);
    public static final FluidEntry<BaseFlowingFluid.Flowing> INVISIBILITY_COFFEE = coffee("invisibility_coffee", "Invisibility Coffee", 0xFF5D526B);

    private static FluidEntry<BaseFlowingFluid.Flowing> coffee(String id, String name, int tint) {
        return ActuallyCreated.REGISTRATE.fluid(id, STILL, FLOWING,
                        (properties, still, flowing) -> new CoffeeFluidType(properties, still, flowing, tint))
                .lang(name)
                .properties(p -> p.density(1050).viscosity(1200))
                .renderType(() -> RenderType::translucent)
                .source(BaseFlowingFluid.Source::new)
                .block().build()
                .bucket()
                .color(() -> () -> (stack, tintIndex) -> tintIndex == 1 ? BUCKET_COFFEE_COLOR : 0xFFFFFFFF)
                .model((context, provider) -> provider.getBuilder(context.getName())
                        .texture("base", ResourceLocation.withDefaultNamespace("item/bucket"))
                        .texture("fluid", ResourceLocation.fromNamespaceAndPath("neoforge", "item/mask/bucket_fluid"))
                        .customLoader(DynamicFluidContainerModelBuilder::begin)
                        .fluid(context.getEntry().content)
                        .applyTint(true)
                        .end())
                .build()
                .register();
    }


    private ACFluids() {}
    public static void register() {}
}
