package io.github.novarch129.jojomod.entity.stand;

import io.github.novarch129.jojomod.capability.Stand;
import io.github.novarch129.jojomod.capability.StandEffects;
import io.github.novarch129.jojomod.entity.stand.attack.NailBulletEntity;
import io.github.novarch129.jojomod.entity.stand.attack.TuskAct4PunchEntity;
import io.github.novarch129.jojomod.init.EntityInit;
import io.github.novarch129.jojomod.init.SoundInit;
import io.github.novarch129.jojomod.util.IChargeable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.HorseEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("ConstantConditions")
public class TuskAct4Entity extends AbstractStandEntity implements IChargeable {
    private int bulletChargeTicks;
    private int prevBulletChargeTicks;

    public TuskAct4Entity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    @Override
    public SoundEvent getSpawnSound() {
        return SoundInit.SPAWN_TUSK_ACT_4.get();
    }

    @Override
    public void attack(boolean special) {
        if (getMaster() == null) return;
        attackTick++;
        if (attackTick == 1)
            if (special) {
                world.playSound(null, getPosition(), SoundInit.TUSK_ACT_4_ORA.get(), SoundCategory.VOICE, 1, 1);
                attackRush = true;
            }
    }

    @Override
    public void chargeAttack(boolean isCharging) {
        if (getMaster() == null) return;
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (props.getCooldown() > 0) return;
            props.setCharging(isCharging);
            if (isCharging && bulletChargeTicks <= (master.getRidingEntity() instanceof HorseEntity ? 1000 : 440)) {
                setChargeTicks(bulletChargeTicks + 10);
                props.setStandDamage(4 + (bulletChargeTicks + 10) / 20f);
            } else if (!isCharging)
                setChargeTicks(0);
        });
    }

    @Override
    public int getChargeTicks() {
        return bulletChargeTicks;
    }

    @Override
    public void setChargeTicks(int chargeTicks) {
        this.prevBulletChargeTicks = this.bulletChargeTicks;
        this.bulletChargeTicks = chargeTicks;
    }

    @Override
    public int getPrevChargeTicks() {
        return prevBulletChargeTicks;
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            if (master.getRidingEntity() instanceof AbstractHorseEntity)
                StandEffects.getLazyOptional(master).ifPresent(props -> props.setRotating(false));
            Stand.getLazyOptional(master).ifPresent(props -> {
                ability = props.getAbility();

                if (props.getAct() == props.getMaxAct() - 3 && props.getStandOn()) {
                    remove();
                    TuskAct3Entity tuskAct3Entity = new TuskAct3Entity(EntityInit.TUSK_ACT_3.get(), world);
                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                    tuskAct3Entity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), master.rotationYaw, master.rotationPitch);
                    tuskAct3Entity.setMaster(master);
                    tuskAct3Entity.setMasterUUID(master.getUniqueID());
                    world.addEntity(tuskAct3Entity);
                }

                if (props.getAbilityUseCount() < 10 && getChargeTicks() == 0 && getChargeTicks() != getPrevChargeTicks()) {
                    world.playSound(null, getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 1, 0.6f / (rand.nextFloat() * 0.3f + 1) * 2);
                    NailBulletEntity nailBulletEntity = new NailBulletEntity(world, this, master, true, master.getRidingEntity() instanceof AbstractHorseEntity);
                    nailBulletEntity.damage = 4 + (getPrevChargeTicks() + 10) / 20f;
                    if (nailBulletEntity.damage >= 15) {
                        for (int i = 0; i < (nailBulletEntity.damage / 5 - 1); i++) {
                            world.playSound(null, getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 2, (1 + (rand.nextFloat() - rand.nextFloat()) * 0.2f) * 0.7f);
                            if (world.isRemote) {
                                for (int k = 0; k < 20; k++) {
                                    double d0 = rand.nextGaussian() * 0.02;
                                    double d1 = rand.nextGaussian() * 0.02;
                                    double d2 = rand.nextGaussian() * 0.02;
                                    Vec3d position = master.getLookVec().mul(0.5, 1, 0.5).add(master.getPositionVec()).add(0, 0.5, 0);
                                    world.addParticle(ParticleTypes.POOF,
                                            position.getX() + ((rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()) / 2),
                                            position.getY() + ((rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()) / 2),
                                            position.getZ() + ((rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()) / 2),
                                            d0, d1, d2);
                                }
                            } else
                                world.setEntityState(this, (byte) 20);
                        }
                    }
                    nailBulletEntity.shoot(getMaster(), 0, rotationYaw, 6, 0.05f);
                    if (!world.isRemote) {
                        world.addEntity(nailBulletEntity);
                        props.setAbilityUseCount(props.getAbilityUseCount() + 1);
                    }
                }

                if (props.getAbilityUseCount() == 10 && props.getCooldown() == 0)
                    props.setCooldown(600);

                if (props.getCooldown() == 1)
                    props.setAbilityUseCount(0);
            });

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;

            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker > 55)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        TuskAct4PunchEntity tuskAct4PunchEntity = new TuskAct4PunchEntity(world, this, master);
                        tuskAct4PunchEntity.randomizePositions();
                        tuskAct4PunchEntity.shoot(master, master.rotationPitch, master.rotationYaw, 5, 0.001f);
                        world.addEntity(tuskAct4PunchEntity);
                        TuskAct4PunchEntity tuskAct4PunchEntity1 = new TuskAct4PunchEntity(world, this, master);
                        tuskAct4PunchEntity1.randomizePositions();
                        tuskAct4PunchEntity1.shoot(master, master.rotationPitch, master.rotationYaw, 5, 0.001f);
                        world.addEntity(tuskAct4PunchEntity1);
                    }
                if (attackTicker >= 170) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}
