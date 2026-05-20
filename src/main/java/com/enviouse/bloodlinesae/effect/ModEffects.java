package com.enviouse.bloodlinesae.effect;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    private ModEffects() {}

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Bloodlinesae.MODID);

    public static final RegistryObject<MobEffect> PARALYSIS =
            EFFECTS.register("paralysis", ParalysisEffect::new);
}
