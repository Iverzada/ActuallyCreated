package dev.actuallycreated;

import dev.actuallycreated.content.display.ActuallyCreatedDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

/**
 * Display source registration. Attach an entry to a block in AllBlocks with
 * transform(DisplaySource.displaySource(entry)).
 */
public class AllDisplaySources {

    public static final RegistryEntry<DisplaySource, ActuallyCreatedDisplaySource> EXAMPLE_SOURCE = ActuallyCreated.REGISTRATE
            .displaySource("actuallycreated_source", ActuallyCreatedDisplaySource::new)
            .register();

    public static void register() {
        // Force class loading to trigger Registrate calls
    }
}
