package dev.actuallycreated.recipe.reconstructing;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/**
 * Boundary for Actually Additions laser recipes. A future implementation should query its
 * recipe manager directly and perform at most maxOperations conversions on one target.
 */
@FunctionalInterface
public interface NativeReconstructionAdapter {
    boolean tryProcess(Level level, ItemEntity target, int maxOperations);

    // TODO: replace with an adapter for Actually Additions' native LaserRecipe type.
    NativeReconstructionAdapter NONE = (level, target, maxOperations) -> false;
}
