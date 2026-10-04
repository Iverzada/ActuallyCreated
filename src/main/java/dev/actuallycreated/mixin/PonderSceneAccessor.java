package dev.actuallycreated.mixin;

import net.createmod.ponder.foundation.PonderScene;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read/write access to {@link PonderScene}'s private {@code scaleFactor} (the view zoom), for the
 * live-zoom instruction — Ponder's {@code scaleSceneView} only sets it once at build time.
 */
@Mixin(PonderScene.class)
public interface PonderSceneAccessor {
    @Accessor(value = "scaleFactor", remap = false)
    float pnp$getScaleFactor();

    @Accessor(value = "scaleFactor", remap = false)
    void pnp$setScaleFactor(float scaleFactor);
}
