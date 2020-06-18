package com.novarch.jojomod.network.message.client;

import com.novarch.jojomod.capabilities.stand.Stand;
import com.novarch.jojomod.entities.stands.aerosmith.EntityAerosmith;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

@SuppressWarnings({"unused", "ConstantConditions"})
public class CSyncAerosmithKeybindsPacket {
	private int action;
	private int direction;
	private boolean sprint;


	public CSyncAerosmithKeybindsPacket(int action, int direction, boolean sprint) {
		this.action = action;
		this.direction = direction;
		this.sprint = sprint;
	}

	public static void encode(CSyncAerosmithKeybindsPacket msg, PacketBuffer buffer) {
		buffer.writeInt(msg.action);
		buffer.writeInt(msg.direction);
		buffer.writeBoolean(msg.sprint);
	}

	public static CSyncAerosmithKeybindsPacket decode(PacketBuffer buffer) {
		return new CSyncAerosmithKeybindsPacket(
				buffer.readInt(),
				buffer.readInt(),
				buffer.readBoolean()
		);
	}

	public static void handle(CSyncAerosmithKeybindsPacket message, Supplier<Context> ctx) {
		if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				PlayerEntity player = ctx.get().getSender();
				assert player != null;
				World world = player.world;
				if (world != null)
					if (!world.isRemote) {
						world.getServer().getWorld(player.dimension).getEntities()
								.filter(entity -> entity instanceof EntityAerosmith)
								.forEach(entity -> {
									float yaw = entity.rotationYaw;
									float pitch = entity.rotationPitch;
									double motionX = (-MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
									double motionZ = (MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * 1.0f);
									double motionY = (-MathHelper.sin((pitch) / 180.0F * (float) Math.PI) * 1.0f);
									switch (message.action) {
										//Movement
										case 1: {
											switch (message.direction) {
												//Forwards
												case 1: {
													if (message.sprint)
														entity.setVelocity(motionX, motionY, motionZ);
													else
														entity.setVelocity(motionX * 0.5, entity.getMotion().getY(), motionZ * 0.5);
													break;
												}
												//Backwards
												case 2: {
													entity.setVelocity(-motionX * 0.6, entity.getMotion().getY(), -motionZ * 0.6);
													break;
												}
												//Right
												case 3: {
													entity.setVelocity(-motionZ * 0.5, entity.getMotion().getY(), motionX * 0.5);
													break;
												}
												//Left
												case 4: {
													entity.setVelocity(motionZ * 0.5, entity.getMotion().getY(), -motionX * 0.5);
													break;
												}
												//Up
												case 5: {
													entity.addVelocity(0, 0.35, 0);
													break;
												}
												//Down
												case 6: {
													((EntityAerosmith) entity).shouldFall = true;
													entity.addVelocity(0, -0.2, 0);
													break;
												}
												default:
													break;
											}
											break;
										}
										//Bomb
										case 2: {
											Stand.getLazyOptional(player).ifPresent(props -> {
												if (props.getCooldown() <= 0) {
													TNTEntity tnt = new TNTEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), player);
													tnt.setVelocity(entity.getLookVec().getX(), entity.getLookVec().getY(), entity.getLookVec().getZ());
													entity.world.addEntity(tnt);
													props.setCooldown(200);
												}
											});
										}
										break;
									}
								});
					}
			});
		}
		ctx.get().setPacketHandled(true);
	}
}
