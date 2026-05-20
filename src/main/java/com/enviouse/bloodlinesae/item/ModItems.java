package com.enviouse.bloodlinesae.item;

import com.enviouse.bloodlinesae.Bloodlinesae;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    private ModItems() {}

    public static final RegistryObject<Item> BONE_FRAGMENT = Bloodlinesae.ITEMS.register(
            "bone_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> WENDIGO_SKULL = Bloodlinesae.ITEMS.register(
            "wendigo_skull",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static void touch() {
        // class-load hook so DeferredRegister entries register through Bloodlinesae.ITEMS.
    }
}
