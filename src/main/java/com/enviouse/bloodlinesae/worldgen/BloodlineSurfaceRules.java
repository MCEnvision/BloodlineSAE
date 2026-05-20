package com.enviouse.bloodlinesae.worldgen;

import com.enviouse.bloodlinesae.block.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class BloodlineSurfaceRules {

    private BloodlineSurfaceRules() {}

    public static SurfaceRules.RuleSource makeRules() {
        BlockState grassState = ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get().defaultBlockState();
        BlockState dirtState  = ModBlocks.BLOODLINE_DIRT_OFFICIAL.get().defaultBlockState();

        SurfaceRules.RuleSource grass = SurfaceRules.state(grassState);
        SurfaceRules.RuleSource dirt  = SurfaceRules.state(dirtState);

        SurfaceRules.RuleSource surface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grass),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, dirt));

        return SurfaceRules.ifTrue(
                SurfaceRules.isBiome(ModBiomes.BLOODLINE_FOREST),
                surface);
    }
}
