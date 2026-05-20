package com.enviouse.bloodlinesae.particle;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {

    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Bloodlinesae.MODID);

    public static final RegistryObject<SimpleParticleType> BLOODLINE_DRIFT =
            PARTICLES.register("bloodline_drift", () -> new SimpleParticleType(false));
}
