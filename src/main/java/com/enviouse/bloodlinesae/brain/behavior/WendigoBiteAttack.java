package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class WendigoBiteAttack<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    private int windupRemaining = 0;
    private LivingEntity targetCached;
    private boolean delivered;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT),
                Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT)
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);
        if (target == null) return false;
        double r = WendigoConfig.SERVER.biteRange.get();
        return entity.distanceToSqr(target) < r * r;
    }

    @Override
    protected void start(E entity) {
        this.targetCached = BrainUtils.getTargetOfEntity(entity);
        this.windupRemaining = WendigoConfig.SERVER.biteWindupTicks.get();
        this.delivered = false;
        entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "attack");
        entity.level().playSound(null, entity.blockPosition(),
                ModSounds.WENDIGO_BITE_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return !this.delivered;
    }

    @Override
    protected void tick(E entity) {
        if (this.windupRemaining > 0) {
            this.windupRemaining--;
            if (this.windupRemaining == 0) {
                executeHit(entity);
            }
        }
    }

    private void executeHit(E entity) {
        this.delivered = true;
        if (this.targetCached == null || !this.targetCached.isAlive()) return;
        double r = WendigoConfig.SERVER.biteRange.get();
        if (entity.distanceToSqr(this.targetCached) > r * r + 1.0) return;

        float damage = WendigoConfig.SERVER.biteDamage.get().floatValue();
        this.targetCached.hurt(entity.damageSources().mobAttack(entity), damage);
        entity.level().playSound(null, entity.blockPosition(),
                ModSounds.WENDIGO_BITE_HIT.get(), SoundSource.HOSTILE, 1.2F, 1.0F);

        int cd = WendigoConfig.SERVER.biteCooldownTicks.get();
        BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, cd);
    }
}
