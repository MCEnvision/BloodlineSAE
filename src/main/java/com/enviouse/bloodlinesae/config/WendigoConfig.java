package com.enviouse.bloodlinesae.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class WendigoConfig {

    private WendigoConfig() {}

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
    }

    public static final class Server {
        public final ForgeConfigSpec.DoubleValue maxHealth;
        public final ForgeConfigSpec.DoubleValue biteDamage;
        public final ForgeConfigSpec.DoubleValue chargeDamage;
        public final ForgeConfigSpec.DoubleValue movementSpeed;
        public final ForgeConfigSpec.DoubleValue followRange;

        public final ForgeConfigSpec.DoubleValue baseSniffRate;
        public final ForgeConfigSpec.DoubleValue meatSniffBonus;
        public final ForgeConfigSpec.DoubleValue centerProximityMaxBonus;
        public final ForgeConfigSpec.DoubleValue sneakSniffReduction;
        public final ForgeConfigSpec.DoubleValue biomeRadius;
        public final ForgeConfigSpec.DoubleValue maxDetectionDistance;
        public final ForgeConfigSpec.IntValue loseScentTimeoutTicks;
        public final ForgeConfigSpec.DoubleValue loseScentDistance;
        public final ForgeConfigSpec.DoubleValue revealThreshold;
        public final ForgeConfigSpec.DoubleValue startStalkingThreshold;
        public final ForgeConfigSpec.DoubleValue returnToDormantThreshold;
        public final ForgeConfigSpec.DoubleValue scentDecayRate;

        public final ForgeConfigSpec.IntValue paralysisDurationTicks;
        public final ForgeConfigSpec.IntValue roarCooldownTicks;
        public final ForgeConfigSpec.DoubleValue roarTriggerRange;
        public final ForgeConfigSpec.DoubleValue paralysisApplyRange;
        public final ForgeConfigSpec.IntValue roarWindupTicks;

        public final ForgeConfigSpec.IntValue chargeWindupTicks;
        public final ForgeConfigSpec.IntValue chargeDurationTicks;
        public final ForgeConfigSpec.DoubleValue chargeSpeed;
        public final ForgeConfigSpec.IntValue chargeCooldownTicks;
        public final ForgeConfigSpec.DoubleValue chargeMinRange;
        public final ForgeConfigSpec.DoubleValue chargeMaxRange;
        public final ForgeConfigSpec.IntValue wallHitStunTicks;

        public final ForgeConfigSpec.IntValue biteWindupTicks;
        public final ForgeConfigSpec.IntValue biteCooldownTicks;
        public final ForgeConfigSpec.DoubleValue biteRange;

        public final ForgeConfigSpec.IntValue respawnDelayMinutes;
        public final ForgeConfigSpec.IntValue maxWendigosPerPlayer;

        public final ForgeConfigSpec.DoubleValue assScratchChancePerTick;

        public final ForgeConfigSpec.BooleanValue lifecycleEnabled;
        public final ForgeConfigSpec.BooleanValue lifecycleDebugForceAll;

        public final ForgeConfigSpec.IntValue biomeCenterX;
        public final ForgeConfigSpec.IntValue biomeCenterZ;
        public final ForgeConfigSpec.IntValue biomeForcedRadius;

        Server(ForgeConfigSpec.Builder b) {
            b.push("wendigo");
            maxHealth        = b.comment("Maximum HP").defineInRange("maxHealth", 500.0, 100.0, 2000.0);
            biteDamage       = b.comment("Bite raw damage").defineInRange("biteDamage", 25.0, 5.0, 100.0);
            chargeDamage     = b.comment("Charge raw damage").defineInRange("chargeDamage", 35.0, 5.0, 100.0);
            movementSpeed    = b.comment("Movement speed attribute").defineInRange("movementSpeed", 0.35, 0.10, 1.0);
            followRange      = b.comment("Follow range").defineInRange("followRange", 64.0, 16.0, 128.0);

            b.push("detection");
            baseSniffRate            = b.defineInRange("baseSniffRate", 0.001, 0.0, 1.0);
            meatSniffBonus           = b.defineInRange("meatSniffBonus", 0.003, 0.0, 1.0);
            centerProximityMaxBonus  = b.defineInRange("centerProximityMaxBonus", 0.002, 0.0, 1.0);
            sneakSniffReduction      = b.defineInRange("sneakSniffReduction", 0.0005, 0.0, 1.0);
            biomeRadius              = b.defineInRange("biomeRadius", 2000.0, 1.0, 100000.0);
            maxDetectionDistance     = b.defineInRange("maxDetectionDistance", 64.0, 16.0, 256.0);
            loseScentTimeoutTicks    = b.defineInRange("loseScentTimeoutTicks", 200, 20, 24000);
            loseScentDistance        = b.defineInRange("loseScentDistance", 32.0, 4.0, 256.0);
            revealThreshold          = b.defineInRange("revealThreshold", 1.0, 0.0, 1.0);
            startStalkingThreshold   = b.defineInRange("startStalkingThreshold", 0.1, 0.0, 1.0);
            returnToDormantThreshold = b.defineInRange("returnToDormantThreshold", 0.3, 0.0, 1.0);
            scentDecayRate           = b.defineInRange("scentDecayRate", 0.0005, 0.0, 1.0);
            b.pop();

            b.push("roar");
            paralysisDurationTicks = b.defineInRange("paralysisDurationTicks", 600, 20, 24000);
            roarCooldownTicks      = b.defineInRange("roarCooldownTicks", 700, 20, 24000);
            roarTriggerRange       = b.defineInRange("roarTriggerRange", 16.0, 1.0, 64.0);
            paralysisApplyRange    = b.defineInRange("paralysisApplyRange", 10.0, 1.0, 64.0);
            roarWindupTicks        = b.defineInRange("roarWindupTicks", 12, 0, 200);
            b.pop();

            b.push("charge");
            chargeWindupTicks   = b.defineInRange("chargeWindupTicks", 20, 0, 200);
            chargeDurationTicks = b.defineInRange("chargeDurationTicks", 30, 1, 200);
            chargeSpeed         = b.defineInRange("chargeSpeed", 0.85, 0.1, 5.0);
            chargeCooldownTicks = b.defineInRange("chargeCooldownTicks", 200, 20, 6000);
            chargeMinRange      = b.defineInRange("chargeMinRange", 6.0, 1.0, 32.0);
            chargeMaxRange      = b.defineInRange("chargeMaxRange", 20.0, 1.0, 64.0);
            wallHitStunTicks    = b.defineInRange("wallHitStunTicks", 40, 0, 600);
            b.pop();

            b.push("bite");
            biteWindupTicks   = b.defineInRange("biteWindupTicks", 8, 0, 200);
            biteCooldownTicks = b.defineInRange("biteCooldownTicks", 30, 1, 600);
            biteRange         = b.defineInRange("biteRange", 3.0, 0.5, 16.0);
            b.pop();

            b.push("respawn");
            respawnDelayMinutes    = b.defineInRange("respawnDelayMinutes", 5, 0, 720);
            maxWendigosPerPlayer   = b.defineInRange("maxWendigosPerPlayer", 1, 0, 10);
            b.pop();

            b.push("easterEgg");
            assScratchChancePerTick = b.defineInRange("assScratchChancePerTick", 0.0001, 0.0, 1.0);
            b.pop();

            b.push("lifecycle");
            lifecycleEnabled       = b.comment("Whether player-bound wendigo spawn lifecycle is active. Disable during isolated entity development.")
                                       .define("lifecycleEnabled", true);
            lifecycleDebugForceAll = b.comment("Debug: pretend every overworld dim is bloodline forest. Test-only.")
                                       .define("lifecycleDebugForceAll", false);
            b.pop();

            b.push("placement");
            biomeCenterX      = b.comment("X coordinate of the bloodline forest center.")
                                  .defineInRange("biomeCenterX", 0, -30_000_000, 30_000_000);
            biomeCenterZ      = b.comment("Z coordinate of the bloodline forest center.")
                                  .defineInRange("biomeCenterZ", 0, -30_000_000, 30_000_000);
            biomeForcedRadius = b.comment("Block radius around the center where the biome is force-applied. 2000 = a 4000-block-diameter circle.")
                                  .defineInRange("biomeForcedRadius", 2000, 100, 20000);
            b.pop();

            b.pop();
        }
    }
}
