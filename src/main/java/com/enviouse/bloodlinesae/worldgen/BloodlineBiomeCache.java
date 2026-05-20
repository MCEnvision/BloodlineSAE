package com.enviouse.bloodlinesae.worldgen;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID)
public final class BloodlineBiomeCache {

    private BloodlineBiomeCache() {}

    @Nullable
    public static volatile Holder<Biome> HOLDER;

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        RegistryAccess access = event.getServer().registryAccess();
        HOLDER = access.registryOrThrow(Registries.BIOME)
                .getHolder(ModBiomes.BLOODLINE_FOREST)
                .orElse(null);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        HOLDER = null;
    }
}
