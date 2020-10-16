package io.github.novarch129.jojomod.network.message.server;

import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.entity.stand.AerosmithEntity;
import io.github.novarch129.jojomod.network.message.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SAerosmithPacket implements IMessage<SAerosmithPacket> {
    private int entityID;
    private byte action;

    public SAerosmithPacket() {
    }

    public SAerosmithPacket(int entityID, byte action) {
        this.entityID = entityID;
        this.action = action;
    }

    @Override
    public void encode(SAerosmithPacket message, PacketBuffer buffer) {
        buffer.writeInt(message.entityID);
        buffer.writeByte(message.action);
    }

    @Override
    public SAerosmithPacket decode(PacketBuffer buffer) {
        return new SAerosmithPacket(buffer.readInt(), buffer.readByte());
    }

    @Override
    public void handle(SAerosmithPacket message, Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            ctx.get().enqueueWork(() -> {
                World world = JojoBizarreSurvival.PROXY.getWorld();
                if (world == null) return;
                Entity entity = world.getEntityByID(message.entityID);
                if (!(entity instanceof AerosmithEntity)) return;
                switch (message.action) {
                    default:
                        return;
                    case 0: {
                        if (JojoBizarreSurvival.PROXY.getPlayer() != null)
                            Minecraft.getInstance().setRenderViewEntity(JojoBizarreSurvival.PROXY.getPlayer());
                        break;
                    }
                    case 1: {
                        break;
                    }
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
