package com.enviouse.bloodlinesae.worldgen.feature;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {

    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Bloodlinesae.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BRANCHING_TREE =
            FEATURES.register("branching_tree", BranchingTreeFeature::new);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LANDFILL =
            FEATURES.register("landfill", LandfillFeature::new);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MUD_PUDDLE =
            FEATURES.register("mud_puddle", MudPuddleFeature::new);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ROCK_FORMATION =
            FEATURES.register("rock_formation", RockFormationFeature::new);
}
