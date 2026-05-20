package com.enviouse.bloodlinesae.brain.sensor;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.ModSensorTypes;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.UUID;

public class MeatInInventorySensor<E extends WendigoEntity> extends ExtendedSensor<E> {

    public static final TagKey<Item> WENDIGO_ATTRACTS_TAG =
            TagKey.create(Registries.ITEM, new ResourceLocation(Bloodlinesae.MODID, "wendigo_attracts"));

    private static final List<MemoryModuleType<?>> MEMORIES =
            List.of(ModMemoryModuleTypes.ASSIGNED_PLAYER_HAS_MEAT.get());

    private static final int MAX_NESTED_SHULKER_DEPTH = 3;

    public MeatInInventorySensor() {
        setScanRate(e -> 20);
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensorTypes.MEAT_IN_INVENTORY.get();
    }

    @Override
    protected void doTick(ServerLevel level, E entity) {
        UUID assignedUuid = BrainUtils.getMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        if (assignedUuid == null) return;
        Player player = level.getPlayerByUUID(assignedUuid);
        if (player == null) return;

        boolean hasMeat = playerHasMeat(player);
        BrainUtils.setMemory(entity, ModMemoryModuleTypes.ASSIGNED_PLAYER_HAS_MEAT.get(), hasMeat);
    }

    private boolean playerHasMeat(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (containsMeat(stack, 0)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (containsMeat(stack, 0)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (containsMeat(stack, 0)) return true;
        }
        return false;
    }

    private boolean containsMeat(ItemStack stack, int depth) {
        if (stack.isEmpty()) return false;
        if (stack.is(WENDIGO_ATTRACTS_TAG)) return true;
        if (depth >= MAX_NESTED_SHULKER_DEPTH) return false;

        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
            CompoundTag tag = BlockItem.getBlockEntityData(stack);
            if (tag != null && tag.contains("Items", Tag.TAG_LIST)) {
                NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
                ContainerHelper.loadAllItems(tag, contents);
                for (ItemStack inner : contents) {
                    if (containsMeat(inner, depth + 1)) return true;
                }
            }
        }
        return false;
    }
}
