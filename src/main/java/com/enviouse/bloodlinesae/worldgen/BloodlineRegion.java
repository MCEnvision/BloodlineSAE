package com.enviouse.bloodlinesae.worldgen;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;

public class BloodlineRegion extends Region {

    public BloodlineRegion(ResourceLocation name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Map bloodline forest to a parameter range similar to dark forest / swamp
        // so it appears in pockets of the overworld. The wendigo lifecycle gates the
        // actual encounter, so the biome is allowed to occur in multiple places.
        this.addBiome(mapper,
                ParameterUtils.Temperature.NEUTRAL,
                ParameterUtils.Humidity.HUMID,
                ParameterUtils.Continentalness.INLAND,
                ParameterUtils.Erosion.EROSION_3,
                ParameterUtils.Weirdness.HIGH_SLICE_VARIANT_ASCENDING,
                ParameterUtils.Depth.SURFACE,
                0.0F,
                ModBiomes.BLOODLINE_FOREST);

        this.addBiome(mapper,
                ParameterUtils.Temperature.COOL,
                ParameterUtils.Humidity.WET,
                ParameterUtils.Continentalness.INLAND,
                ParameterUtils.Erosion.EROSION_3,
                ParameterUtils.Weirdness.PEAK_NORMAL,
                ParameterUtils.Depth.SURFACE,
                0.0F,
                ModBiomes.BLOODLINE_FOREST);
    }
}
