package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.WendigoState;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.UUID;

public class SniffOut<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(
                Pair.of(ModMemoryModuleTypes.ASSIGNED_PLAYER.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(ModMemoryModuleTypes.DETECTION_LEVEL.get(), MemoryStatus.REGISTERED),
                Pair.of(ModMemoryModuleTypes.WENDIGO_STATE.get(), MemoryStatus.VALUE_PRESENT)
        );
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        WendigoState state = BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        return state == WendigoState.DORMANT || state == WendigoState.STALKING || state == WendigoState.LOSING_SCENT;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return shouldKeepRunning(entity);
    }

    @Override
    protected void tick(E entity) {
        ServerLevel level = (ServerLevel) entity.level();
        UUID assignedUuid = BrainUtils.getMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        if (assignedUuid == null) return;
        Player player = level.getPlayerByUUID(assignedUuid);

        WendigoState state = BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        float currentDetection = BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.DETECTION_LEVEL.get(), () -> 0f);

        if (player == null) {
            BrainUtils.setMemory(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.DORMANT);
            float decayed = Mth.clamp(currentDetection - WendigoConfig.SERVER.scentDecayRate.get().floatValue(), 0f, 1f);
            BrainUtils.setMemory(entity, ModMemoryModuleTypes.DETECTION_LEVEL.get(), decayed);
            return;
        }

        float delta;
        if (state == WendigoState.LOSING_SCENT) {
            delta = -WendigoConfig.SERVER.scentDecayRate.get().floatValue();
        } else {
            delta = entity.computeDetectionDelta(player);
        }

        float newDetection = Mth.clamp(currentDetection + delta, 0f, 1f);
        BrainUtils.setMemory(entity, ModMemoryModuleTypes.DETECTION_LEVEL.get(), newDetection);

        // State transitions
        float startStalk = WendigoConfig.SERVER.startStalkingThreshold.get().floatValue();
        float reveal = WendigoConfig.SERVER.revealThreshold.get().floatValue();
        float returnDormant = WendigoConfig.SERVER.returnToDormantThreshold.get().floatValue();

        if (state == WendigoState.DORMANT && newDetection > startStalk) {
            BrainUtils.setMemory(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.STALKING);
            entity.setStalking(true);
        } else if (state == WendigoState.STALKING && newDetection >= reveal) {
            entity.triggerReveal();
        } else if (state == WendigoState.LOSING_SCENT && newDetection < returnDormant) {
            BrainUtils.setMemory(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.DORMANT);
            entity.setStalking(false);
        }

        // Easter egg
        float assScratchChance = WendigoConfig.SERVER.assScratchChancePerTick.get().floatValue();
        if (entity.getRandom().nextFloat() < assScratchChance) {
            entity.triggerAnim(WendigoEntity.MAIN_CONTROLLER, "ass_scratch");
        }
    }
}
