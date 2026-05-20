package com.enviouse.bloodlinesae.worldgen.feature;

import com.enviouse.bloodlinesae.block.ConnectableBranchBlock;
import com.enviouse.bloodlinesae.block.ModBlocks;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * Tall, leafless, procedurally branching tree.
 *
 * Tree shape:
 * - 1x1 / 2x2 / 3x3 trunks at 50 / 35 / 15.
 * - Branches start at Y=15..20, taper through 5 thickness tiers ending in twigs.
 * - Adjacency-aware: a new sub-branch never spawns next to an existing branch step
 *   (excluding the segment it came from). If the picked direction is blocked we try
 *   opposite then perpendicular; if none free, the sub-branch is skipped.
 * - Exactly ONE twig at the very tip of each branch line — no clumps.
 * - Exactly ONE soul-lantern attempt per tree, on a random twig position, 20% chance.
 * - No overlap with neighbouring trunks; no water/ocean ground.
 */
public class BranchingTreeFeature extends Feature<NoneFeatureConfiguration> {

    public BranchingTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static final int NO_OVERLAP_RADIUS = 9;
    private static final int OVERLAP_Y_DEPTH   = 8;
    private static final double OUTER_BAN      = 0.875;
    private static final int BRANCH_START_MIN  = 15;
    private static final int BRANCH_START_MAX  = 20;

    private static final Direction[] HORIZONTAL = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();

        if (!level.getBlockState(origin).isAir()) return false;
        if (!isValidGround(level, origin.below())) return false;
        if (hasNearbyWater(level, origin)) return false;

        int cx = WendigoConfig.SERVER.biomeCenterX.get();
        int cz = WendigoConfig.SERVER.biomeCenterZ.get();
        int radius = WendigoConfig.SERVER.biomeForcedRadius.get();
        double dx0 = origin.getX() - cx;
        double dz0 = origin.getZ() - cz;
        double distNorm = Math.min(1.0, Math.sqrt(dx0 * dx0 + dz0 * dz0) / radius);
        if (distNorm > OUTER_BAN) return false;
        double edgeT = distNorm / OUTER_BAN;
        double spawnChance = Math.pow(1.0 - edgeT, 1.8);
        if (rand.nextFloat() > spawnChance) return false;

        if (hasNearbyTrunk(level, origin)) return false;

        int thickness;
        float t = rand.nextFloat();
        if (t < 0.50f) thickness = 1;
        else if (t < 0.85f) thickness = 2;
        else thickness = 3;

        int minH = 80 + (thickness - 1) * 10;
        int maxH = 100 + (thickness - 1) * 12;
        int height = minH + rand.nextInt(maxH - minH + 1);

