package com.enviouse.bloodlinesae.client;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bloodlinesae.MODID, value = Dist.CLIENT)
public final class ParalysisClientHandler {

    private ParalysisClientHandler() {}

    private static boolean heartbeatPlaying = false;
    // Slot snapshot taken on the tick paralysis begins; the slot is pinned to this value
    // until the effect ends. Belt-and-suspenders alongside InputEvent cancellation.
    private static int pinnedSlot = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            heartbeatPlaying = false;
            pinnedSlot = -1;
            return;
        }

        boolean paralyzed = player.hasEffect(ModEffects.PARALYSIS.get());

        if (paralyzed) {
            if (pinnedSlot < 0) {
                pinnedSlot = player.getInventory().selected;
            }
            // Pin the hotbar slot to the snapshot value every tick — clobbers slot changes
            // that slipped past InputEvent cancellation.
            if (player.getInventory().selected != pinnedSlot) {
                player.getInventory().selected = pinnedSlot;
            }
            if (!heartbeatPlaying) {
                mc.getSoundManager().play(new HeartbeatSoundInstance());
                heartbeatPlaying = true;
            }
        } else {
            if (heartbeatPlaying) {
                heartbeatPlaying = false;
            }
            pinnedSlot = -1;
        }
    }
}
