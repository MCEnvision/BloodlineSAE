package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.WendigoState;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class ChargeAttack<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    private enum Phase { WINDUP, CHARGING, RECOVERY }

    private Phase phase;
    private int phaseTicksRemaining;
    private Vec3 chargeDirection;
    private LivingEntity targetCached;
    private boolean done;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        LivingEntity target = BrainUtils.getTargetOfEntity(entity);
        if (target == null) return false;
        double distSq = entity.distanceToSqr(target);
        double min = WendigoConfig.SERVER.chargeMinRange.get();
        double max = WendigoConfig.SERVER.chargeMaxRange.get();
        boolean inRange = distSq >= min * min && distSq <= max * max;
        Integer cd = BrainUtils.getMemory(entity, ModMemoryModuleTypes.CHARGE_COOLDOWN_TICKS.get());
        boolean cooldownReady = cd == null || cd <= 0;
        WendigoState state = BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        return inRange && cooldownReady && state != WendigoState.STUNNED;
    }

    @Override
    protected void start(E entity) {
        this.targetCached = BrainUtils.getTargetOfEntity(entity);
        this.phase = Phase.WINDUP;
        this.phaseTicksRemaining = WendigoConfig.SERVER.chargeWindupTicks.get();
        this.done = false;
        entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "attack2");
        entity.level().playSound(null, entity.blockPosition(),
                ModSounds.WENDIGO_CHARGE_WARN.get(), SoundSource.HOSTILE, 1.2F, 1.0F);
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return !this.done;
    }

    @Override
    protected void tick(E entity) {
        this.phaseTicksRemaining--;
        switch (this.phase) {
            case WINDUP -> tickWindup(entity);
            case CHARGING -> tickCharging(entity);
            case RECOVERY -> tickRecovery(entity);
        }
    }

    private void tickWindup(E entity) {
        if (this.targetCached != null) {
            entity.getLookControl().setLookAt(this.targetCached);
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        }
        if (this.phaseTicksRemaining <= 0 && this.targetCached != null) {
            Vec3 toTarget = this.targetCached.position().subtract(entity.position()).normalize();
            this.chargeDirection = new Vec3(toTarget.x, 0, toTarget.z);
            this.phase = Phase.CHARGING;
            this.phaseTicksRemaining = WendigoConfig.SERVER.chargeDurationTicks.get();
            entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "run2");
        }
    }

    private void tickCharging(E entity) {
        if (this.chargeDirection == null) {
            startRecovery(entity, 5);
            return;
        }
        double speed = WendigoConfig.SERVER.chargeSpeed.get();
        Vec3 currentVel = entity.getDeltaMovement();
        entity.setDeltaMovement(
                this.chargeDirection.x * speed,
                currentVel.y,
                this.chargeDirection.z * speed
        );
        entity.hasImpulse = true;

        if (this.targetCached != null
                && this.targetCached.isAlive()
                && entity.distanceToSqr(this.targetCached) < 2.5 * 2.5
                && entity.tickCount > entity.lastChargeHitTick + 10) {

            float damage = WendigoConfig.SERVER.chargeDamage.get().floatValue();
            this.targetCached.hurt(entity.damageSources().mobAttack(entity), damage);
            this.targetCached.push(
                    this.chargeDirection.x * 2.0,
                    0.3,
                    this.chargeDirection.z * 2.0
            );
            entity.lastChargeHitTick = entity.tickCount;
            applyChargeCooldown(entity);
            startRecovery(entity, 10);
            return;
        }

        if (entity.horizontalCollision) {
            entity.level().playSound(null, entity.blockPosition(),
                    ModSounds.WENDIGO_CHARGE_IMPACT.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
            int stun = WendigoConfig.SERVER.wallHitStunTicks.get();
            BrainUtils.setForgettableMemory(entity,
                    ModMemoryModuleTypes.STUNNED_TICKS_REMAINING.get(),
                    stun, stun);
            BrainUtils.setMemory(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.STUNNED);
            entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "idle");
            applyChargeCooldown(entity);
            startRecovery(entity, 0);
            return;
        }

        if (this.phaseTicksRemaining <= 0) {
            applyChargeCooldown(entity);
            startRecovery(entity, 10);
        }
    }

    private void tickRecovery(E entity) {
        if (this.phaseTicksRemaining <= 0) {
            this.done = true;
        }
    }

    private void startRecovery(E entity, int ticks) {
        this.phase = Phase.RECOVERY;
        this.phaseTicksRemaining = ticks;
    }

    private void applyChargeCooldown(E entity) {
        int cd = WendigoConfig.SERVER.chargeCooldownTicks.get();
        BrainUtils.setForgettableMemory(entity, ModMemoryModuleTypes.CHARGE_COOLDOWN_TICKS.get(), cd, cd);
    }
}
