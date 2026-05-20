package com.enviouse.bloodlinesae.brain;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;
import java.util.UUID;

public final class ModMemoryModuleTypes {

    private ModMemoryModuleTypes() {}

    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, Bloodlinesae.MODID);

    public static final RegistryObject<MemoryModuleType<WendigoState>> WENDIGO_STATE =
            MEMORIES.register("wendigo_state",
                    () -> new MemoryModuleType<>(Optional.of(WendigoState.CODEC)));

    public static final RegistryObject<MemoryModuleType<Float>> DETECTION_LEVEL =
            MEMORIES.register("detection_level",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<UUID>> ASSIGNED_PLAYER =
            MEMORIES.register("assigned_player",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<BlockPos>> LAST_KNOWN_PLAYER_POS =
            MEMORIES.register("last_known_player_pos",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> ASSIGNED_PLAYER_HAS_MEAT =
            MEMORIES.register("assigned_player_has_meat",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Float>> DISTANCE_TO_CENTER =
            MEMORIES.register("distance_to_center",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Boolean>> ASSIGNED_PLAYER_SNEAKING =
            MEMORIES.register("assigned_player_sneaking",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> ROAR_COOLDOWN_TICKS =
            MEMORIES.register("roar_cooldown_ticks",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> CHARGE_COOLDOWN_TICKS =
            MEMORIES.register("charge_cooldown_ticks",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> STUNNED_TICKS_REMAINING =
            MEMORIES.register("stunned_ticks_remaining",
                    () -> new MemoryModuleType<>(Optional.empty()));

    public static final RegistryObject<MemoryModuleType<Integer>> TICKS_SINCE_TARGET_SEEN =
            MEMORIES.register("ticks_since_target_seen",
                    () -> new MemoryModuleType<>(Optional.empty()));
}
