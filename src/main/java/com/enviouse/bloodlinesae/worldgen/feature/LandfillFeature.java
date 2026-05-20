package com.enviouse.bloodlinesae.worldgen.feature;

import com.enviouse.bloodlinesae.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * "Fills in" ocean / deep water basins inside the bloodline biome.
 * Runs in LOCAL_MODIFICATIONS (decoration step 2). For every column in this chunk,
 * scans down from sea level and:
 *   - if water-depth at this column is > {@code DEEP_WATER_THRESHOLD}, fills the
 *     water with bloodline dirt + a grass top so the chunk has dry land instead of ocean
 *   - thin water (rivers, shore puddles) is left untouched
 */
public class LandfillFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SEA_LEVEL = 63;
    private static final int FLOOR_SCAN = 30;            // lowest Y to scan
    private static final int DEEP_WATER_THRESHOLD = 4;   // > 4 blocks of water = ocean, fill

    public LandfillFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        int cx = origin.getX();
        int cz = origin.getZ();

        BlockState grass = ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get().defaultBlockState();
        BlockState dirt  = ModBlocks.BLOODLINE_DIRT_OFFICIAL.get().defaultBlockState();

        boolean changed = false;

        // Iterate the chunk slice covered by this decoration call (16x16 area centered on chunk origin)
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = cx + dx;
                int z = cz + dz;

                // Count contiguous water depth at this column (top-down from SEA_LEVEL).
                int topWaterY = -1;
                int bottomWaterY = -1;
                for (int y = SEA_LEVEL; y >= FLOOR_SCAN; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (level.getBlockState(p).getFluidState().is(Fluids.WATER)) {
                        if (topWaterY < 0) topWaterY = y;
                        bottomWaterY = y;
                    } else if (topWaterY >= 0) {
                        // first non-water below water column; stop
                        break;
                    }
                }
                if (topWaterY < 0 || bottomWaterY < 0) continue;
                int depth = topWaterY - bottomWaterY + 1;
                if (depth <= DEEP_WATER_THRESHOLD) continue; // thin water = river, leave alone

                // Fill from bottomWaterY..topWaterY: stone-equivalent at bottom, dirt in middle, grass on top.
                for (int y = bottomWaterY; y <= topWaterY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (y == topWaterY) {
                        level.setBlock(p, grass, 2);
                    } else {
                        level.setBlock(p, dirt, 2);
                    }
                }
                changed = true;
            }
        }
        return changed;
    }
}
