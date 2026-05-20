package com.enviouse.bloodlinesae.lifecycle;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.entity.ModEntities;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.worldgen.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID)
public final class WendigoLifecycleHandler {

    private WendigoLifecycleHandler() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!WendigoConfig.SERVER.lifecycleEnabled.get()) return;
        if (!isInBloodlineForest(player)) return;
        spawnWendigoForPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        despawnWendigoForPlayer(player);
    }

    @SubscribeEvent
    public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Despawn whatever wendigo was tracking the player; a fresh one spawns on next tick
        // if the player is still in bloodline forest in the new dimension.
        despawnWendigoForPlayer(player);
    }

    @SubscribeEvent
    public static void onEntityChangeDim(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof WendigoEntity) {
            // Wendigo itself can't switch dimensions (canChangeDimensions returns false),
            // but block any sneaky travel attempt anyway.
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.player.tickCount % 20 != 0) return;
        if (!WendigoConfig.SERVER.lifecycleEnabled.get()) return;

        ServerLevel level = player.serverLevel();
        WendigoAssignmentData data = WendigoAssignmentData.get(level);
        boolean inBiome = isInBloodlineForest(player);
        boolean hasWendigo = data.getWendigoFor(player.getUUID()).isPresent();

        if (inBiome && !hasWendigo) {
            if (data.shouldRespawn(player.getUUID(), level.getGameTime())) {
                data.clearRespawn(player.getUUID());
                spawnWendigoForPlayer(player);
            } else if (!data.shouldRespawn(player.getUUID(), Long.MAX_VALUE)) {
                spawnWendigoForPlayer(player);
            }
        } else if (!inBiome && hasWendigo) {
            despawnWendigoForPlayer(player);
        }
    }

    private static boolean isInBloodlineForest(Player player) {
        if (WendigoConfig.SERVER.lifecycleDebugForceAll.get()) return true;
        return player.level().getBiome(player.blockPosition()).is(ModBiomes.IS_BLOODLINE_FOREST);
    }

    public static void spawnWendigoForPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WendigoAssignmentData data = WendigoAssignmentData.get(level);
        if (data.getWendigoFor(player.getUUID()).isPresent()) return;

        BlockPos spawnPos = findSpawnLocationNearPlayer(level, player);
        if (spawnPos == null) return;

        WendigoEntity wendigo = ModEntities.WENDIGO.get().create(level);
        if (wendigo == null) return;
        wendigo.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        BrainUtils.setMemory(wendigo, ModMemoryModuleTypes.ASSIGNED_PLAYER.get(), player.getUUID());
        level.addFreshEntity(wendigo);

        data.assign(player.getUUID(), wendigo.getUUID());
    }

    public static void despawnWendigoForPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WendigoAssignmentData data = WendigoAssignmentData.get(level);
        Optional<UUID> wendigoUuid = data.getWendigoFor(player.getUUID());
        if (wendigoUuid.isEmpty()) return;

        Entity wendigo = level.getEntity(wendigoUuid.get());
        if (wendigo instanceof WendigoEntity we) {
            we.discard();
        }
        data.unassign(player.getUUID());
    }

    private static BlockPos findSpawnLocationNearPlayer(ServerLevel level, ServerPlayer player) {
        RandomSource random = level.getRandom();
        BlockPos playerPos = player.blockPosition();
        for (int attempt = 0; attempt < 20; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 40 + random.nextDouble() * 20;
            int dx = (int) (Math.cos(angle) * dist);
            int dz = (int) (Math.sin(angle) * dist);
            BlockPos candidate = playerPos.offset(dx, 0, dz);
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, candidate.getX(), candidate.getZ());
            BlockPos surface = new BlockPos(candidate.getX(), surfaceY, candidate.getZ());
            if (isValidSpawn(level, surface)) {
                return surface;
            }
        }
        return null;
    }

    private static boolean isValidSpawn(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) return false;
        if (!level.getBlockState(pos.above()).isAir()) return false;
        if (!level.getBlockState(pos.below()).isSolid()) return false;
        return true;
    }
}
