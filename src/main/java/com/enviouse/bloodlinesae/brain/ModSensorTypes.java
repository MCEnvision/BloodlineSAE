package com.enviouse.bloodlinesae.brain;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.brain.sensor.AssignedPlayerSensor;
import com.enviouse.bloodlinesae.brain.sensor.BiomeCenterProximitySensor;
import com.enviouse.bloodlinesae.brain.sensor.MeatInInventorySensor;
import com.enviouse.bloodlinesae.brain.sensor.PlayerSneakingSensor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSensorTypes {

    private ModSensorTypes() {}

    public static final DeferredRegister<SensorType<?>> SENSORS =
            DeferredRegister.create(Registries.SENSOR_TYPE, Bloodlinesae.MODID);

    public static final RegistryObject<SensorType<AssignedPlayerSensor<?>>> ASSIGNED_PLAYER =
            SENSORS.register("assigned_player",
                    () -> new SensorType<>(AssignedPlayerSensor::new));

    public static final RegistryObject<SensorType<MeatInInventorySensor<?>>> MEAT_IN_INVENTORY =
            SENSORS.register("meat_in_inventory",
                    () -> new SensorType<>(MeatInInventorySensor::new));

    public static final RegistryObject<SensorType<BiomeCenterProximitySensor<?>>> BIOME_CENTER_PROXIMITY =
            SENSORS.register("biome_center_proximity",
                    () -> new SensorType<>(BiomeCenterProximitySensor::new));

    public static final RegistryObject<SensorType<PlayerSneakingSensor<?>>> PLAYER_SNEAKING =
            SENSORS.register("player_sneaking",
                    () -> new SensorType<>(PlayerSneakingSensor::new));
}
