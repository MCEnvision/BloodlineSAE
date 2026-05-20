package com.enviouse.bloodlinesae.sound;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Bloodlinesae.MODID);

    public static final RegistryObject<SoundEvent> WENDIGO_BREATHING   = register("wendigo.breathing");
    public static final RegistryObject<SoundEvent> WENDIGO_FOOTSTEP    = register("wendigo.footstep");
    public static final RegistryObject<SoundEvent> WENDIGO_SNIFF       = register("wendigo.sniff");
    public static final RegistryObject<SoundEvent> WENDIGO_GROWL_LOW   = register("wendigo.growl_low");
    public static final RegistryObject<SoundEvent> WENDIGO_ROAR        = register("wendigo.roar");
    public static final RegistryObject<SoundEvent> WENDIGO_SCREAM_REVEAL = register("wendigo.scream_reveal");
    public static final RegistryObject<SoundEvent> WENDIGO_CHARGE_WARN  = register("wendigo.charge_warn");
    public static final RegistryObject<SoundEvent> WENDIGO_CHARGE_IMPACT = register("wendigo.charge_impact");
    public static final RegistryObject<SoundEvent> WENDIGO_BITE_ATTACK = register("wendigo.bite_attack");
    public static final RegistryObject<SoundEvent> WENDIGO_BITE_HIT    = register("wendigo.bite_hit");
    public static final RegistryObject<SoundEvent> WENDIGO_DEATH       = register("wendigo.death");
    public static final RegistryObject<SoundEvent> WENDIGO_DEATH_GLOBAL = register("wendigo.death_global");
    public static final RegistryObject<SoundEvent> WENDIGO_HURT        = register("wendigo.hurt");
    public static final RegistryObject<SoundEvent> WENDIGO_HEARTBEAT   = register("wendigo.heartbeat");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(Bloodlinesae.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
