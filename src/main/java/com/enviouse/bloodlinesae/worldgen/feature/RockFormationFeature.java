package com.enviouse.bloodlinesae.worldgen.feature;

import com.enviouse.bloodlinesae.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Sculpts a small rock formation of crimstone above the surface. Picks one of:
 *   PILLAR — a 1–3 block tall vertical column (sometimes with a wider base).
 *   GRAVE  — a low mound topped with a single vertical headstone.
 *   CROSS  — a + shape laid horizontally with a vertical post.
 *   HEAP   — irregular cluster of cobblestone-ish boulders 2–3 wide.
 *
 * Used in place of vanilla {@code minecraft:forest_rock} for more shape variety.
 */
public class RockFormationFeature extends Feature<NoneFeatureConfiguration> {

    public RockFormationFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private enum Shape { PILLAR, GRAVE, CROSS, HEAP, PYRAMID, PLUS_CENTER }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();

        if (!validGround(level, origin.below())) return false;

        BlockState stone = ModBlocks.CRIMSTONE_OFFICIAL.get().defaultBlockState();

        Shape shape = Shape.values()[rand.nextInt(Shape.values().length)];
        return switch (shape) {
            case PILLAR      -> placePillar(level, rand, origin, stone);
            case GRAVE       -> placeGrave(level, rand, origin, stone);
            case CROSS       -> placeCross(level, rand, origin, stone);
            case HEAP        -> placeHeap(level, rand, origin, stone);
            case PYRAMID     -> placePyramid(level, rand, origin, stone);
            case PLUS_CENTER -> placePlusCenter(level, rand, origin, stone);
        };
    }

    /** Stepped pyramid: 5x5 base, 3x3 middle, 1x1 cap. */
    private boolean placePyramid(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        // base layer 5x5
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = o.offset(dx, 0, dz);
                if (canReplace(level, p)) level.setBlock(p, stone, 2);
            }
        }
        // middle layer 3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = o.offset(dx, 1, dz);
                if (canReplace(level, p)) level.setBlock(p, stone, 2);
            }
        }
        // cap 1x1
        BlockPos cap = o.above(2);
        if (canReplace(level, cap)) level.setBlock(cap, stone, 2);
        return true;
    }

    /** A horizontal + with its center cube raised one block. */
    private boolean placePlusCenter(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        BlockPos[] arms = {
                o, o.offset(1, 0, 0), o.offset(-1, 0, 0),
                o.offset(0, 0, 1), o.offset(0, 0, -1)
        };
        for (BlockPos p : arms) {
            if (canReplace(level, p)) level.setBlock(p, stone, 2);
        }
        // center raised
        BlockPos raised = o.above();
        if (canReplace(level, raised)) level.setBlock(raised, stone, 2);
        // sometimes a second raised block for a small "spire"
        if (rand.nextFloat() < 0.4f) {
            BlockPos top = raised.above();
            if (canReplace(level, top)) level.setBlock(top, stone, 2);
        }
        return true;
    }

    private boolean placePillar(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        int height = 2 + rand.nextInt(3); // 2..4
        // optional 2-wide base
        if (rand.nextBoolean()) {
            for (int dx = 0; dx <= 1; dx++)
                for (int dz = 0; dz <= 1; dz++)
                    if (canReplace(level, o.offset(dx, 0, dz)))
                        level.setBlock(o.offset(dx, 0, dz), stone, 2);
        }
        for (int y = 0; y < height; y++) {
            BlockPos p = o.above(y);
            if (!canReplace(level, p)) break;
            level.setBlock(p, stone, 2);
        }
        return true;
    }

    private boolean placeGrave(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        // 3x3 low mound (1 block tall)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) >= 3) continue; // round the corners
                BlockPos p = o.offset(dx, 0, dz);
                if (canReplace(level, p)) level.setBlock(p, stone, 2);
            }
        }
        // headstone
        BlockPos head = o.above();
        if (canReplace(level, head)) level.setBlock(head, stone, 2);
        if (rand.nextBoolean() && canReplace(level, head.above())) {
            level.setBlock(head.above(), stone, 2);
        }
        return true;
    }

    private boolean placeCross(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        // vertical post (2-3 tall)
        int postH = 2 + rand.nextInt(2);
        for (int y = 0; y < postH; y++) {
            BlockPos p = o.above(y);
            if (canReplace(level, p)) level.setBlock(p, stone, 2);
        }
        // cross bar at top-1
        int barY = postH - 1;
        BlockPos[] arms = {
                o.offset( 1, barY, 0), o.offset(-1, barY, 0),
                o.offset( 0, barY, 1), o.offset( 0, barY,-1),
        };
        for (BlockPos arm : arms) {
            if (canReplace(level, arm)) level.setBlock(arm, stone, 2);
        }
        return true;
    }

    private boolean placeHeap(WorldGenLevel level, RandomSource rand, BlockPos o, BlockState stone) {
        int width = 1 + rand.nextInt(2);   // half-width 1..2
        int height = 1 + rand.nextInt(2);  // 1..2 blocks tall
        for (int dx = -width; dx <= width; dx++) {
            for (int dz = -width; dz <= width; dz++) {
                int d = dx * dx + dz * dz;
                if (d > width * width) continue;
                for (int dy = 0; dy < height; dy++) {
                    if (rand.nextFloat() < 0.18f) continue; // irregular gaps
                    BlockPos p = o.offset(dx, dy, dz);
                    if (canReplace(level, p)) level.setBlock(p, stone, 2);
                }
            }
        }
        return true;
    }

    private static boolean validGround(WorldGenLevel level, BlockPos pos) {
        Block b = level.getBlockState(pos).getBlock();
        return b == ModBlocks.BLOODLINE_GRASS_BLOCK.get()
                || b == ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get()
                || b == ModBlocks.BLOODLINE_DIRT.get()
                || b == ModBlocks.BLOODLINE_DIRT_OFFICIAL.get()
                || b == ModBlocks.BLOODLINE_MUD.get()
                || b == ModBlocks.BLOODLINE_MUD2.get();
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced();
    }
}
