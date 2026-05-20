package com.enviouse.bloodlinesae.brain.behavior;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.WendigoState;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class EmitStalkingSounds<E extends WendigoEntity> extends ExtendedBehaviour<E> {

    private int nextSoundTick = 0;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of(Pair.of(ModMemoryModuleTypes.WENDIGO_STATE.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT)
                == WendigoState.STALKING;
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return BrainUtils.memoryOrDefault(entity, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT)
                == WendigoState.STALKING;
    }

    @Override
    protected void tick(E entity) {
        if (entity.tickCount < this.nextSoundTick) return;

        SoundEvent[] stalkSounds = {
                ModSounds.WENDIGO_BREATHING.get(),
                ModSounds.WENDIGO_FOOTSTEP.get(),
                ModSounds.WENDIGO_GROWL_LOW.get(),
                ModSounds.WENDIGO_SNIFF.get()
        };
        SoundEvent chosen = stalkSounds[entity.getRandom().nextInt(stalkSounds.length)];

        entity.level().playSound(
                null,
                entity.blockPosition(),
                chosen,
                SoundSource.HOSTILE,
                0.6F + entity.getRandom().nextFloat() * 0.3F,
                0.9F + entity.getRandom().nextFloat() * 0.2F
        );

        this.nextSoundTick = entity.tickCount + 100 + entity.getRandom().nextInt(200);
    }
}
