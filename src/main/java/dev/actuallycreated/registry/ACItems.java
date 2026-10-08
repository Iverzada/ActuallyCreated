package dev.actuallycreated.registry;

import com.tterrag.registrate.util.entry.ItemEntry;
import dev.actuallycreated.ActuallyCreated;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public final class ACItems {
    public static final ItemEntry<Item> KINEK_CRYSTAL = crystal("kinek_crystal", "Kinek Crystal", "palis_crystal");
    public static final ItemEntry<Item> MECHA_CRYSTAL = crystal("mecha_crystal", "Mecha Crystal", "enori_crystal");

    private static ItemEntry<Item> crystal(String id, String name, String texture) {
        return ActuallyCreated.REGISTRATE.item(id, Item::new)
                .lang(name)
                .model((context, provider) -> provider.getBuilder(context.getName())
                        .parent(new ModelFile.UncheckedModelFile(
                                ResourceLocation.fromNamespaceAndPath("actuallyadditions", "item/" + texture))))
                .register();
    }

    public static void register() { }

    private ACItems() { }
}
