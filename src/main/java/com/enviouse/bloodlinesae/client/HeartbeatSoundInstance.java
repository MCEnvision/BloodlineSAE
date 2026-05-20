package com.enviouse.bloodlinesae.client;

import com.enviouse.bloodlinesae.effect.ModEffects;
import com.enviouse.bloodlinesae.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

public class HeartbeatSoundInstance extends AbstractTickableSoundInstance {

    public HeartbeatSoundInstance() {
        super(ModSounds.WENDIGO_HEARTBEAT.get(), SoundSource.AMBIENT, net.minecraft.util.RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = (float) WendigoClientConfig.CLIENT.heartbeatVolume.get().doubleValue();
        this.pitch = 1.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(ModEffects.PARALYSIS.get())) {
            this.stop();
            return;
        }
        // Volume can be re-read live in case config changes.
        this.volume = (float) WendigoClientConfig.CLIENT.heartbeatVolume.get().doubleValue();
    }
}
