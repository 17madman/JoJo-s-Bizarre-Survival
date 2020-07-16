package io.github.novarch129.jojomod.network.message;

import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.network.message.client.*;
import io.github.novarch129.jojomod.network.message.server.SSyncSilverChariotArmorPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncStandCapabilityPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncStandMasterPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncTimestopCapabilityPacket;
import net.minecraftforge.fml.network.NetworkDirection;

import java.util.Optional;

@SuppressWarnings("unused")
public class PacketHandler {
    static int networkId = 0;

    public static void register() {
        registerPacket(CStandSummonPacket.class, new CStandSummonPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CStandAttackPacket.class, new CStandAttackPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CToggleAbilityPacket.class, new CToggleAbilityPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CAerosmithControlPacket.class, new CAerosmithControlPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(CSyncStandAbilitiesPacket.class, new CSyncStandAbilitiesPacket(), NetworkDirection.PLAY_TO_SERVER);
        registerPacket(SSyncStandCapabilityPacket.class, new SSyncStandCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncTimestopCapabilityPacket.class, new SSyncTimestopCapabilityPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncStandMasterPacket.class, new SSyncStandMasterPacket(), NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(SSyncSilverChariotArmorPacket.class, new SSyncSilverChariotArmorPacket(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <P> void registerPacket(Class<P> clazz, IMessage<P> message) {
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++, clazz, message::encode, message::decode, message::handle);
    }

    public static <P> void registerPacket(Class<P> clazz, IMessage<P> message, NetworkDirection direction) {
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++, clazz, message::encode, message::decode, message::handle, Optional.of(direction));
    }
}
