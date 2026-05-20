package com.enviouse.bloodlinesae.worldgen;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomes {

    private ModBiomes() {}

    public static final ResourceKey<Biome> BLOODLINE_FOREST = ResourceKey.create(
            Registries.BIOME,
            new ResourceLocation(Bloodlinesae.MODID, "bloodline_forest"));

    public static final TagKey<Biome> IS_BLOODLINE_FOREST = TagKey.create(
            Registries.BIOME,
            new ResourceLocation(Bloodlinesae.MODID, "is_bloodline_forest"));
}
