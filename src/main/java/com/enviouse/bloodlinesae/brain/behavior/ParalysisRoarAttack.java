package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.effect.ModEffects;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class ParalysisRoarAttack<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    private int windupRemaining = 0;
    private boolean executed;
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
        double r = WendigoConfig.SERVER.roarTriggerRange.get();
        if (entity.distanceToSqr(target) > r * r) return false;
        Integer cd = BrainUtils.getMemory(entity, ModMemoryModuleTypes.ROAR_COOLDOWN_TICKS.get());
        return cd == null || cd <= 0;
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return !this.done;
    }

    @Override
    protected void start(E entity) {
        this.windupRemaining = WendigoConfig.SERVER.roarWindupTicks.get();
        this.executed = false;
        this.done = false;
        entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "roar");
        entity.level().playSound(null, entity.blockPosition(),
                ModSounds.WENDIGO_ROAR.get(), SoundSource.HOSTILE, 1.6F, 1.0F);
    }

    @Override
    protected void tick(E entity) {
        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);

        if (this.windupRemaining > 0) {
            this.windupRemaining--;
            return;
        }

        if (!this.executed) {
            applyParalysisToNearbyPlayers(entity);
            this.executed = true;
            int cd = WendigoConfig.SERVER.roarCooldownTicks.get();
            BrainUtils.setForgettableMemory(entity, ModMemoryModuleTypes.ROAR_COOLDOWN_TICKS.get(), cd, cd);
            this.done = true;
        }
    }

    private void applyParalysisToNearbyPlayers(E entity) {
        int duration = WendigoConfig.SERVER.paralysisDurationTicks.get();
        double range = WendigoConfig.SERVER.paralysisApplyRange.get();
        AABB box = entity.getBoundingBox().inflate(range);
        for (Player p : entity.level().getEntitiesOfClass(Player.class, box)) {
            if (p.isCreative() || p.isSpectator()) continue;
            p.addEffect(new MobEffectInstance(
                    ModEffects.PARALYSIS.get(),
                    duration,
                    0,
                    false,
                    true,
                    true
            ));
        }
    }
}
