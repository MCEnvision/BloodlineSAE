package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class MoveTowardLastKnownPlayerPos<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    private static final float STALK_SPEED = 0.45f;
    private static final double ARRIVAL_DISTANCE_SQR = 4.0 * 4.0;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(
                Pair.of(ModMemoryModuleTypes.LAST_KNOWN_PLAYER_POS.get(), MemoryStatus.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT)
        );
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        BlockPos pos = BrainUtils.getMemory(entity, ModMemoryModuleTypes.LAST_KNOWN_PLAYER_POS.get());
        return pos != null && entity.blockPosition().distSqr(pos) > ARRIVAL_DISTANCE_SQR;
    }

    @Override
    protected void start(E entity) {
        BlockPos pos = BrainUtils.getMemory(entity, ModMemoryModuleTypes.LAST_KNOWN_PLAYER_POS.get());
        if (pos == null) return;
        BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(pos, STALK_SPEED, 1));
    }
}
