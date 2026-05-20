package com.enviouse.bloodlinesae.entity.client;

import com.enviouse.bloodlinesae.client.WendigoClientConfig;
import com.enviouse.bloodlinesae.entity.custom.WendigoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WendigoRenderer extends GeoEntityRenderer<WendigoEntity> {

    public WendigoRenderer(EntityRendererProvider.Context context) {
        super(context, new WendigoModel());
        this.shadowRadius = 0.6F;
    }

    @Override
    public RenderType getRenderType(WendigoEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isStalking()) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, WendigoEntity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                               float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        if (animatable.isStalking() && !isReRender) {
            float shimmer = WendigoClientConfig.CLIENT.stalkingShimmerAlpha.get().floatValue();
            alpha *= shimmer;
            // Tiny vertical wobble for shimmer feel.
            float wobble = (float) Math.sin((animatable.tickCount + partialTick) * 0.1f) * 0.02f;
            poseStack.pushPose();
            poseStack.translate(0, wobble, 0);
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender,
                    partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }

        if (!isReRender) {
            captureBonePosition(animatable, WendigoEntity.HEAD_BONE,      animatable::setHeadBonePos);
            captureBonePosition(animatable, WendigoEntity.TORSO_BONE,     animatable::setTorsoBonePos);
            captureBonePosition(animatable, WendigoEntity.LEFT_ARM_BONE,  animatable::setLeftArmBonePos);
            captureBonePosition(animatable, WendigoEntity.RIGHT_ARM_BONE, animatable::setRightArmBonePos);
            captureBonePosition(animatable, WendigoEntity.LEFT_LEG_BONE,  animatable::setLeftLegBonePos);
            captureBonePosition(animatable, WendigoEntity.RIGHT_LEG_BONE, animatable::setRightLegBonePos);
        }
    }

    private void captureBonePosition(WendigoEntity entity, String boneName, java.util.function.Consumer<Vec3> setter) {
        this.model.getBone(boneName).ifPresent(bone -> {
            Vector3d pos = bone.getWorldPosition();
            setter.accept(new Vec3(pos.x, pos.y, pos.z));
        });
    }
}
