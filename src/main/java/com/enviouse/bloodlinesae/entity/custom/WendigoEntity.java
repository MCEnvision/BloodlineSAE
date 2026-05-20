package com.enviouse.bloodlinesae.entity.custom;

import com.enviouse.bloodlinesae.brain.ModMemoryModuleTypes;
import com.enviouse.bloodlinesae.brain.WendigoState;
import com.enviouse.bloodlinesae.brain.behavior.ChargeAttack;
import com.enviouse.bloodlinesae.brain.behavior.EmitStalkingSounds;
import com.enviouse.bloodlinesae.brain.behavior.MoveTowardLastKnownPlayerPos;
import com.enviouse.bloodlinesae.brain.behavior.ParalysisRoarAttack;
import com.enviouse.bloodlinesae.brain.behavior.SniffOut;
import com.enviouse.bloodlinesae.brain.behavior.WendigoBiteAttack;
import com.enviouse.bloodlinesae.brain.sensor.AssignedPlayerSensor;
import com.enviouse.bloodlinesae.brain.sensor.BiomeCenterProximitySensor;
import com.enviouse.bloodlinesae.brain.sensor.MeatInInventorySensor;
import com.enviouse.bloodlinesae.brain.sensor.PlayerSneakingSensor;
import com.enviouse.bloodlinesae.config.WendigoConfig;
import com.enviouse.bloodlinesae.lifecycle.WendigoAssignmentData;
import com.enviouse.bloodlinesae.sound.ModSounds;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class WendigoEntity extends Monster implements SmartBrainOwner<WendigoEntity>, GeoEntity {

    public static final String MAIN_CONTROLLER = "main";

    // Bone names from wendigo.geo.json — anchored at the bedrock pivots of each limb root.
    public static final String HEAD_BONE      = "upperhead";
    public static final String TORSO_BONE     = "chest";
    public static final String LEFT_ARM_BONE  = "rightarmfull";   // bedrock author labelled from observer POV;
    public static final String RIGHT_ARM_BONE = "leftarmfull";    // these end up on the opposite side once rendered.
    public static final String LEFT_LEG_BONE  = "rightupperleg";
    public static final String RIGHT_LEG_BONE = "leftupperleg";

    // Synced state visible to client renderer / animation predicates.
    private static final EntityDataAccessor<Integer> DATA_AI_STATE =
            SynchedEntityData.defineId(WendigoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STALKING =
            SynchedEntityData.defineId(WendigoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DETECTION_PERCENT =
            SynchedEntityData.defineId(WendigoEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Last-known world positions captured by WendigoRenderer after each render pass (client only).
    @Nullable private Vec3 headBonePos, torsoBonePos, leftArmBonePos, rightArmBonePos, leftLegBonePos, rightLegBonePos;

    public void setHeadBonePos(Vec3 pos)      { this.headBonePos = pos; }
    public void setTorsoBonePos(Vec3 pos)     { this.torsoBonePos = pos; }
    public void setLeftArmBonePos(Vec3 pos)   { this.leftArmBonePos = pos; }
    public void setRightArmBonePos(Vec3 pos)  { this.rightArmBonePos = pos; }
    public void setLeftLegBonePos(Vec3 pos)   { this.leftLegBonePos = pos; }
    public void setRightLegBonePos(Vec3 pos)  { this.rightLegBonePos = pos; }

    private record PartLayout(String name, float offLeft, float offUp, float offFwd,
                              float width, float height, float boneAnchorY) {}

    private static final PartLayout HEAD_LAYOUT      = new PartLayout("head",       0.00F, 4.35F,  1.50F, 1.80F, 1.30F, 0.5F);
    private static final PartLayout TORSO_LAYOUT     = new PartLayout("torso",      0.00F, 2.80F,  0.00F, 1.00F, 1.80F, 0.5F);
    private static final PartLayout LEFT_ARM_LAYOUT  = new PartLayout("left_arm",   0.90F, 2.90F,  1.10F, 0.70F, 2.60F, 1.0F);
    private static final PartLayout RIGHT_ARM_LAYOUT = new PartLayout("right_arm", -0.90F, 2.90F,  1.10F, 0.70F, 2.60F, 1.0F);
    private static final PartLayout LEFT_LEG_LAYOUT  = new PartLayout("left_leg",   0.30F, 1.35F,  0.00F, 0.70F, 2.70F, 1.0F);
    private static final PartLayout RIGHT_LEG_LAYOUT = new PartLayout("right_leg", -0.30F, 1.35F,  0.00F, 0.70F, 2.70F, 1.0F);

    public final WendigoPart head;
    public final WendigoPart torso;
    public final WendigoPart leftArm;
    public final WendigoPart rightArm;
    public final WendigoPart leftLeg;
    public final WendigoPart rightLeg;
    private final WendigoPart[] parts;
    private final PartLayout[] layouts;

    private int revealTransitionTicks = 0;
    public int lastChargeHitTick = -1000;
    private int ticksSinceLastSawTarget = 0;
    private int ticksWithoutMovement = 0;
    private Vec3 lastPosForStuckCheck = Vec3.ZERO;

    public WendigoEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.head     = makePart(HEAD_LAYOUT);
        this.torso    = makePart(TORSO_LAYOUT);
        this.leftArm  = makePart(LEFT_ARM_LAYOUT);
        this.rightArm = makePart(RIGHT_ARM_LAYOUT);
        this.leftLeg  = makePart(LEFT_LEG_LAYOUT);
        this.rightLeg = makePart(RIGHT_LEG_LAYOUT);
        this.parts   = new WendigoPart[]{head, torso, leftArm, rightArm, leftLeg, rightLeg};
        this.layouts = new PartLayout[]{HEAD_LAYOUT, TORSO_LAYOUT, LEFT_ARM_LAYOUT, RIGHT_ARM_LAYOUT, LEFT_LEG_LAYOUT, RIGHT_LEG_LAYOUT};
    }

    private WendigoPart makePart(PartLayout layout) {
        return new WendigoPart(this, layout.name, layout.width, layout.height);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_AI_STATE, WendigoState.DORMANT.ordinal());
        this.entityData.define(DATA_STALKING, false);
        this.entityData.define(DATA_DETECTION_PERCENT, 0);
    }

    @Override
    protected void registerGoals() {
        // intentional no-op: AI is handled by SmartBrainLib.
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void checkDespawn() {
        // No-op: lifecycle handler controls despawn.
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    protected boolean canRide(net.minecraft.world.entity.Entity vehicle) {
        return false;
    }

    @Override
    public int getExperienceReward() {
        return 30 + this.random.nextInt(21);
    }

    // ---------- Multipart plumbing ----------

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    public boolean hurt(WendigoPart part, DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        WendigoState state = BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        if (state == WendigoState.STUNNED) {
            amount *= 1.5f;
        }
        // If a non-assigned player attacks: ignore damage and send a chat hint.
        if (source.getEntity() instanceof Player p && !isAssignedPlayer(p)) {
            if (!this.level().isClientSide && this.tickCount % 40 == 0) {
                p.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.bloodlinesae.wendigo_uninterested")
                                .withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.ITALIC),
                        true);
            }
            return false;
        }
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            BrainUtils.setMemory(this, MemoryModuleType.HURT_BY_ENTITY, attacker);
            this.level().playSound(null, blockPosition(), ModSounds.WENDIGO_HURT.get(),
                    SoundSource.HOSTILE, 0.9F, 1.0F);
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        double yawRad = Math.toRadians(this.yBodyRot);
        double cosY = Math.cos(yawRad);
        double sinY = Math.sin(yawRad);

        positionPart(head,     headBonePos,     HEAD_LAYOUT,      cosY, sinY);
        positionPart(torso,    torsoBonePos,    TORSO_LAYOUT,     cosY, sinY);
        positionPart(leftArm,  leftArmBonePos,  LEFT_ARM_LAYOUT,  cosY, sinY);
        positionPart(rightArm, rightArmBonePos, RIGHT_ARM_LAYOUT, cosY, sinY);
        positionPart(leftLeg,  leftLegBonePos,  LEFT_LEG_LAYOUT,  cosY, sinY);
        positionPart(rightLeg, rightLegBonePos, RIGHT_LEG_LAYOUT, cosY, sinY);
    }

    private void positionPart(WendigoPart part, @Nullable Vec3 bonePos, PartLayout layout, double cosY, double sinY) {
        double anchorOffset = part.getBbHeight() * layout.boneAnchorY;
        double wx, wy, wz;
        if (bonePos != null) {
            wx = bonePos.x;
            wy = bonePos.y - anchorOffset;
            wz = bonePos.z;
        } else {
            double wDx = layout.offLeft * cosY - layout.offFwd * sinY;
            double wDz = layout.offLeft * sinY + layout.offFwd * cosY;
            wx = this.getX() + wDx;
            wy = this.getY() + layout.offUp - anchorOffset;
            wz = this.getZ() + wDz;
        }
        part.moveTo(wx, wy, wz, 0F, 0F);
    }

    // ---------- SmartBrainLib wiring ----------

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<?> brain = brainProvider().makeBrain(dynamic);
        BrainUtils.setMemory(brain, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.DORMANT);
        BrainUtils.setMemory(brain, ModMemoryModuleTypes.DETECTION_LEVEL.get(), 0f);
        return brain;
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        tickBrain(this);

        // Ensure default memories exist.
        if (BrainUtils.getMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get()) == null) {
            BrainUtils.setMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.DORMANT);
        }
        if (BrainUtils.getMemory(this, ModMemoryModuleTypes.DETECTION_LEVEL.get()) == null) {
            BrainUtils.setMemory(this, ModMemoryModuleTypes.DETECTION_LEVEL.get(), 0f);
        }

        // Reveal transition.
        if (this.revealTransitionTicks > 0) {
            this.revealTransitionTicks--;
            if (this.revealTransitionTicks == 0) {
                BrainUtils.setMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.HUNTING);
                this.setStalking(false);
                // Promote nearest visible player to attack target.
                Player nearest = BrainUtils.getMemory(this, MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                if (nearest != null) {
                    BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, nearest);
                }
            }
        }

        // Stunned tick.
        Integer stunnedRemaining = BrainUtils.getMemory(this, ModMemoryModuleTypes.STUNNED_TICKS_REMAINING.get());
        if (stunnedRemaining != null && stunnedRemaining > 0) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            int next = stunnedRemaining - 1;
            if (next <= 0) {
                BrainUtils.clearMemory(this, ModMemoryModuleTypes.STUNNED_TICKS_REMAINING.get());
                BrainUtils.setMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.HUNTING);
            } else {
                BrainUtils.setMemory(this, ModMemoryModuleTypes.STUNNED_TICKS_REMAINING.get(), next);
            }
        }

        // Decrement attack-specific cooldowns each tick (Brain memory expiry handles forgettables,
        // but we want explicit countdowns visible to behaviors that read raw ticks remaining).
        decrementCooldown(ModMemoryModuleTypes.CHARGE_COOLDOWN_TICKS.get());
        decrementCooldown(ModMemoryModuleTypes.ROAR_COOLDOWN_TICKS.get());

        // Hunting → losing_scent detection.
        WendigoState state = BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        LivingEntity target = BrainUtils.getTargetOfEntity(this);
        if (state == WendigoState.HUNTING) {
            double loseDist = WendigoConfig.SERVER.loseScentDistance.get();
            int loseTimeout = WendigoConfig.SERVER.loseScentTimeoutTicks.get();
            if (target == null || this.distanceToSqr(target) > loseDist * loseDist || !this.hasLineOfSight(target)) {
                this.ticksSinceLastSawTarget++;
                if (this.ticksSinceLastSawTarget > loseTimeout) {
                    BrainUtils.setMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.LOSING_SCENT);
                    this.setStalking(true);
                }
            } else {
                this.ticksSinceLastSawTarget = 0;
            }
        } else {
            this.ticksSinceLastSawTarget = 0;
        }

        // Stuck detection: only relevant while HUNTING.
        if (state == WendigoState.HUNTING && target != null) {
            Vec3 now = this.position();
            if (this.lastPosForStuckCheck.distanceToSqr(now) < 0.04) {
                this.ticksWithoutMovement++;
            } else {
                this.ticksWithoutMovement = 0;
                this.lastPosForStuckCheck = now;
            }
            if (this.ticksWithoutMovement > 200) {
                Vec3 toward = target.position().subtract(now).normalize().scale(3.0);
                this.teleportTo(now.x + toward.x, target.getY(), now.z + toward.z);
                this.ticksWithoutMovement = 0;
                this.lastPosForStuckCheck = this.position();
            }
        } else {
            this.ticksWithoutMovement = 0;
            this.lastPosForStuckCheck = this.position();
        }

        // Copy server-side state into synced fields for client renderer/HUD.
        this.entityData.set(DATA_AI_STATE, state.ordinal());
        float detection = BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.DETECTION_LEVEL.get(), () -> 0f);
        this.entityData.set(DATA_DETECTION_PERCENT, Math.round(detection * 100f));
    }

    private void decrementCooldown(MemoryModuleType<Integer> mem) {
        Integer cur = BrainUtils.getMemory(this, mem);
        if (cur != null && cur > 0) {
            BrainUtils.setMemory(this, mem, cur - 1);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1) {
            this.triggerAnim(MAIN_CONTROLLER, "idle");
            this.level().playSound(null, this.blockPosition(),
                    ModSounds.WENDIGO_DEATH.get(), SoundSource.HOSTILE, 1.5F, 1.0F);
        }
        if (this.deathTime >= 60 && !this.level().isClientSide) {
            ServerLevel level = (ServerLevel) this.level();
            UUID assignedPlayer = BrainUtils.getMemory(this, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
            UUID myUuid = this.getUUID();
            this.remove(RemovalReason.KILLED);

            // Only touch the lifecycle ledger if THIS wendigo was the player's
            // lifecycle-tracked one — auto-assigned wendigos (spawn egg, /summon)
            // don't trigger respawn scheduling and don't unassign anyone.
            if (assignedPlayer != null) {
                WendigoAssignmentData data = WendigoAssignmentData.get(level);
                if (data.getWendigoFor(assignedPlayer).map(myUuid::equals).orElse(false)) {
                    data.unassign(assignedPlayer);
                    long delayTicks = WendigoConfig.SERVER.respawnDelayMinutes.get() * 60L * 20L;
                    data.scheduleRespawn(assignedPlayer, level.getGameTime() + delayTicks);
                }
            }

            level.playSound(null, this.blockPosition(),
                    ModSounds.WENDIGO_DEATH_GLOBAL.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        }
    }

    // ---------- Player assignment / targeting ----------

    @Nullable
    public Player getAssignedPlayer() {
        UUID uuid = BrainUtils.getMemory(this, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        if (uuid == null) return null;
        if (!(this.level() instanceof ServerLevel level)) return null;
        return level.getPlayerByUUID(uuid);
    }

    public boolean isAssignedPlayer(Entity entity) {
        UUID assigned = BrainUtils.getMemory(this, ModMemoryModuleTypes.ASSIGNED_PLAYER.get());
        return assigned != null && entity != null && entity.getUUID().equals(assigned);
    }

    public boolean canSenseAssignedPlayer(Player player) {
        if (player == null) return false;
        if (player.isSpectator() || player.isCreative()) return false;
        if (!player.isAlive()) return false;
        if (player.isInWater()) return false;
        double maxDist = WendigoConfig.SERVER.maxDetectionDistance.get();
        return this.distanceToSqr(player) <= maxDist * maxDist;
    }

    public float computeDetectionDelta(Player player) {
        if (!canSenseAssignedPlayer(player)) return 0f;

        float baseRate = WendigoConfig.SERVER.baseSniffRate.get().floatValue();
        float meatBonus = WendigoConfig.SERVER.meatSniffBonus.get().floatValue();
        float centerBonusMax = WendigoConfig.SERVER.centerProximityMaxBonus.get().floatValue();
        float sneakPenalty = WendigoConfig.SERVER.sneakSniffReduction.get().floatValue();
        float biomeRadius = WendigoConfig.SERVER.biomeRadius.get().floatValue();

        float delta = baseRate;

        if (BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.ASSIGNED_PLAYER_HAS_MEAT.get(), () -> false)) {
            delta += meatBonus;
        }

        float distToCenter = BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.DISTANCE_TO_CENTER.get(), () -> biomeRadius);
        float centerNormalized = Math.max(0f, 1f - (distToCenter / biomeRadius));
        delta += centerBonusMax * centerNormalized;

        if (BrainUtils.memoryOrDefault(this, ModMemoryModuleTypes.ASSIGNED_PLAYER_SNEAKING.get(), () -> false)) {
            delta -= sneakPenalty;
        }

        if (player.isInWater()) {
            delta = 0f;
        }

        return delta;
    }

    public void triggerReveal() {
        BrainUtils.setMemory(this, ModMemoryModuleTypes.WENDIGO_STATE.get(), WendigoState.REVEALING);
        this.triggerAnim(MAIN_CONTROLLER, "roar");

        this.level().playSound(null, this.blockPosition(),
                ModSounds.WENDIGO_SCREAM_REVEAL.get(),
                SoundSource.HOSTILE,
                1.5F, 1.0F);

        Player player = this.getAssignedPlayer();
        if (player != null) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, false));
        }

        this.revealTransitionTicks = 60;
    }

    public boolean isStalking() {
        return this.entityData.get(DATA_STALKING);
    }

    public void setStalking(boolean stalking) {
        this.entityData.set(DATA_STALKING, stalking);
    }

    public int getDetectionPercent() {
        return this.entityData.get(DATA_DETECTION_PERCENT);
    }

    public WendigoState getStateSynced() {
        return WendigoState.fromOrdinal(this.entityData.get(DATA_AI_STATE));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && !this.isAssignedPlayer(target)) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof WendigoEntity) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (!isAssignedPlayer(target)) return false;
        return super.canAttack(target);
    }

    // ---------- SmartBrainOwner ----------

    @Override
    public List<ExtendedSensor<WendigoEntity>> getSensors() {
        return ObjectArrayList.of(
                new AssignedPlayerSensor<>(),
                new MeatInInventorySensor<>(),
                new BiomeCenterProximitySensor<>(),
                new PlayerSneakingSensor<>(),
                new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<WendigoEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>()
        );
    }

    @Override
    public BrainActivityGroup<WendigoEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new SniffOut<>(),
                new EmitStalkingSounds<>(),
                new FirstApplicableBehaviour<>(
                        new MoveTowardLastKnownPlayerPos<>(),
                        new SetRandomWalkTarget<>().speedModifier(0.5f),
                        new Idle<>().runFor(e -> e.getRandom().nextInt(60, 120))
                )
        );
    }

    @Override
    public BrainActivityGroup<WendigoEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<WendigoEntity>().invalidateIf(this::shouldDisengage),
                new TargetOrRetaliate<>(),
                new FirstApplicableBehaviour<>(
                        new ParalysisRoarAttack<>(),
                        new ChargeAttack<>(),
                        new WendigoBiteAttack<>(),
                        new SetWalkTargetToAttackTarget<WendigoEntity>().speedMod((owner, target) -> 1.0f)
                )
        );
    }

    private boolean shouldDisengage(WendigoEntity self, LivingEntity target) {
        if (target == null || !target.isAlive()) return true;
        if (target instanceof Player p && (p.isCreative() || p.isSpectator())) return true;
        if (!self.isAssignedPlayer(target)) return true;
        WendigoState state = BrainUtils.memoryOrDefault(self, ModMemoryModuleTypes.WENDIGO_STATE.get(), () -> WendigoState.DORMANT);
        return state == WendigoState.LOSING_SCENT || state == WendigoState.DORMANT;
    }

    // ---------- Sounds ----------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WENDIGO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WENDIGO_DEATH.get();
    }

    // ---------- Animation ----------

    // RawAnimation handles reused by the locomotion controller.
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation RUN_ANIM  = RawAnimation.begin().thenLoop("run2");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Trigger-driven controller for one-off animations and the /wendigoadmin command.
        controllers.add(new AnimationController<>(this, MAIN_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim("idle",        IDLE_ANIM)
                .triggerableAnim("run2",        RUN_ANIM)
                .triggerableAnim("attack",      RawAnimation.begin().thenLoop("attack"))
                .triggerableAnim("attack2",     RawAnimation.begin().thenLoop("attack2"))
                .triggerableAnim("roar",        RawAnimation.begin().then("roar", Animation.LoopType.PLAY_ONCE))
                .triggerableAnim("ass_scratch", RawAnimation.begin().thenLoop("ass scratch"))
        );

        // Automatic locomotion controller. Smooth 8-tick transitions reduce stiffness.
        // Walk is derived from run2 by playing it at half speed — same skeleton motion, slower cadence.
        AnimationController<WendigoEntity> loco =
                new AnimationController<>(this, "locomotion", 8, this::locomotionPredicate);
        loco.setAnimationSpeed(0.7);   // ~30% slowdown so movement reads less twitchy
        controllers.add(loco);
    }

    private software.bernie.geckolib.core.object.PlayState locomotionPredicate(
            software.bernie.geckolib.core.animation.AnimationState<WendigoEntity> state) {
        double vx = this.getDeltaMovement().x;
        double vz = this.getDeltaMovement().z;
        double speed = Math.sqrt(vx * vx + vz * vz);

        if (speed > 0.18) {
            // Sprint / charge speeds — full-speed run.
            state.getController().setAnimationSpeed(1.0);
            state.setAndContinue(RUN_ANIM);
        } else if (speed > 0.02) {
            // Walking — same skeleton motion as run, but at half cadence.
            state.getController().setAnimationSpeed(0.5);
            state.setAndContinue(RUN_ANIM);
        } else {
            state.getController().setAnimationSpeed(1.0);
            state.setAndContinue(IDLE_ANIM);
        }
        return software.bernie.geckolib.core.object.PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
