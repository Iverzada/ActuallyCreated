package dev.actuallycreated.datagen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiConsumer;

import dev.actuallycreated.ActuallyCreated;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Merges the addon's hand-authored language partials into the lang file that Registrate
 * generates during runData. This mirrors how Create keeps its English copy in
 * {@code assets/create/lang/default/*.json} rather than in Java: the strings live in
 * {@code assets/actuallycreated/lang/default/*.json}, and only the translation <em>keys</em>
 * appear in code (see {@link dev.actuallycreated.Lang}).
 *
 * <p>Registrate already writes a single {@code en_us.json} (block and item names, creative
 * tab, etc.). Rather than write a second, conflicting file, this merger feeds the partials
 * into that same provider via {@link ActuallyCreated#registerLangPartials()}, so the partials
 * and the generated names end up merged in one place. Add a new partial by dropping a JSON
 * file under {@code lang/default} and listing its name in {@link #PARTIALS}.
 */
public class ActuallyCreatedLangMerger {

    private static final Gson GSON = new Gson();

    /** Partial file names under {@code assets/actuallycreated/lang/default/}, without ".json". */
    private static final String[] PARTIALS = {"interface"};

    /**
     * Reads every partial and forwards its entries to the consumer (Registrate's lang
     * provider add-method). Runs only during data generation.
     */
    public static void mergeInto(BiConsumer<String, String> consumer) {
        for (String partial : PARTIALS) {
            String path = "assets/" + ActuallyCreated.ID + "/lang/default/" + partial + ".json";
            try (InputStream in = ActuallyCreatedLangMerger.class.getClassLoader().getResourceAsStream(path)) {
                if (in == null) {
                    ActuallyCreated.LOGGER.error("Lang partial not found on the classpath: {}", path);
                    continue;
                }
                JsonObject json = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    consumer.accept(entry.getKey(), entry.getValue().getAsString());
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read lang partial " + path, e);
            }
        }
    }
}
