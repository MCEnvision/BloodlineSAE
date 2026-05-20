package com.enviouse.bloodlinesae.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.entity.PartEntity;

public class WendigoPart extends PartEntity<WendigoEntity> {

    public final String partName;
    private final EntityDimensions size;

    public WendigoPart(WendigoEntity parent, String name, float width, float height) {
        super(parent);
        this.partName = name;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.getParent().hurt(this, source, amount);
    }

    @Override
    public boolean is(Entity other) {
        return this == other || this.getParent() == other;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }
}
