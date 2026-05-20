package com.enviouse.bloodlinesae;

import com.enviouse.bloodlinesae.block.ModBlocks;
import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.ModSensorTypes;
import com.enviouse.bloodlinesae.client.WendigoClientConfig;
import com.enviouse.bloodlinesae.command.WendigoAdminCommand;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.effect.ModEffects;
import com.enviouse.bloodlinesae.entity.ModEntities;
import com.enviouse.bloodlinesae.entity.client.WendigoRenderer;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.enviouse.bloodlinesae.item.ModItems;
import com.enviouse.bloodlinesae.particle.BloodlineDriftParticle;
import com.enviouse.bloodlinesae.particle.ModParticles;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.enviouse.bloodlinesae.worldgen.BloodlineRegion;
import com.enviouse.bloodlinesae.worldgen.BloodlineSurfaceRules;
import com.enviouse.bloodlinesae.worldgen.feature.ModFeatures;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@Mod(Bloodlinesae.MODID)
public class Bloodlinesae {

    public static final String MODID = "bloodlinesae";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> WENDIGO_TAB = CREATIVE_MODE_TABS.register("wendigo_tab",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .title(net.minecraft.network.chat.Component.literal("Bloodline"))
                    .icon(() -> ModEntities.WENDIGO_SPAWN_EGG.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModEntities.WENDIGO_SPAWN_EGG.get());
                        output.accept(ModItems.BONE_FRAGMENT.get());
                        output.accept(ModItems.WENDIGO_SKULL.get());
                        output.accept(ModBlocks.BLOODLINE_LOG.get());
                        output.accept(ModBlocks.STRIPPED_BLOODLINE_LOG.get());
                        output.accept(ModBlocks.BLOODLINE_PLANKS.get());
                        output.accept(ModBlocks.BLOODLINE_LEAVES.get());
                        output.accept(ModBlocks.BLOODLINE_SAPLING.get());
                        output.accept(ModBlocks.BLOODLINE_GRASS_BLOCK.get());
                        output.accept(ModBlocks.BLOODLINE_DIRT.get());
                        output.accept(ModBlocks.CRIMSTONE.get());
                        output.accept(ModBlocks.BONE_PILE.get());
                        output.accept(ModBlocks.BLOODLINE_LOG_OFFICIAL.get());
                        output.accept(ModBlocks.BLOODLINE_DIRT_OFFICIAL.get());
                        output.accept(ModBlocks.BLOODLINE_GRASS_BLOCK_OFFICIAL.get());
                        output.accept(ModBlocks.CRIMSTONE_OFFICIAL.get());
                        output.accept(ModBlocks.BLOODLINE_MUD.get());
                        output.accept(ModBlocks.BLOODLINE_MUD2.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_XL.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_LARGE.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_THICK.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_PRIMARY.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_MEDIUM.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_SECONDARY.get());
                        output.accept(ModBlocks.BLOODLINE_BRANCH_TWIG.get());
                    })
                    .build());

    public Bloodlinesae() {
        // Class-load triggers for static RegistryObjects in helper classes.
        ModItems.touch();
        ModBlocks.touch();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onAttributeCreation);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMemoryModuleTypes.MEMORIES.register(modEventBus);
        ModSensorTypes.SENSORS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WendigoConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, WendigoClientConfig.CLIENT_SPEC);

        GeckoLib.initialize();
    }

    private void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.WENDIGO.get(), WendigoEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WendigoAdminCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // TerraBlender region registration must happen on the main thread after registries are built.
            Regions.register(new BloodlineRegion(
                    new ResourceLocation(MODID, "overworld"),
                    4));
            // Paint our biome's surface (official grass on top, official dirt under).
            SurfaceRuleManager.addSurfaceRules(
                    SurfaceRuleManager.RuleCategory.OVERWORLD,
                    MODID,
                    BloodlineSurfaceRules.makeRules());
            LOGGER.info("[Bloodline] worldgen wired (region + surface rules)");
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModEntities.WENDIGO_SPAWN_EGG);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[Bloodline] server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.WENDIGO.get(), WendigoRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.BLOODLINE_DRIFT.get(), BloodlineDriftParticle.Provider::new);
        }
    }
}
