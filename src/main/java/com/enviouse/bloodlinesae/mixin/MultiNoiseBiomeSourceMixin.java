package com.enviouse.bloodlinesae.mixin;

import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.worldgen.BloodlineBiomeCache;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forces the bloodline forest biome inside a perfect circle centered on
 * (biomeCenterX, biomeCenterZ). The land-fill feature later converts ocean basins
 * within the circle to dry land, so ocean territory inside the circle still becomes
 * forest. Rivers are left alone (looks like blood).
 */
@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"),
            cancellable = true)
    private void bloodlinesae$forceCenterBiome(int x, int y, int z, Climate.Sampler sampler,
                                               CallbackInfoReturnable<Holder<Biome>> cir) {
        Holder<Biome> cached = BloodlineBiomeCache.HOLDER;
        if (cached == null) return;

        MultiNoiseBiomeSource self = (MultiNoiseBiomeSource) (Object) this;
        if (!self.possibleBiomes().contains(cached)) return;

        int blockX = x << 2;
        int blockZ = z << 2;

        int centerX = WendigoConfig.SERVER.biomeCenterX.get();
        int centerZ = WendigoConfig.SERVER.biomeCenterZ.get();
        int radius  = WendigoConfig.SERVER.biomeForcedRadius.get();

        long dx = (long) blockX - centerX;
        long dz = (long) blockZ - centerZ;
        long distSq = dx * dx + dz * dz;
        long rSq    = (long) radius * radius;
        if (distSq <= rSq) {
            cir.setReturnValue(cached);
        }
    }
}
