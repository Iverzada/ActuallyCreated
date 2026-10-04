package dev.actuallycreated;

import java.util.concurrent.CompletableFuture;

import dev.actuallycreated.content.ponder.ActuallyCreatedPonderPlugin;
import dev.actuallycreated.registry.ACBlocks;
import dev.actuallycreated.registry.ACBlockEntities;
import dev.actuallycreated.registry.ACRecipeTypes;
import dev.actuallycreated.registry.ACRecipeSerializers;
import dev.actuallycreated.datagen.ActuallyCreatedCompactingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedCrushingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedCuttingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedDeployingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedEmptyingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedFillingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedLangMerger;
import dev.actuallycreated.datagen.ActuallyCreatedHauntingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedMillingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedMixingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedPressingRecipeGen;
import dev.actuallycreated.datagen.ActuallyCreatedSequencedAssemblyGen;
import dev.actuallycreated.datagen.ActuallyCreatedWashingRecipeGen;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ActuallyCreated.ID)
public class ActuallyCreated {
    public static final String ID = "actuallycreated";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public ActuallyCreated(IEventBus modBus) {
        REGISTRATE.registerEventListeners(modBus);

        AllCreativeModeTabs.register();
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.MAIN_TAB);
        registerLangPartials();
        registerPonderLang();
        AllItems.register();
        AllDisplaySources.register();
        AllBlocks.register();
        AllBlockEntityTypes.register();
        ACBlocks.register();
        ACBlockEntities.register();
        ACRecipeTypes.register();
        ACRecipeSerializers.register();

        modBus.addListener(this::onCommonSetup);
        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onGatherData);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup...");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Client setup...");
        event.enqueueWork(() -> {
            // Ponder is client-only
            PonderIndex.addPlugin(new ActuallyCreatedPonderPlugin());
        });
    }

    /**
     * Feeds the hand-authored language partials (assets/actuallycreated/lang/default/*.json)
     * into Registrate's lang provider so runData merges them with the generated block and
     * item names into a single en_us.json. Keeps English copy out of Java, mirroring how
     * Create authors its own translations.
     */
    private void registerLangPartials() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, provider ->
                ActuallyCreatedLangMerger.mergeInto(provider::add));
    }

    /**
     * Feeds the Ponder scenes' text (titles and captions) into Registrate's lang provider
     * so runData writes it into the same en_us.json as the block and item names. The
     * registered callback only runs during data generation, so it is safe to touch the
     * client-only PonderIndex from here.
     */
    private void registerPonderLang() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            PonderIndex.addPlugin(new ActuallyCreatedPonderPlugin());
            PonderIndex.getLangAccess().provideLang(ID, provider::add);
        });
    }

    /**
     * Registers the data generators. Running gradlew runData writes their output into
     * src/generated/resources.
     */
    private void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ActuallyCreatedSequencedAssemblyGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedWashingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedHauntingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedCrushingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedMillingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedPressingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedCuttingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedMixingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedCompactingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedFillingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedEmptyingRecipeGen(output, registries));
        generator.addProvider(event.includeServer(), new ActuallyCreatedDeployingRecipeGen(output, registries));
    }
}
