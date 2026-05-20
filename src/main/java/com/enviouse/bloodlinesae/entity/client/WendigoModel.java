package com.enviouse.bloodlinesae.entity.client;

import com.enviouse.bloodlinesae.Bloodlinesae;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class WendigoModel extends GeoModel<WendigoEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(Bloodlinesae.MODID, "geo/wendigo.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Bloodlinesae.MODID, "textures/entity/wendigo.png");
    private static final ResourceLocation ANIMATIONS =
            new ResourceLocation(Bloodlinesae.MODID, "animations/wendigo.animation.json");

    @Override
    public ResourceLocation getModelResource(WendigoEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WendigoEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WendigoEntity entity) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(WendigoEntity animatable, long instanceId, AnimationState<WendigoEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        // Enable world-space matrix tracking so the renderer caches the world position of these bones.
        // Read by WendigoRenderer after the render pass and fed back to position the limb hitboxes.
        enableTracking(WendigoEntity.HEAD_BONE);
        enableTracking(WendigoEntity.TORSO_BONE);
        enableTracking(WendigoEntity.LEFT_ARM_BONE);
        enableTracking(WendigoEntity.RIGHT_ARM_BONE);
        enableTracking(WendigoEntity.LEFT_LEG_BONE);
        enableTracking(WendigoEntity.RIGHT_LEG_BONE);
    }

    private void enableTracking(String boneName) {
        getBone(boneName).ifPresent(GeoBone::getWorldSpaceMatrix);
    }
}
