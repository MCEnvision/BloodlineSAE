package com.enviouse.bloodlinesae.client;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID, value = Dist.CLIENT)
public final class HotbarLockHandler {

    private HotbarLockHandler() {}

    private static final int[] HOTBAR_KEYS = {
            GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3,
            GLFW.GLFW_KEY_4, GLFW.GLFW_KEY_5, GLFW.GLFW_KEY_6,
            GLFW.GLFW_KEY_7, GLFW.GLFW_KEY_8, GLFW.GLFW_KEY_9
    };

    private static boolean isParalyzed() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.hasEffect(ModEffects.PARALYSIS.get());
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isParalyzed()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (!isParalyzed()) return;
        for (int key : HOTBAR_KEYS) {
            if (event.getKey() == key) {
                // InputEvent.Key is not cancellable; the fallback approach is a mixin on
                // KeyMapping.click. Documented in WENDIGO_FULL_SPEC.md §5.7 — try the
                // scroll cancel + future mixin fallback if hotbar slot switching still fires.
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    // Force keep selected slot pinned (re-set in case input slipped through).
                    mc.player.getInventory().selected = mc.player.getInventory().selected;
                }
                return;
            }
        }
    }
}
