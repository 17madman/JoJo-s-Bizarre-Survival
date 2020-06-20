package com.novarch.jojomod.entities.stands.theHand;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.EntityStandBase;
import com.novarch.jojomod.entities.stands.EntityStandPunch;
import com.novarch.jojomod.init.EntityInit;
import com.novarch.jojomod.init.SoundInit;
import com.novarch.jojomod.util.Util;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("ConstantConditions")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EntityTheHand extends EntityStandBase {
	private int oratick = 0;

	private int oratickr = 0;

	public EntityTheHand(EntityType<? extends EntityStandBase> type, World world) {
		super(type, world);
		spawnSound = SoundInit.SPAWN_MAGICIANS_RED.get();
		standID = Util.StandID.theHand;
	}

	public EntityTheHand(World world) {
		super(EntityInit.THE_HAND.get(), world);
		spawnSound = SoundInit.SPAWN_MAGICIANS_RED.get();
		standID = Util.StandID.theHand;
	}

	public void teleportEntity(int id) {
		Entity entity = world.getEntityByID(id);
		if(entity != null && getMaster() != null) {
			float yaw = getMaster().rotationYaw;
			float pitch = getMaster().rotationPitch;
			double motionX = (-MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
			double motionZ = (MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
			double motionY = (-MathHelper.sin((pitch) / 180.0F * (float) Math.PI) * 1.0f);
			entity.setMotion(-motionX * 2, -motionY * 2, -motionZ * 2);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (getMaster() != null) {
			PlayerEntity player = getMaster();
			Stand.getLazyOptional(player).ifPresent(props -> ability = props.getAbility());

			followMaster();
			setRotationYawHead(player.rotationYaw);
			setRotation(player.rotationYaw, player.rotationPitch);

			if (player.isSprinting()) {
				if (attackSwing(player))
					oratick++;
				if (oratick == 1) {
					if (!world.isRemote)
						orarush = true;
				}
			} else if (attackSwing(player)) {
				if (!world.isRemote) {
					oratick++;
					if (oratick == 1) {
						world.playSound(null, new BlockPos(getPosX(), getPosY(), getPosZ()), SoundInit.PUNCH_MISS.get(), getSoundCategory(), 1.0F, 0.8F / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
						EntityStandPunch.TheHand theHand = new EntityStandPunch.TheHand(world, this, player);
						theHand.shoot(player, player.rotationPitch, player.rotationYaw, 1.0f, 0.4f);
						world.addEntity(theHand);
					}
				}
			}
			if (player.swingProgressInt == 0)
				oratick = 0;
			if (orarush) {
				player.setSprinting(false);
				oratickr++;
				if (oratickr >= 10)
					if (!world.isRemote) {
						player.setSprinting(false);
						EntityStandPunch.TheHand theHand1 = new EntityStandPunch.TheHand(world, this, player);
						theHand1.setRandomPositions();
						theHand1.shoot(player, player.rotationPitch, player.rotationYaw, 0.8f, 0.5f);
						world.addEntity(theHand1);
						EntityStandPunch.TheHand theHand2 = new EntityStandPunch.TheHand(world, this, player);
						theHand2.setRandomPositions();
						theHand2.shoot(player, player.rotationPitch, player.rotationYaw, 0.8f, 0.5f);
						world.addEntity(theHand2);
					}
				if (oratickr >= 80) {
					orarush = false;
					oratickr = 0;
				}
			}
		}
	}
}
