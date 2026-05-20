package com.enviouse.bloodlinesae.brain.sensor;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.ModSensorTypes;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.lifecycle.WendigoAssignmentData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.UUID;

public class AssignedPlayerSensor<E extends WendigoEntity> extends ExtendedSensor<E> {

    private static final List<MemoryModuleType<?>> MEMORIES = List.of(
            ModMemoryModuleTypes.ASSIGNED_PLAYER.get(),
            ModMemoryModuleTypes.LAST_KNOWN_PLAYER_POS.get(),
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.ATTACK_TARGET
    );

    public AssignedPlayerSensor() {
        setScanRate(e -> 10);
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.ASSIGNED_PLAYER.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        UUID assignedUuid = BrainUtils.getMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        if (assignedUuid == null) {
            // 1. Check the lifecycle ledger first (spawned by per-player spawn handler).
            UUID lookup = WendigoAssignmentData.get(level).getPlayerForWendigo(entity.getUUID());
            if (lookup != null) {
                BrainUtils.setMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get(), lookup);
                assignedUuid = lookup;
            }
        }
        if (assignedUuid == null) {
            // 2. Otherwise this is a manually-spawned wendigo (spawn egg / /summon / creative).
            // Auto-assign to the nearest survival player within follow range.
            Player nearest = findNearestSurvivalPlayer(level, entity);
            if (nearest != null) {
                BrainUtils.setMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get(), nearest.getUUID());
                assignedUuid = nearest.getUUID();
            }
        }
        if (assignedUuid == null) return;

        Player player = level.getPlayerByUUID(assignedUuid);
        if (player == null || !player.isAlive() || player.isSpectator() || player.isCreative()) {
            BrainUtils.clearMemory(entity, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
            BrainUtils.clearMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE);
            BrainUtils.clearMemory(entity, MemoryModuleType.ATTACK_TARGET);
            return;
        }

        BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_VISIBLE_PLAYER, player);
        BrainUtils.setMemory(entity, ModMemoryModuleTypes.LAST_KNOWN_PLAYER_POS.get(), BlockPos.containing(player.position()));
        if (entity.canAttack(player)) {
            BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, player);
        }
    }

    private static Player findNearestSurvivalPlayer(ServerLevel level, WendigoEntity entity) {
        double range = entity.getAttributeValue(Attributes.FOLLOW_RANGE);
        double bestDistSq = range * range;
        Player best = null;
        for (Player p : level.players()) {
            if (p.isSpectator() || p.isCreative()) continue;
            if (!p.isAlive()) continue;
            double d = entity.distanceToSqr(p);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = p;
            }
        }
        return best;
    }
}
