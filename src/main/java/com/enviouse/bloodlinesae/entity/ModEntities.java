package com.enviouse.bloodlinesae.entity;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Bloodlinesae.MODID);

    // Parent bbox is a thin column (1 wide x 5 tall) — just enough for block collision and pathfinding
    // through forests. Hit detection happens entirely on the limb PartEntities, so the wide model
    // doesn't need a wide collision footprint.
    public static final RegistryObject<EntityType<WendigoEntity>> WENDIGO = ENTITY_TYPES.register(
            "wendigo",
            () -> EntityType.Builder.<WendigoEntity>of(WendigoEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 5.0F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(Bloodlinesae.MODID, "wendigo").toString())
    );

    public static final RegistryObject<Item> WENDIGO_SPAWN_EGG = Bloodlinesae.ITEMS.register(
            "wendigo_spawn_egg",
            () -> new ForgeSpawnEggItem(WENDIGO, 0x3A2A1F, 0xB02828, new Item.Properties())
    );
}
