package io.github.novarch129.jojomod.network.message.client;

import io.github.novarch129.jojomod.capability.stand.Stand;
import io.github.novarch129.jojomod.entity.FakePlayerEntity;
import io.github.novarch129.jojomod.event.custom.AbilityEvent;
import io.github.novarch129.jojomod.init.SoundInit;
import io.github.novarch129.jojomod.network.message.IMessage;
import io.github.novarch129.jojomod.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CToggleAbilityPacket implements IMessage<CToggleAbilityPacket> {
    @Override
    public void encode(CToggleAbilityPacket msg, PacketBuffer buffer) {
    }

    @Override
    public CToggleAbilityPacket decode(PacketBuffer buffer) {
        return new CToggleAbilityPacket();
    }

    @Override
    public void handle(CToggleAbilityPacket msg, Supplier<NetworkEvent.Context> supplier) {
        final NetworkEvent.Context ctx = supplier.get();
        if (ctx.getDirection().getReceptionSide().isServer()) {
            ctx.enqueueWork(() ->
            {
                ServerPlayerEntity sender = ctx.getSender();
                if (sender == null)
                    return;
                Stand.getLazyOptional(sender).ifPresent(props -> {
                    FakePlayerEntity fakePlayer = new FakePlayerEntity(sender.world, sender);
                    fakePlayer.setPosition(fakePlayer.getParent().getPosX(), fakePlayer.getParent().getPosY(), fakePlayer.getParent().getPosZ());
                    int standID = props.getStandID();
                    int act = props.getAct();
                    boolean standOn = props.getStandOn();

                    props.setAbility(!props.getAbility());

                    if (props.getAbility()) {
                        switch (standID) {
                            case Util.StandID.THE_HAND:
                            case Util.StandID.MAGICIANS_RED:
                            case Util.StandID.KILLER_QUEEN:
                            case Util.StandID.THE_EMPEROR:
                                break;
                            case Util.StandID.GOLD_EXPERIENCE: {
                                sender.sendMessage(new StringTextComponent("Mode: Lifegiver"));
                                break;
                            }
                            case Util.StandID.GER: {
                                sender.sendMessage(new StringTextComponent("Mode: Gold Experience Requiem"));
                                break;
                            }
                            case Util.StandID.AEROSMITH: {
                                sender.sendMessage(new StringTextComponent("Ability: ON"));
                                if (standOn)
                                    sender.world.addEntity(fakePlayer);
                            }
                            default: {
                                if (standID != Util.StandID.MADE_IN_HEAVEN || act != 0)
                                    sender.sendMessage(new StringTextComponent("Ability: ON"));
                            }
                        }
                    } else {
                        switch (standID) {
                            case Util.StandID.THE_HAND:
                            case Util.StandID.MAGICIANS_RED:
                            case Util.StandID.THE_EMPEROR:
                            case Util.StandID.KILLER_QUEEN:
                                break;
                            case Util.StandID.GOLD_EXPERIENCE:
                            case Util.StandID.GER: {
                                sender.sendMessage(new StringTextComponent("Mode: Normal"));
                                break;
                            }
                            default: {
                                if (standID != Util.StandID.MADE_IN_HEAVEN || act != 0)
                                    sender.sendMessage(new StringTextComponent("Ability: OFF"));
                                if (props.getStandID() == Util.StandID.THE_WORLD && props.getStandOn() && props.getTimeLeft() > 780 && props.getCooldown() <= 0)
                                    sender.world.playSound(null, new BlockPos(sender.getPosX(), sender.getPosY(), sender.getPosZ()), SoundInit.RESUME_TIME.get(), SoundCategory.NEUTRAL, 5.0f, 1.0f);

                                if (props.getStandID() == Util.StandID.STAR_PLATINUM && props.getStandOn() && props.getTimeLeft() > 900 && props.getCooldown() <= 0)
                                    sender.world.playSound(null, new BlockPos(sender.getPosX(), sender.getPosY(), sender.getPosZ()), SoundInit.TIME_RESUME_STAR_PLATINUM.get(), SoundCategory.NEUTRAL, 5.0f, 1.0f);
                            }
                        }
                    }

                    MinecraftForge.EVENT_BUS.post(props.getAbility() ? new AbilityEvent.AbilityActivated(sender) : new AbilityEvent.AbilityDeactivated(sender));
                });
            });
        }
        ctx.setPacketHandled(true);
    }
}