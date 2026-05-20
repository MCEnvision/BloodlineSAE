package com.enviouse.bloodlinesae.worldgen.feature;

import com.enviouse.bloodlinesae.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Creates a depressed mud puddle: scoops out the surface grass, leaving a circular
 * basin one block lower filled with bloodline mud variants. Looks like a sunken
 * pool rather than a flat surface change.
 */
public class MudPuddleFeature extends Feature<NoneFeatureConfiguration> {

    public MudPuddleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();

        int radius = 2 + rand.nextInt(3); // 2..4 block radius
        int rSq = radius * radius;
        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > rSq) continue;

                BlockPos surface = origin.offset(dx, 0, dz);
                if (!isBloodlineGround(level, surface)) continue;

                // Edge softening: outer ring sometimes skipped to make irregular shape
                if (d2 >= (radius - 1) * (radius - 1) && rand.nextFloat() < 0.35f) continue;

                // Replace top grass with air → makes it look sunken by 1 block
                level.setBlock(surface, Blocks.AIR.defaultBlockState(), 2);
                // The dirt 1 block below becomes mud, plus the level below stays dirt
                level.setBlock(surface.below(), pickMud(rand), 2);
                placed = true;
            }
        }
        return placed;
    }

    private static boolean isBloodlineGround(WorldGenLevel level, BlockPos pos) {
        Block b = level.getBlockState(pos).getBlock();
        return b == ModBlocks.BLOODLINE_GRASS_BLOCK.get()
                || b == ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get()
                || b == ModBlocks.BLOODLINE_DIRT.get()
                || b == ModBlocks.BLOODLINE_DIRT_OFFICIAL.get();
    }

    private static BlockState pickMud(RandomSource rand) {
        return rand.nextBoolean()
                ? ModBlocks.BLOODLINE_MUD.get().defaultBlockState()
                : ModBlocks.BLOODLINE_MUD2.get().defaultBlockState();
    }
}
