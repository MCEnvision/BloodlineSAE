package com.enviouse.bloodlinesae.client;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class WendigoClientConfig {

    private WendigoClientConfig() {}

    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> p = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = p.getLeft();
        CLIENT_SPEC = p.getRight();
    }

    public static final class Client {
        public final ForgeConfigSpec.DoubleValue stalkingShimmerAlpha;
        public final ForgeConfigSpec.DoubleValue paralysisVignetteIntensity;
        public final ForgeConfigSpec.DoubleValue heartbeatVolume;
        public final ForgeConfigSpec.IntValue hotbarTintColor;
        public final ForgeConfigSpec.BooleanValue paralysisJitter;

        Client(ForgeConfigSpec.Builder b) {
            b.push("wendigo");
            b.push("visuals");
            stalkingShimmerAlpha       = b.defineInRange("stalkingShimmerAlpha", 0.25, 0.0, 1.0);
            paralysisVignetteIntensity = b.defineInRange("paralysisVignetteIntensity", 1.0, 0.0, 2.0);
            heartbeatVolume            = b.defineInRange("heartbeatVolume", 1.0, 0.0, 1.0);
            hotbarTintColor            = b.comment("ARGB tint over the hotbar during paralysis")
                                          .defineInRange("hotbarTintColor", 0xAA4400AA, Integer.MIN_VALUE, Integer.MAX_VALUE);
            paralysisJitter            = b.comment("Enable subtle 1-pixel screen jitter while paralyzed")
                                          .define("paralysisJitter", true);
            b.pop();
            b.pop();
        }
    }
}