        BlockState log = ModBlocks.BLOODLINE_LOG_OFFICIAL.get().defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.Y);

        int placedHeight = height;
        outer:
        for (int dx = 0; dx < thickness; dx++) {
            for (int dz = 0; dz < thickness; dz++) {
                BlockPos base = origin.offset(dx, 0, dz);
                for (int y = 0; y < height; y++) {
                    BlockPos p = base.above(y);
                    if (!canReplace(level, p)) {
                        if (y < 5) return false;
                        placedHeight = Math.min(placedHeight, y);
                        if (placedHeight <= BRANCH_START_MIN) return false;
                        break outer;
                    }
                }
            }
        }
        for (int dx = 0; dx < thickness; dx++) {
            for (int dz = 0; dz < thickness; dz++) {
                BlockPos base = origin.offset(dx, 0, dz);
                for (int y = 0; y < placedHeight; y++) {
                    setBlock(level, base.above(y), log);
                }
            }
        }

        // Track twig positions for later: one lantern, max.
        List<BlockPos> twigTips = new ArrayList<>();

        int branchStart = BRANCH_START_MIN + rand.nextInt(BRANCH_START_MAX - BRANCH_START_MIN + 1);
        if (branchStart >= placedHeight - 4) branchStart = Math.max(BRANCH_START_MIN, placedHeight - 6);

        // -------- Tier-controlled branching --------
        // Off the trunk we spawn 1-2 XL (tier 1) branches at random heights in the branch range,
        // plus 1-2 additional XL branches near the crown. Each XL recursively grows children
        // through tiers 2..7 with strict per-tier child counts. Recursion handles the rest.
        int branchVerticalRange = placedHeight - branchStart - 2;
        int xlCount = 1 + rand.nextInt(2);          // trunk → XL: 1 or 2
        for (int i = 0; i < xlCount; i++) {
            int by = branchStart + rand.nextInt(Math.max(1, branchVerticalRange));
            int sx = rand.nextInt(thickness);
            int sz = rand.nextInt(thickness);
            BlockPos start = origin.offset(sx, by, sz);
            Direction dir = HORIZONTAL[rand.nextInt(HORIZONTAL.length)];
            int len = 4 + thickness + rand.nextInt(3);
            placeBranchTier(level, start, dir, len, rand, 1, twigTips);
        }
        int crownXl = 1 + rand.nextInt(2);          // 1-2 crown XL branches
        for (int i = 0; i < crownXl; i++) {
            int crownY = placedHeight - 2 - rand.nextInt(3);
            int sx = rand.nextInt(thickness);
            int sz = rand.nextInt(thickness);
            BlockPos start = origin.offset(sx, crownY, sz);
            Direction dir = HORIZONTAL[rand.nextInt(HORIZONTAL.length)];
            placeBranchTier(level, start, dir, 3 + rand.nextInt(3), rand, 1, twigTips);
        }

        // Single lantern attempt per tree — ~20% chance, picking a random twig position.
        if (!twigTips.isEmpty() && rand.nextFloat() < 0.20f) {
            BlockPos tip = twigTips.get(rand.nextInt(twigTips.size()));
            tryHangSoulLantern(level, tip, rand);
        }

        // Visual join post-pass.
        refreshBranchConnectionsInBox(level, origin, placedHeight, thickness);

        return true;
    }

    /**
     * Recursive branch placement.
     * - placed[] tracks (line) the segments we've laid so we don't form 2x1 clumps.
     * - The "no two branches next to each other" rule is enforced when spawning child
     *   sub-branches. We pick a starting direction; if its first cell is adjacent to an
     *   existing branch (other than our trunk-line continuation), we try the OPPOSITE,
     *   then perpendiculars, and if none clears we skip this child entirely.
     */
    private void placeBranchTier(WorldGenLevel level, BlockPos start, Direction dir, int length,
                                 RandomSource rand, int tier, List<BlockPos> twigTips) {
        if (length <= 0 || tier > 7) return;

        boolean isLog = tier == 0;

        BlockPos cur = start;
        BlockPos lastPlaced = null;
        Direction lastDir = dir;
        for (int step = 0; step < length; step++) {
            BlockPos next = cur.relative(dir);
            boolean kinkUp = step > 0 && rand.nextFloat() < 0.30f;
            if (kinkUp) {
                BlockPos up = cur.above();
                if (canReplace(level, up)) {
                    placeSegment(level, up, isLog, Direction.Axis.Y, tier);
                    cur = up;
                    lastPlaced = up;
                    continue;
                }
            }
            if (!canReplace(level, next)) break;
            Direction.Axis axis = (dir == Direction.NORTH || dir == Direction.SOUTH) ? Direction.Axis.Z : Direction.Axis.X;
            placeSegment(level, next, isLog, axis, tier);
            cur = next;
            lastPlaced = next;
            lastDir = dir;
        }

        if (lastPlaced == null) return;

        // Twig tier (7) — register tip for lantern selection and stop recursing.
        if (tier >= 7) {
            twigTips.add(lastPlaced);
            return;
        }

        // Per-tier child counts (count of next-thinner branches per parent):
        //   tier 1 (XL → Large):       2-3
        //   tier 2 (Large → Thick):    2-3
        //   tier 3 (Thick → Primary):  2-3
        //   tier 4 (Primary → Medium): 1-2
        //   tier 5 (Medium → Secondary): 1-2
        //   tier 6 (Secondary → Twig): 1
        //   tier 7: terminal (handled above)
        int childCount;
        switch (tier) {
            case 1, 2, 3 -> childCount = 2 + rand.nextInt(2);
            case 4, 5    -> childCount = 1 + rand.nextInt(2);
            default      -> childCount = 1;
        }
        for (int c = 0; c < childCount; c++) {
            Direction childDir = pickFreeDirection(level, lastPlaced, lastDir, rand);
            if (childDir == null) continue;
            int childLen = Math.max(1, length - 1 - rand.nextInt(2));
            placeBranchTier(level, lastPlaced, childDir, childLen, rand, tier + 1, twigTips);
        }
    }

    /**
     * Try a random horizontal direction; if its first cell would land next to an existing
     * branch block (excluding the line we came from), try opposite then perpendiculars.
     * Returns null if every candidate is blocked.
     */
    private Direction pickFreeDirection(WorldGenLevel level, BlockPos from, Direction comingFromDir, RandomSource rand) {
        // Build try-order: random preferred, then opposite, then the two perpendiculars (shuffled).
        Direction preferred = HORIZONTAL[rand.nextInt(HORIZONTAL.length)];
        Direction opposite  = preferred.getOpposite();
        Direction[] perp = perpendiculars(preferred);
        if (rand.nextBoolean()) { Direction tmp = perp[0]; perp[0] = perp[1]; perp[1] = tmp; }

        Direction[] order = { preferred, opposite, perp[0], perp[1] };
        for (Direction d : order) {
            BlockPos firstCell = from.relative(d);
            if (!canReplace(level, firstCell)) continue;
            if (isAdjacentToOtherBranch(level, firstCell, d.getOpposite())) continue;
            return d;
        }
        return null;
    }

    private static Direction[] perpendiculars(Direction d) {
        return switch (d) {
            case NORTH, SOUTH -> new Direction[]{ Direction.EAST, Direction.WEST };
            case EAST, WEST   -> new Direction[]{ Direction.NORTH, Direction.SOUTH };
            default           -> new Direction[]{ Direction.NORTH, Direction.SOUTH };
        };
    }

    /**
     * Returns true if any horizontal neighbour of {@code pos} (other than {@code parentDir})
     * is a branch / log block. Used to refuse 2-wide branch clumps.
     */
    private static boolean isAdjacentToOtherBranch(WorldGenLevel level, BlockPos pos, Direction parentDir) {
        for (Direction d : HORIZONTAL) {
            if (d == parentDir) continue;
            Block b = level.getBlockState(pos.relative(d)).getBlock();
            if (b instanceof ConnectableBranchBlock) return true;
            if (b == ModBlocks.BLOODLINE_LOG_OFFICIAL.get()) return true;
            if (b == ModBlocks.BLOODLINE_LOG.get()) return true;
        }
        return false;
    }

    private BlockState branchStateForTier(int tier) {
        // 8-step taper. Tier 0 is the trunk LOG (has AXIS); 1..7 are connectable branch blocks.
        return switch (tier) {
            case 0  -> ModBlocks.BLOODLINE_LOG_OFFICIAL.get().defaultBlockState();
            case 1  -> ModBlocks.BLOODLINE_BRANCH_XL.get().defaultBlockState();
            case 2  -> ModBlocks.BLOODLINE_BRANCH_LARGE.get().defaultBlockState();
            case 3  -> ModBlocks.BLOODLINE_BRANCH_THICK.get().defaultBlockState();
            case 4  -> ModBlocks.BLOODLINE_BRANCH_PRIMARY.get().defaultBlockState();
            case 5  -> ModBlocks.BLOODLINE_BRANCH_MEDIUM.get().defaultBlockState();
            case 6  -> ModBlocks.BLOODLINE_BRANCH_SECONDARY.get().defaultBlockState();
            default -> ModBlocks.BLOODLINE_BRANCH_TWIG.get().defaultBlockState();
        };
    }

    private void placeSegment(WorldGenLevel level, BlockPos pos, boolean isLog, Direction.Axis axis, int tier) {
        BlockState base = branchStateForTier(tier);
        if (isLog) {
            setBlock(level, pos, base.setValue(BlockStateProperties.AXIS, axis));
        } else {
            setBlock(level, pos, base);
        }
    }

    private void refreshBranchConnectionsInBox(WorldGenLevel level, BlockPos origin, int placedHeight, int thickness) {
        int pad = 14;
        for (int dx = -pad; dx <= thickness + pad; dx++) {
            for (int dz = -pad; dz <= thickness + pad; dz++) {
                for (int dy = 0; dy <= placedHeight + 3; dy++) {
                    BlockPos p = origin.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (!(s.getBlock() instanceof ConnectableBranchBlock)) continue;
                    BlockState updated = ConnectableBranchBlock.computeConnections(s, level, p);
                    if (!updated.equals(s)) level.setBlock(p, updated, 2);
                }
            }
        }
    }

    private void tryHangSoulLantern(WorldGenLevel level, BlockPos branchTip, RandomSource rand) {
        BlockPos chainStart = branchTip.below();
        int chainLen = 1 + rand.nextInt(3);
        for (int i = 0; i < chainLen; i++) {
            BlockPos p = chainStart.below(i);
            if (!canReplace(level, p)) return;
        }
        BlockPos lanternPos = chainStart.below(chainLen);
        if (!canReplace(level, lanternPos)) return;

        BlockState chain = Blocks.CHAIN.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
        for (int i = 0; i < chainLen; i++) {
            setBlock(level, chainStart.below(i), chain);
        }
        BlockState soulLantern = Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        setBlock(level, lanternPos, soulLantern);
    }

    private static boolean isValidGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.LAVA)) return false;
        Block b = state.getBlock();
        return b == ModBlocks.BLOODLINE_GRASS_BLOCK.get()
                || b == ModBlocks.BLOODLINE_DIRT.get()
                || b == ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get()
                || b == ModBlocks.BLOODLINE_DIRT_OFFICIAL.get()
                || b == ModBlocks.BLOODLINE_MUD.get()
                || b == ModBlocks.BLOODLINE_MUD2.get();
    }

    private static boolean hasNearbyWater(WorldGenLevel level, BlockPos origin) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = origin.offset(dx, 0, dz);
                if (level.getBlockState(p).getFluidState().is(Fluids.WATER)) return true;
                if (level.getBlockState(p.below()).getFluidState().is(Fluids.WATER)) return true;
            }
        }
        return false;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        return s.isAir() || s.canBeReplaced();
    }

    private static boolean hasNearbyTrunk(WorldGenLevel level, BlockPos origin) {
        for (int dx = -NO_OVERLAP_RADIUS; dx <= NO_OVERLAP_RADIUS; dx++) {
            for (int dz = -NO_OVERLAP_RADIUS; dz <= NO_OVERLAP_RADIUS; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (dx * dx + dz * dz > NO_OVERLAP_RADIUS * NO_OVERLAP_RADIUS) continue;
                BlockPos p = origin.offset(dx, 0, dz);
                for (int dy = -OVERLAP_Y_DEPTH; dy <= OVERLAP_Y_DEPTH; dy++) {
                    Block b = level.getBlockState(p.above(dy)).getBlock();
                    if (b == ModBlocks.BLOODLINE_LOG.get() || b == ModBlocks.BLOODLINE_LOG_OFFICIAL.get()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
