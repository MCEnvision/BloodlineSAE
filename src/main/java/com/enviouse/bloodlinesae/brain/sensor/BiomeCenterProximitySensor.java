package com.enviouse.bloodlinesae.brain.sensor;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.ModSensorTypes;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.UUID;

public class BiomeCenterProximitySensor<E extends WendigoEntity> extends ExtendedSensor<E> {

    private static final List<MemoryModuleType<?>> MEMORIES =
            List.of(ModMemoryModuleTypes.DISTANCE_TO_CENTER.get());

    public BiomeCenterProximitySensor() {
        setScanRate(e -> 40);
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.BIOME_CENTER_PROXIMITY.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        UUID assignedUuid = BrainUtils.getMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        if (assignedUuid == null) return;
        Player player = level.getPlayerByUUID(assignedUuid);
        if (player == null) return;

        double dx = player.getX();
        double dz = player.getZ();
        float dist = (float) Math.sqrt(dx * dx + dz * dz);
        BrainUtils.setMemory(entity, ModMemoryModuleTypes.DISTANCE_TO_CENTER.get(), dist);
    }
}
