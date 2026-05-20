package com.enviouse.bloodlinesae.block;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class ModBlocks {

    private ModBlocks() {}

    public static final RegistryObject<Block> BLOODLINE_LOG = registerWithItem("bloodline_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.NETHER)
                    .strength(2.5F)));

    public static final RegistryObject<Block> STRIPPED_BLOODLINE_LOG = registerWithItem("stripped_bloodline_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_DARK_OAK_LOG)
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(2.5F)));

    public static final RegistryObject<Block> BLOODLINE_PLANKS = registerWithItem("bloodline_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_PLANKS)
                    .mapColor(MapColor.NETHER)
                    .strength(2.0F)));

    public static final RegistryObject<Block> BLOODLINE_LEAVES = registerWithItem("bloodline_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LEAVES)
                    .mapColor(MapColor.NETHER)
                    .noOcclusion()
                    .isValidSpawn(ModBlocks::ocelotOrParrot)
                    .isSuffocating(ModBlocks::neverSuffocate)
                    .isViewBlocking(ModBlocks::neverSuffocate)));

    public static final RegistryObject<Block> BLOODLINE_SAPLING = registerWithItem("bloodline_sapling",
            () -> new SaplingBlock(new BloodlineTreeGrower(),
                    BlockBehaviour.Properties.copy(Blocks.DARK_OAK_SAPLING)));

    public static final RegistryObject<Block> BLOODLINE_DIRT = registerWithItem("bloodline_dirt",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.5F)));

    public static final RegistryObject<Block> BLOODLINE_GRASS_BLOCK = registerWithItem("bloodline_grass_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.NETHER)
                    .strength(0.6F)));

    public static final RegistryObject<Block> CRIMSTONE = registerWithItem("crimstone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.CRIMSON_HYPHAE)
                    .strength(3.0F, 8.0F)));

    public static final RegistryObject<Block> BONE_PILE = registerWithItem("bone_pile",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BONE_BLOCK)
                    .mapColor(MapColor.SAND)
                    .pushReaction(PushReaction.DESTROY)
                    .strength(1.5F)));

    // ---------- Official-texture variant set (second skin, no leaves) ----------

    public static final RegistryObject<Block> BLOODLINE_LOG_OFFICIAL = registerWithItem("bloodline_log_official",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F)));

    public static final RegistryObject<Block> BLOODLINE_DIRT_OFFICIAL = registerWithItem("bloodline_dirt_official",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.5F)));

    public static final RegistryObject<Block> BLOODLINE_GRASS_BLOCK_OFFICIAL = registerWithItem("bloodline_grass_block_official",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
                    .mapColor(MapColor.NETHER)
                    .strength(0.6F)));

    public static final RegistryObject<Block> CRIMSTONE_OFFICIAL = registerWithItem("crimstone_official",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .mapColor(MapColor.CRIMSON_HYPHAE)
                    .strength(3.0F, 8.0F)));

    public static final RegistryObject<Block> BLOODLINE_MUD = registerWithItem("bloodline_mud",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.5F)));

    public static final RegistryObject<Block> BLOODLINE_MUD2 = registerWithItem("bloodline_mud2",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MUD)
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(0.5F)));

    // Procedural branch pillars — same log texture, shrinking voxel cross-sections.
    // 8-step taper used by BranchingTreeFeature:
    //   trunk(8) → xl(7) → large(6) → thick(5) → primary(4) → medium(3) → secondary(2) → twig(1)
    public static final RegistryObject<Block> BLOODLINE_BRANCH_XL = registerWithItem("bloodline_branch_xl",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F)
                    .noOcclusion(), 7));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_LARGE = registerWithItem("bloodline_branch_large",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.5F)
                    .noOcclusion(), 6));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_THICK = registerWithItem("bloodline_branch_thick",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.2F)
                    .noOcclusion(), 5));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_PRIMARY = registerWithItem("bloodline_branch_primary",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(2.0F)
                    .noOcclusion(), 4));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_MEDIUM = registerWithItem("bloodline_branch_medium",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.6F)
                    .noOcclusion(), 3));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_SECONDARY = registerWithItem("bloodline_branch_secondary",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(1.0F)
                    .noOcclusion(), 2));

    public static final RegistryObject<Block> BLOODLINE_BRANCH_TWIG = registerWithItem("bloodline_branch_twig",
            () -> new ConnectableBranchBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(0.5F)
                    .noOcclusion(), 1));

    // A "branch" decoration block - a cross-render plant block with the branch silhouette.
    public static final RegistryObject<Block> BLOODLINE_BRANCH = registerWithItem("bloodline_branch",
            () -> new net.minecraft.world.level.block.BushBlock(BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH)
                    .noCollission()
                    .instabreak()
                    .sound(net.minecraft.world.level.block.SoundType.WOOD)
                    .mapColor(MapColor.NETHER)) {
                @Override
                protected boolean mayPlaceOn(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
                    return true;
                }
            });

    private static RegistryObject<Block> registerWithItem(String name, Supplier<Block> blockSupplier) {
        RegistryObject<Block> obj = Bloodlinesae.BLOCKS.register(name, blockSupplier);
        Bloodlinesae.ITEMS.register(name, () -> new BlockItem(obj.get(), new Item.Properties()));
        return obj;
    }

    private static boolean ocelotOrParrot(BlockState state, BlockGetter getter, BlockPos pos, net.minecraft.world.entity.EntityType<?> type) {
        return type == net.minecraft.world.entity.EntityType.OCELOT || type == net.minecraft.world.entity.EntityType.PARROT;
    }

    private static boolean neverSuffocate(BlockState state, BlockGetter getter, BlockPos pos) {
        return false;
    }

    public static void touch() {}

    /**
     * Sapling grower → triggers the bloodline_tree configured feature.
     */
    public static class BloodlineTreeGrower extends AbstractTreeGrower {
        @Nullable
        @Override
        protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> getConfiguredFeature(
                net.minecraft.util.RandomSource rand, boolean hasFlowers) {
            return net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
                    new net.minecraft.resources.ResourceLocation(Bloodlinesae.MODID, "bloodline_tree"));
        }
    }
}
