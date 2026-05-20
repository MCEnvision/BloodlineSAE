package com.enviouse.bloodlinesae.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class BloodlineDriftParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    private BloodlineDriftParticle(ClientLevel level, double x, double y, double z,
                                   double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.sprites = sprites;
        this.friction = 0.96f;
        this.gravity = -0.0025f;            // negative = drifts up
        this.xd = (this.random.nextDouble() - 0.5) * 0.01;
        this.yd = 0.015 + this.random.nextDouble() * 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.01;
        this.lifetime = 90 + this.random.nextInt(140);
        this.quadSize = 0.04f + this.random.nextFloat() * 0.08f;
        // Cyan / electric blue — slight variance for shimmer.
        float blueShift = 0.78f + this.random.nextFloat() * 0.22f;
        this.rCol = 0.20f;
        this.gCol = 0.55f + this.random.nextFloat() * 0.25f;
        this.bCol = blueShift;
        this.alpha = 0.65f;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        // Fade tail
        float ageFrac = (float) this.age / (float) this.lifetime;
        this.alpha = 0.65f * (1.0f - ageFrac);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new BloodlineDriftParticle(level, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
