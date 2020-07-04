package com.novarch.jojomod.entities.stands.attacks;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.AbstractStandEntity;
import com.novarch.jojomod.entities.stands.AbstractStandPunchEntity;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class CMoonPunchEntity extends AbstractStandPunchEntity {
    public CMoonPunchEntity(EntityType<? extends AbstractStandPunchEntity> type, World worldIn) {
        super(type, worldIn);
    }

    public CMoonPunchEntity(World worldIn, AbstractStandEntity shooter, PlayerEntity player) {
        super(EntityInit.CMOON_PUNCH.get(), worldIn, shooter, player);
    }

    @Override
    protected void onEntityHit(EntityRayTraceResult result) {
        Entity entity = result.getEntity();
        int act = Stand.getCapabilityFromPlayer(standMaster).getAct();
        int standID = Stand.getCapabilityFromPlayer(standMaster).getStandID();
        if (act == 1 && standID == Util.StandID.CMOON) {
            WhitesnakePunchEntity whitesnakePunch = new WhitesnakePunchEntity(world, shootingStand, standMaster);
            whitesnakePunch.onEntityHit(result);
        } else {
            if (!(entity instanceof LivingEntity)) return;
            entity.attackEntityFrom(DamageSource.causeMobDamage(standMaster), ((LivingEntity) entity).getHealth() / 2 + 0.0001f);
            entity.hurtResistantTime = 0;
            if (shootingStand.ability) {
                entity.setCustomName(new StringTextComponent("Dinnerbone"));
                if (world.rand.nextInt(10) == 10)
                    ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SPEED, 100, 5));
            }
        }
    }

    @Override
    protected void onBlockHit(BlockRayTraceResult result) {
        BlockPos pos = result.getPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlockHardness(world, pos) != -1 && state.getBlockHardness(world, pos) < 3) {
            world.removeBlock(pos, false);
            if (world.rand.nextBoolean())
                state.getBlock().harvestBlock(world, standMaster, pos, state, null, standMaster.getActiveItemStack());
        }
    }
}
