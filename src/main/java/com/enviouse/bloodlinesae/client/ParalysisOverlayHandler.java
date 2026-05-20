package com.enviouse.bloodlinesae.client;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.effect.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID, value = Dist.CLIENT)
public final class ParalysisOverlayHandler {

    private ParalysisOverlayHandler() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.hasEffect(ModEffects.PARALYSIS.get())) return;

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        GuiGraphics gfx = event.getGuiGraphics();

        int jitter = 0;
        if (WendigoClientConfig.CLIENT.paralysisJitter.get()) {
            // 4Hz vertical 1-pixel shake.
            jitter = (int) (Math.sin(System.currentTimeMillis() / 62.5) > 0 ? 1 : 0);
        }

        renderRedVignette(gfx, width, height, jitter);
        renderHotbarTint(gfx, width, height, jitter);
    }

    private static void renderRedVignette(GuiGraphics gfx, int width, int height, int jitter) {
        double intensityScale = WendigoClientConfig.CLIENT.paralysisVignetteIntensity.get();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float t = (System.currentTimeMillis() % 1000L) / 1000f;
        float intensity = 0.55f + 0.30f * (float) Math.abs(Math.sin(t * Math.PI));
        int alpha = Math.min(255, Math.max(0, (int) (intensity * 180f * intensityScale)));
        int color = (alpha << 24) | 0x4A0000;
        gfx.fill(0, jitter, width, height + jitter, color);
        RenderSystem.disableBlend();
    }

    private static void renderHotbarTint(GuiGraphics gfx, int width, int height, int jitter) {
        int hotbarWidth = 182;
        int hotbarHeight = 22;
        int x = (width - hotbarWidth) / 2;
        int y = height - hotbarHeight + jitter;
        int color = WendigoClientConfig.CLIENT.hotbarTintColor.get();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gfx.fill(x, y, x + hotbarWidth, y + hotbarHeight, color);
        RenderSystem.disableBlend();
    }
}
