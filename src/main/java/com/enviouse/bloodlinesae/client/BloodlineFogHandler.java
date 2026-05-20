package com.enviouse.bloodlinesae.client;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.worldgen.ModBiomes;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID, value = Dist.CLIENT)
public final class BloodlineFogHandler {

    private BloodlineFogHandler() {}

    // Fog distance bounds (block-space). At the biome edge fog is thin; at center it's a wall.
    private static final float EDGE_FAR     = 220.0F;  // very thin at outer edge — basically vanilla feel
    private static final float CENTER_FAR   =  14.0F;  // wall of fog right at center
    private static final float VANILLA_FAR  = 192.0F;  // approximate vanilla forest fog far plane

    // Color end-points (RGB 0..1). Edge ≈ vanilla foggy grey-red, deepens to creamy pale red at center.
    private static final float[] EDGE_COLOR   = { 0.42F, 0.18F, 0.18F };
    private static final float[] CENTER_COLOR = { 0.86F, 0.62F, 0.55F };

    /**
     * Continuous fog factor inside the biome.
     *
     *  0.0  at the biome edge (radius)
     *  1.0  at biome center
     *
     * Returns {@code -1} if the player isn't standing in the biome at all — vanilla atmosphere
     * stays in that case so the transition into the biome is itself smooth.
     */
    private static float centerNormalized() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return -1f;

        if (!player.level().getBiome(player.blockPosition()).is(ModBiomes.IS_BLOODLINE_FOREST)) return -1f;

        int cx = WendigoConfig.SERVER.biomeCenterX.get();
        int cz = WendigoConfig.SERVER.biomeCenterZ.get();
        int radius = WendigoConfig.SERVER.biomeForcedRadius.get();
        double dx = player.getX() - cx;
        double dz = player.getZ() - cz;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float t = 1.0f - (float) (dist / radius);
        return Mth.clamp(t, 0f, 1f);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float t = centerNormalized();
        if (t < 0) return;

        // ease-in toward center so fog density ramps up faster the deeper you push
        float eased = (float) Math.pow(t, 2.0);

        // Blend our far plane against the vanilla-like far plane: at the biome edge the value is
        // essentially vanilla; at center it's the wall.
        float far = Mth.lerp(eased, EDGE_FAR, CENTER_FAR);

        // Soft blend: lerp from vanilla far → our far using a low-power factor near the edge so
        // crossing the biome boundary doesn't snap.
        float edgeBlend = (float) Math.pow(t, 0.5);
        far = Mth.lerp(edgeBlend, VANILLA_FAR, far);

        event.setNearPlaneDistance(0.0F);
        event.setFarPlaneDistance(far);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float t = centerNormalized();
        if (t < 0) return;

        // Color interpolates smoothly. Near edge, t≈0, so color = EDGE_COLOR (subtle), which
        // blends with vanilla via Minecraft's own time-of-day mixing.
        float eased = (float) Math.pow(t, 1.4);
        float vanillaR = event.getRed();
        float vanillaG = event.getGreen();
        float vanillaB = event.getBlue();

        // Step 1: pick our target color along the EDGE→CENTER ramp.
        float targetR = Mth.lerp(eased, EDGE_COLOR[0], CENTER_COLOR[0]);
        float targetG = Mth.lerp(eased, EDGE_COLOR[1], CENTER_COLOR[1]);
        float targetB = Mth.lerp(eased, EDGE_COLOR[2], CENTER_COLOR[2]);

        // Step 2: blend our target with vanilla color so the boundary fades in, not snaps.
        float edgeBlend = (float) Math.pow(t, 0.5);
        float r = Mth.lerp(edgeBlend, vanillaR, targetR);
        float g = Mth.lerp(edgeBlend, vanillaG, targetG);
        float b = Mth.lerp(edgeBlend, vanillaB, targetB);

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}
