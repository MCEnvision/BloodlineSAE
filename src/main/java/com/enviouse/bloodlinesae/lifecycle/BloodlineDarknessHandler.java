package com.enviouse.bloodlinesae.lifecycle;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.worldgen.ModBiomes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Distance-based atmospheric darkness applied to players inside the bloodline forest.
 * The closer to the center of the biome, the worse it gets.
 *
 *   > 1500  → no effects (just visual fog from the client fog handler)
 *   <= 1500 → BLINDNESS I starts fading in (mild edge-of-vision haze)
 *   <= 1000 → BLINDNESS II + DARKNESS pulse begins
 *   <=  500 → full DARKNESS + persistent BLINDNESS
 *
 * Effects are re-applied every 60 ticks so they never wear off while the player is in range,
 * but flick straight off the moment the player steps outside the biome (we use very short
 * effect durations).
 */
@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID)
public final class BloodlineDarknessHandler {

    private BloodlineDarknessHandler() {}

    private static final int REAPPLY_TICKS = 60;
    private static final int EFFECT_LIFETIME_TICKS = 100;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.player.tickCount % REAPPLY_TICKS != 0) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (!player.level().getBiome(player.blockPosition()).is(ModBiomes.IS_BLOODLINE_FOREST)) return;

        int cx = WendigoConfig.SERVER.biomeCenterX.get();
        int cz = WendigoConfig.SERVER.biomeCenterZ.get();
        double dx = player.getX() - cx;
        double dz = player.getZ() - cz;
        double dist = Math.sqrt(dx * dx + dz * dz);

        // Inside 500: strong darkness + heavy blindness.
        if (dist <= 500) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, EFFECT_LIFETIME_TICKS, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_LIFETIME_TICKS, 1, false, false, false));
            return;
        }

        // Inside 1000: pulsing darkness + mild blindness.
        if (dist <= 1000) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, EFFECT_LIFETIME_TICKS, 0, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_LIFETIME_TICKS, 0, false, false, false));
            return;
        }

        // 1000 → 1500: mild blindness only — edge of vision haze.
        if (dist <= 1500) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, EFFECT_LIFETIME_TICKS, 0, false, false, false));
        }
    }
}
