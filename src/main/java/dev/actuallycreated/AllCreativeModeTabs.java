package dev.actuallycreated;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import dev.actuallycreated.registry.ACBlocks;

public class AllCreativeModeTabs {

    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            ActuallyCreated.REGISTRATE.defaultCreativeTab("main_tab", builder ->
                    builder
                            .title(Component.translatable(Lang.CREATIVE_TAB))
                            .icon(() -> new ItemStack(ACBlocks.COFFEE_PRESS.get()))
                            .build()
            ).register();

    public static void register() {
        // Force class loading to trigger Registrate calls
    }
}
