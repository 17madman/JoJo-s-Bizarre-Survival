package io.github.novarch129.jojomod.init;

import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.network.message.client.*;
import io.github.novarch129.jojomod.network.message.server.SSyncSilverChariotArmorPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncStandCapabilityPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncStandMasterPacket;
import io.github.novarch129.jojomod.network.message.server.SSyncTimestopCapabilityPacket;

@SuppressWarnings("ALL")
public class PacketInit {
    public static void register() {
        int networkId = 0;
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                CSyncStandSummonPacket.class,
                CSyncStandSummonPacket::encode,
                CSyncStandSummonPacket::decode,
                CSyncStandSummonPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                CSyncStandAttackPacket.class,
                CSyncStandAttackPacket::encode,
                CSyncStandAttackPacket::decode,
                CSyncStandAttackPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                CSyncAbilityPacket.class,
                CSyncAbilityPacket::encode,
                CSyncAbilityPacket::decode,
                CSyncAbilityPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                SSyncStandCapabilityPacket.class,
                SSyncStandCapabilityPacket::encode,
                SSyncStandCapabilityPacket::decode,
                SSyncStandCapabilityPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                SSyncTimestopCapabilityPacket.class,
                SSyncTimestopCapabilityPacket::encode,
                SSyncTimestopCapabilityPacket::decode,
                SSyncTimestopCapabilityPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                SSyncSilverChariotArmorPacket.class,
                SSyncSilverChariotArmorPacket::encode,
                SSyncSilverChariotArmorPacket::decode,
                SSyncSilverChariotArmorPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                CSyncAerosmithPacket.class,
                CSyncAerosmithPacket::encode,
                CSyncAerosmithPacket::decode,
                CSyncAerosmithPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                CSyncStandAbilitiesPacket.class,
                CSyncStandAbilitiesPacket::encode,
                CSyncStandAbilitiesPacket::decode,
                CSyncStandAbilitiesPacket::handle);
        JojoBizarreSurvival.INSTANCE.registerMessage(networkId++,
                SSyncStandMasterPacket.class,
                SSyncStandMasterPacket::encode,
                SSyncStandMasterPacket::decode,
                SSyncStandMasterPacket::handle);
    }
}