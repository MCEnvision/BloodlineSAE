package com.enviouse.bloodlinesae.block;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Branch pillar block that auto-connects to other branch blocks and bloodline logs
 * on its 6 faces. Visually shows a small central "core" plus an "arm" toward each
 * connected neighbour — like a multipart fence/chain but for tree branches.
 *
 * Three thickness levels share this class via the {@code halfWidth} arg.
 */
public class ConnectableBranchBlock extends Block {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;

    public static final TagKey<Block> BRANCH_CONNECTABLE = TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK,
            new ResourceLocation(Bloodlinesae.MODID, "branch_connectable"));

    private final int halfWidth; // 1..4
    private final VoxelShape core;
    private final VoxelShape armUp, armDown, armNorth, armSouth, armEast, armWest;

    public ConnectableBranchBlock(Properties props, int halfWidth) {
        super(props);
        this.halfWidth = halfWidth;
        float lo = (8 - halfWidth) / 16f;
        float hi = (8 + halfWidth) / 16f;
        this.core   = Shapes.box(lo, lo, lo, hi, hi, hi);
        this.armUp    = Shapes.box(lo, hi, lo,  hi, 1.0, hi);
        this.armDown  = Shapes.box(lo, 0.0, lo, hi, lo,  hi);
        this.armNorth = Shapes.box(lo, lo, 0.0, hi, hi, lo);
        this.armSouth = Shapes.box(lo, lo, hi,  hi, hi, 1.0);
        this.armWest  = Shapes.box(0.0, lo, lo, lo,  hi, hi);
        this.armEast  = Shapes.box(hi,  lo, lo, 1.0, hi, hi);

        registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(EAST,  false).setValue(WEST,  false)
                .setValue(UP,    false).setValue(DOWN,  false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape shape = core;
        if (state.getValue(UP))    shape = Shapes.or(shape, armUp);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, armDown);
        if (state.getValue(NORTH)) shape = Shapes.or(shape, armNorth);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, armSouth);
        if (state.getValue(EAST))  shape = Shapes.or(shape, armEast);
        if (state.getValue(WEST))  shape = Shapes.or(shape, armWest);
        return shape;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) { return true; }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return computeConnections(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        boolean connect = canConnectTo(neighborState);
        return state.setValue(propertyFor(direction), connect);
    }

    private static BooleanProperty propertyFor(Direction d) {
        return switch (d) {
            case UP    -> UP;
            case DOWN  -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
        };
    }

    public static BlockState computeConnections(BlockState base, LevelReader level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockState n = level.getBlockState(pos.relative(d));
            base = base.setValue(propertyFor(d), canConnectTo(n));
        }
        return base;
    }

    public static boolean canConnectTo(BlockState other) {
        if (other.is(BRANCH_CONNECTABLE)) return true;
        // chains hanging from twigs should attach cleanly — no floating links
        if (other.is(net.minecraft.world.level.block.Blocks.CHAIN)) return true;
        // also auto-connect to bloodline logs so branches join the trunk visually
        return other.is(BlockTags.LOGS);
    }

    public int getHalfWidth() { return halfWidth; }
}
