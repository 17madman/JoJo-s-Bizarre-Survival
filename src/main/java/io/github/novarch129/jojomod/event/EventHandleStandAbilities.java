package io.github.novarch129.jojomod.event;

import io.github.novarch129.jojomod.JojoBizarreSurvival;
import io.github.novarch129.jojomod.capability.*;
import io.github.novarch129.jojomod.config.JojoBizarreSurvivalConfig;
import io.github.novarch129.jojomod.entity.stand.AbstractStandEntity;
import io.github.novarch129.jojomod.entity.stand.MadeInHeavenEntity;
import io.github.novarch129.jojomod.entity.stand.StarPlatinumEntity;
import io.github.novarch129.jojomod.entity.stand.TheWorldEntity;
import io.github.novarch129.jojomod.event.custom.StandAttackEvent;
import io.github.novarch129.jojomod.event.custom.StandEvent;
import io.github.novarch129.jojomod.init.EffectInit;
import io.github.novarch129.jojomod.init.EntityInit;
import io.github.novarch129.jojomod.init.ItemInit;
import io.github.novarch129.jojomod.init.SoundInit;
import io.github.novarch129.jojomod.item.StandDiscItem;
import io.github.novarch129.jojomod.network.message.server.SSyncStandMasterPacket;
import io.github.novarch129.jojomod.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber(modid = JojoBizarreSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandleStandAbilities {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        Stand.getLazyOptional(player).ifPresent(stand -> {
            Random rand = player.world.rand;
            int standID = stand.getStandID();
            boolean standOn = stand.getStandOn();
            double cooldown = stand.getCooldown();
            double timeLeft = stand.getTimeLeft();
            double invulnerableTicks = stand.getInvulnerableTicks();

            if (!player.world.isRemote && stand.getGameTime() != -1 && stand.getGameTime() < player.world.getGameTime() - 24000) {
                player.world.setGameTime(stand.getGameTime());
                player.world.setDayTime(stand.getDayTime());
                stand.setGameTime(-1);
                stand.setDayTime(-1);
                player.setHealth(player.getMaxHealth());
                player.world.loadedTileEntityList.stream()
                        .filter(tileEntity -> tileEntity instanceof LockableTileEntity && !tileEntity.getWorld().isRemote)
                        .forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                            ((LockableTileEntity) tileEntity).clear();
                            if (tileEntity instanceof ChestTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                                    ((ChestTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof AbstractFurnaceTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getFurnaceInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getFurnaceInventory().get(i);
                                    ((AbstractFurnaceTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getFurnaceInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof BrewingStandTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getBrewingInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getBrewingInventory().get(i);
                                    ((BrewingStandTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getBrewingInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof BarrelTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getBarrelInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getBarrelInventory().get(i);
                                    ((BarrelTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getBarrelInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof DispenserTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getDispenserInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getDispenserInventory().get(i);
                                    ((DispenserTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getDispenserInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof HopperTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getHopperInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getHopperInventory().get(i);
                                    ((HopperTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getHopperInventory().set(i, ItemStack.EMPTY);
                                }
                            else if (tileEntity instanceof ShulkerBoxTileEntity)
                                for (int i = 0; i < standTileEntityEffects.getShulkerBoxInventory().size(); i++) {
                                    ItemStack stack = standTileEntityEffects.getShulkerBoxInventory().get(i);
                                    ((ShulkerBoxTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                    standTileEntityEffects.getShulkerBoxInventory().set(i, ItemStack.EMPTY);
                                }
                            tileEntity.markDirty();
                        }));
                player.getServer().getWorld(player.dimension).getEntities().forEach(entity -> {
                    if (entity instanceof PlayerEntity && !entity.world.isRemote)
                        StandPlayerEffects.getLazyOptional((PlayerEntity) entity).ifPresent(standPlayerEffects -> {
                            ((PlayerEntity) entity).inventory.clear();
                            for (int i = 0; i < standPlayerEffects.getMainInventory().size(); i++) {
                                ItemStack stack = standPlayerEffects.getMainInventory().get(i);
                                ((PlayerEntity) entity).inventory.setInventorySlotContents(i, stack);
                                standPlayerEffects.getMainInventory().set(i, ItemStack.EMPTY);
                            }
                            for (int i = 0; i < standPlayerEffects.getArmorInventory().size(); i++) {
                                ItemStack stack = standPlayerEffects.getArmorInventory().get(i);
                                ((PlayerEntity) entity).inventory.setInventorySlotContents(i + 36, stack);
                                standPlayerEffects.getArmorInventory().set(i, ItemStack.EMPTY);
                            }
                            for (int i = 0; i < standPlayerEffects.getOffHandInventory().size(); i++) {
                                ItemStack stack = standPlayerEffects.getOffHandInventory().get(i);
                                ((PlayerEntity) entity).inventory.setInventorySlotContents(i + 40, stack);
                                standPlayerEffects.getOffHandInventory().set(i, ItemStack.EMPTY);
                            }
                        });
                    StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                        if (!standEffects.getAlteredTileEntities().isEmpty())
                            standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                    blockPosList.forEach(blockPos -> {
                                        if (entity.world.getChunkProvider().isChunkLoaded(pos))
                                            entity.world.getChunkProvider().forceChunk(pos, true);
                                        TileEntity tileEntity = entity.world.getTileEntity(blockPos);
                                        if (!(tileEntity instanceof LockableTileEntity)) return;
                                        StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                                            ((LockableTileEntity) tileEntity).clear();
                                            if (tileEntity instanceof ChestTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getChestInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getChestInventory().get(i);
                                                    ((ChestTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getChestInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof AbstractFurnaceTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getFurnaceInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getFurnaceInventory().get(i);
                                                    ((AbstractFurnaceTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getFurnaceInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof BrewingStandTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getBrewingInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getBrewingInventory().get(i);
                                                    ((BrewingStandTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getBrewingInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof BarrelTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getBarrelInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getBarrelInventory().get(i);
                                                    ((BarrelTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getBarrelInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof DispenserTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getDispenserInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getDispenserInventory().get(i);
                                                    ((DispenserTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getDispenserInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof HopperTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getHopperInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getHopperInventory().get(i);
                                                    ((HopperTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getHopperInventory().set(i, ItemStack.EMPTY);
                                                }
                                            else if (tileEntity instanceof ShulkerBoxTileEntity)
                                                for (int i = 0; i < standTileEntityEffects.getShulkerBoxInventory().size(); i++) {
                                                    ItemStack stack = standTileEntityEffects.getShulkerBoxInventory().get(i);
                                                    ((ShulkerBoxTileEntity) tileEntity).setInventorySlotContents(i, stack);
                                                    standTileEntityEffects.getShulkerBoxInventory().set(i, ItemStack.EMPTY);
                                                }
                                            tileEntity.markDirty();
                                        });
                                    }));
                        if (standEffects.isShouldBeRemoved())
                            entity.remove();
                        if (entity instanceof ItemEntity && standEffects.getBitesTheDustPos() == BlockPos.ZERO)
                            entity.remove();
                        if (!standEffects.getDestroyedBlocks().isEmpty()) {
                            Map<BlockPos, BlockState> removalMap = new ConcurrentHashMap<>();
                            standEffects.getDestroyedBlocks().forEach((pos, list) -> {
                                list.forEach((blockPos, blockState) -> {
                                    if (player.world.getChunkProvider().isChunkLoaded(pos))
                                        player.world.getChunkProvider().forceChunk(pos, true);
                                    player.world.setBlockState(blockPos, blockState);
                                    removalMap.put(blockPos, blockState);
                                });
                                if (!removalMap.isEmpty())
                                    removalMap.forEach(list::remove);
                            });
                        }
                        if (standEffects.getBitesTheDustPos() != BlockPos.ZERO) {
                            entity.setPositionAndUpdate(standEffects.getBitesTheDustPos().getX(), standEffects.getBitesTheDustPos().getY(), standEffects.getBitesTheDustPos().getZ());
                            standEffects.setBitesTheDustPos(BlockPos.ZERO);
                        }
                    });
                });
                stand.setCooldown(36000);
            }

            if (invulnerableTicks > 0) {
                stand.setInvulnerableTicks(stand.getInvulnerableTicks() - 0.5);
                for (int i = 0; i < 10; i++)
                    player.world.addOptionalParticle(
                            ParticleTypes.DRAGON_BREATH,
                            player.getPosX() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                            player.getPosY() + player.world.rand.nextDouble(),
                            player.getPosZ() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                            0, 0.3 + (rand.nextBoolean() ? 0.1 : -0.1), 0);
                if (invulnerableTicks == 0.5)
                    stand.setCooldown(140);
            }
            if (standID == Util.StandID.STICKY_FINGERS && stand.getAbilityActive())
                for (int i = 0; i < 10; i++)
                    player.world.addOptionalParticle(
                            ParticleTypes.DRAGON_BREATH,
                            player.getPosX() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                            player.getPosY() + player.world.rand.nextDouble(),
                            player.getPosZ() + (player.world.rand.nextBoolean() ? rand.nextDouble() : -rand.nextDouble()),
                            0, 0.3 + (rand.nextBoolean() ? 0.1 : -0.1), 0);

            if (cooldown == 0.5 && stand.getStandID() != Util.StandID.MADE_IN_HEAVEN)
                stand.setTimeLeft(1000);

            if (standID == Util.StandID.GER)
                player.clearActivePotions();

            if (!player.world.isRemote && standID == Util.StandID.CMOON && stand.getAbilitiesUnlocked() == 3) {
                if ((int) player.getPosX() == 28 && (int) player.getPosZ() == 80 && player.world.canSeeSky(player.getPosition())) {
                    stand.removeStand();
                    stand.setStandID(Util.StandID.MADE_IN_HEAVEN);
                    stand.setStandOn(true);
                    MadeInHeavenEntity madeInHeaven = new MadeInHeavenEntity(EntityInit.MADE_IN_HEAVEN.get(), player.world);
                    if (Collections.frequency(player.getServer().getWorld(player.dimension).getEntities().collect(Collectors.toList()), madeInHeaven) > 0)
                        return;
                    Vec3d position = player.getLookVec().mul(0.5, 1, 0.5).add(player.getPositionVec()).add(0, 0.5, 0);
                    madeInHeaven.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), player.rotationYaw, player.rotationPitch);
                    madeInHeaven.setMaster(player);
                    madeInHeaven.setMasterUUID(player.getUniqueID());
                    JojoBizarreSurvival.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new SSyncStandMasterPacket(madeInHeaven.getEntityId(), player.getEntityId()));
                    player.world.addEntity(madeInHeaven);
                    player.sendStatusMessage(new StringTextComponent("Made in Heaven! 4/4"), true);
                }
            }

            if (!standOn) {
                if (cooldown > 0)
                    stand.setCooldown(stand.getCooldown() - 0.5);

                if (timeLeft < 1000)
                    stand.setTimeLeft(stand.getTimeLeft() + 0.5);

                player.setInvulnerable(false);
            } else if (!stand.getAbilityActive()) {
                if (cooldown > 0)
                    stand.setCooldown(stand.getCooldown() - 0.5);

                if (cooldown == 0.5 && stand.getStandID() != Util.StandID.MADE_IN_HEAVEN)
                    stand.setTimeLeft(1000);

                if (timeLeft < 1000 && cooldown == 0)
                    stand.setTimeLeft(stand.getTimeLeft() + 0.5);
            }
        });
    }

    @SubscribeEvent
    public static void xpEvent(PlayerXpEvent.XpChange event) {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;
        Stand.getLazyOptional(player).ifPresent(stand -> {
            if (Util.StandID.EVOLUTION_STANDS.contains(stand.getStandID())) {
                stand.addExperiencePoints(event.getAmount());
                if (stand.getExperiencePoints() >= 1000 && stand.getExperiencePoints() < 3000 && stand.getPrevExperiencePoints() < 1000) {
                    switch (stand.getStandID()) {
                        default:
                            break;
                        case Util.StandID.ECHOES_ACT_1: {
                            stand.removeStand();
                            stand.setStandID(Util.StandID.ECHOES_ACT_2);
                            player.sendStatusMessage(new StringTextComponent("Your\u00A7e Echoes\u00A7f has evolved to\u00A7e Act 2!"), true);
                            break;
                        }
                        case Util.StandID.TUSK_ACT_1: {
                            stand.removeStand();
                            stand.setStandID(Util.StandID.TUSK_ACT_2);
                            player.sendStatusMessage(new StringTextComponent("Your\u00A7e Tusk\u00A7f has evolved to\u00A7e Act 2!"), true);
                            break;
                        }
                    }
                } else if (stand.getExperiencePoints() >= 3000 && stand.getExperiencePoints() < 8000 && stand.getPrevExperiencePoints() < 3000) {
                    switch (stand.getStandID()) {
                        default:
                            break;
                        case Util.StandID.WHITESNAKE: {
                            stand.removeStand();
                            stand.setStandID(Util.StandID.CMOON);
                            player.sendStatusMessage(new StringTextComponent("Your\u00A7e Whitesnake\u00A7f has evolved into\u00A7e C-Moon!"), true);
                            break;
                        }
                        case Util.StandID.ECHOES_ACT_2: {
                            stand.removeStand();
                            stand.setStandID(Util.StandID.ECHOES_ACT_3);
                            player.sendStatusMessage(new StringTextComponent("Your\u00A7e Echoes\u00A7f has evolved to\u00A7e Act 3!"), true);
                            break;
                        }
                        case Util.StandID.TUSK_ACT_2: {
                            stand.removeStand();
                            stand.setStandID(Util.StandID.TUSK_ACT_3);
                            player.sendStatusMessage(new StringTextComponent("Your\u00A7e Tusk\u00A7f has evolved to\u00A7e Act 3!"), true);
                            break;
                        }
                    }
                } else if (stand.getExperiencePoints() >= 8000 && stand.getExperiencePoints() < 25000 && stand.getPrevExperiencePoints() < 8000) {
                    if (stand.getStandID() == Util.StandID.TUSK_ACT_3) {
                        stand.removeStand();
                        stand.setStandID(Util.StandID.TUSK_ACT_4);
                        player.sendStatusMessage(new StringTextComponent("Your\u00A7e Tusk\u00A7f has evolved to\u00A7e Act 4!"), true);
                    }
                } else if (stand.getExperiencePoints() >= 25000 && stand.getPrevExperiencePoints() < 25000 && stand.getExperiencePoints() < 1000000) {
                    if (stand.getStandID() == Util.StandID.KILLER_QUEEN) {
                        stand.addAbilityUnlocked(1);
                        player.sendStatusMessage(new StringTextComponent("Your\u00A7e Killer Queen\u00A7f can now obtain\u00A7e Bites the Dust!"), true);
                    }
                } else if (stand.getExperiencePoints() >= 1000000 && stand.getPrevExperiencePoints() < 1000000) {
                    if (stand.getStandID() == Util.StandID.GOLD_EXPERIENCE) {
                        stand.addAbilityUnlocked(1);
                        player.sendStatusMessage(new StringTextComponent("Your\u00A7e Gold Experience\u00A7f can now\u00A7e evolve!"), true);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void blockDestroyed(BlockEvent.BreakEvent event) {
        LivingEntity livingEntity = event.getPlayer();
        if (livingEntity == null) return;
        Chunk chunk = livingEntity.world.getChunkAt(livingEntity.getPosition());
        StandEffects.getLazyOptional(livingEntity).ifPresent(standEffects -> {
            if (standEffects.getDestroyedBlocks().containsKey(chunk.getPos()) && standEffects.getDestroyedBlocks().get(chunk.getPos()).containsKey(event.getPos()))
                return;
            if (standEffects.getBitesTheDustPos() != BlockPos.ZERO)
                standEffects.putDestroyedBlock(chunk.getPos(), event.getPos(), event.getState());
        });
    }

    @SubscribeEvent
    public static void blockPlaced(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity == null) return;
        Chunk chunk = entity.world.getChunkAt(entity.getPosition());
        StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
            if (standEffects.getDestroyedBlocks().containsKey(chunk.getPos()) && standEffects.getDestroyedBlocks().get(chunk.getPos()).containsKey(event.getPos()))
                return;
            if (standEffects.getBitesTheDustPos() != BlockPos.ZERO)
                standEffects.putDestroyedBlock(chunk.getPos(), event.getPos(), Blocks.AIR.getDefaultState());

        });
    }

    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (player == null) return;
        Chunk chunk = player.world.getChunkAt(player.getPosition());
        if (event.getUseBlock() == Event.Result.ALLOW && player.world.getTileEntity(event.getPos()) != null)
            StandEffects.getLazyOptional(player).ifPresent(standEffects -> {
                if (standEffects.getBitesTheDustPos() != BlockPos.ZERO)
                    standEffects.putAlteredTileEntity(chunk.getPos(), event.getPos());
            });
    }

    @SubscribeEvent
    public static void itemCrafted(PlayerEvent.ItemCraftedEvent event) {
        PlayerEntity player = event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(stand -> {
            if (stand.getStandID() == Util.StandID.CMOON && stand.getAbilitiesUnlocked() == 0 && event.getCrafting().getItem() == ItemInit.SUMMON_THE_WORLD.get()) {
                stand.addAbilityUnlocked(1);
                player.sendStatusMessage(new StringTextComponent("A bit closer to Heaven... 1/4"), true);
            }
        });
    }

    @SubscribeEvent
    public static void livingDeath(LivingDeathEvent event) {
        if (event.getSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity && (event.getEntityLiving() instanceof EnderDragonEntity || (event.getEntityLiving() instanceof PlayerEntity && Stand.getCapabilityFromPlayer((PlayerEntity) event.getEntityLiving()).getStandID() != 0))) {
            PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
            Stand.getLazyOptional(player).ifPresent(stand -> {
                if (stand.getStandID() == Util.StandID.CMOON && stand.getAbilitiesUnlocked() == 1) {
                    stand.addAbilityUnlocked(1);
                    player.sendStatusMessage(new StringTextComponent("A bit closer to Heaven... 2/4"), true);
                }
            });
        } else if (event.getEntityLiving() != null && event.getSource().getTrueSource() instanceof LivingEntity && event.getEntityLiving() instanceof PlayerEntity) {
            LivingEntity livingEntity = (LivingEntity) event.getSource().getTrueSource();
            StandEffects.getLazyOptional(livingEntity).ifPresent(standEffects -> {
                if (standEffects.getTimeOfDeath() != -1 && event.getEntityLiving().getUniqueID().equals(standEffects.getStandUser()))
                    standEffects.setTimeOfDeath(-1);
            });
        }
    }

    @SubscribeEvent
    public static void destroyItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (event.getItem().getItem() instanceof EnchantedGoldenAppleItem)
            Stand.getLazyOptional(player).ifPresent(stand -> {
                if (stand.getStandID() == Util.StandID.CMOON && stand.getAbilitiesUnlocked() == 2) {
                    stand.addAbilityUnlocked(1);
                    player.sendStatusMessage(new StringTextComponent("A bit closer to Heaven... 3/4"), true);
                }
            });
    }

    @SubscribeEvent
    public static void effectRemovedEvent(PotionEvent.PotionRemoveEvent event) {
        if (event.getPotion() == Effects.GLOWING) event.getEntityLiving().setGlowing(false);
        if (event.getPotion() == EffectInit.OXYGEN_POISONING.get()) event.setCanceled(true);
        if (event.getPotion() == EffectInit.HAZE.get()) event.setCanceled(true);
        if (event.getPotion() == EffectInit.AGING.get()) event.setCanceled(true);
    }

    @SubscribeEvent //This one still bugs me to this day, can't think of a way to automate it.
    public static void tooltipEvent(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof StandDiscItem)) return;
        String standName = "";
        if (event.getItemStack().getTag() != null)
            switch (event.getItemStack().getTag().getInt("StandID")) {
                case Util.StandID.KING_CRIMSON: {
                    standName = "King Crimson";
                    break;
                }
                case Util.StandID.D4C: {
                    standName = "D4C";
                    break;
                }
                case Util.StandID.GOLD_EXPERIENCE: {
                    standName = "Gold Experience";
                    break;
                }
                case Util.StandID.MADE_IN_HEAVEN: {
                    standName = "Made in Heaven";
                    break;
                }
                case Util.StandID.GER: {
                    standName = "Gold Experience Requiem";
                    break;
                }
                case Util.StandID.AEROSMITH: {
                    standName = "Aerosmith";
                    break;
                }
                case Util.StandID.WEATHER_REPORT: {
                    standName = "Weather Report";
                    break;
                }
                case Util.StandID.KILLER_QUEEN: {
                    standName = "Killer Queen";
                    break;
                }
                case Util.StandID.CRAZY_DIAMOND: {
                    standName = "Crazy Diamond";
                    break;
                }
                case Util.StandID.PURPLE_HAZE: {
                    standName = "Purple Haze";
                    break;
                }
                case Util.StandID.THE_EMPEROR: {
                    standName = "The Emperor";
                    break;
                }
                case Util.StandID.WHITESNAKE: {
                    standName = "Whitesnake";
                    break;
                }
                case Util.StandID.CMOON: {
                    standName = "C-Moon";
                    break;
                }
                case Util.StandID.THE_WORLD: {
                    standName = "The World";
                    break;
                }
                case Util.StandID.STAR_PLATINUM: {
                    standName = "Star Platinum";
                    break;
                }
                case Util.StandID.SILVER_CHARIOT: {
                    standName = "Silver Chariot";
                    break;
                }
                case Util.StandID.MAGICIANS_RED: {
                    standName = "Magician's Red";
                    break;
                }
                case Util.StandID.THE_HAND: {
                    standName = "The Hand";
                    break;
                }
                case Util.StandID.HIEROPHANT_GREEN: {
                    standName = "Hierophant Green";
                    break;
                }
                case Util.StandID.GREEN_DAY: {
                    standName = "Green Day";
                    break;
                }
                case Util.StandID.TWENTIETH_CENTURY_BOY: {
                    standName = "20th Century Boy";
                    break;
                }
                case Util.StandID.THE_GRATEFUL_DEAD: {
                    standName = "The Grateful Dead";
                    break;
                }
                case Util.StandID.STICKY_FINGERS: {
                    standName = "Sticky Fingers";
                    break;
                }
                case Util.StandID.TUSK_ACT_1: {
                    standName = "Tusk (Act 1)";
                    break;
                }
                case Util.StandID.TUSK_ACT_2: {
                    standName = "Tusk (Act 2)";
                    break;
                }
                case Util.StandID.TUSK_ACT_3: {
                    standName = "Tusk (Act 3)";
                    break;
                }
                case Util.StandID.TUSK_ACT_4: {
                    standName = "Tusk (Act 4)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_1: {
                    standName = "Echoes (Act 1)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_2: {
                    standName = "Echoes (Act 2)";
                    break;
                }
                case Util.StandID.ECHOES_ACT_3: {
                    standName = "Echoes (Act 3)";
                    break;
                }
                case Util.StandID.BEACH_BOY: {
                    standName = "Beach Boy";
                    break;
                }
            }
        if (!standName.equals(""))
            event.getToolTip().add(new StringTextComponent(standName));
    }

    @SubscribeEvent
    public static void throwawayEvent(ItemTossEvent event) {
        if (event.getEntityItem().getItem().getItem() == ItemInit.THE_EMPEROR.get() || event.getEntityItem().getItem().getItem() == ItemInit.BEACH_BOY.get()) {
            event.setCanceled(true);
            Stand.getLazyOptional(event.getPlayer()).ifPresent(props -> props.setStandOn(false));
        }
        if (event.getEntityItem().getItem().getOrCreateTag().getBoolean("bomb"))
            StandEffects.getLazyOptional(event.getEntityItem()).ifPresent(props -> {
                props.setBomb(true);
                props.setStandUser(event.getPlayer().getUniqueID());
            });
        StandEffects.getLazyOptional(event.getPlayer()).ifPresent(standEffects -> {
            if (standEffects.getBitesTheDustPos() != BlockPos.ZERO)
                StandEffects.getLazyOptional(event.getEntityItem()).ifPresent(item -> item.setShouldBeRemoved(true));
        });
    }

    @SubscribeEvent
    public static void standUnsummoned(StandEvent.StandUnsummonedEvent event) {
        PlayerEntity player = event.getPlayer();
        Stand.getLazyOptional(player).ifPresent(props -> {
            player.setInvulnerable(false);
            player.setNoGravity(false);
            if (!player.isCreative() && !player.isSpectator())
                player.setGameType(GameType.SURVIVAL);
            if (props.getStandID() == Util.StandID.THE_WORLD) {
                if (props.getAbility() && props.getTimeLeft() > 780)
                    player.world.playSound(null, player.getPosition(), SoundInit.RESUME_TIME.get(), SoundCategory.NEUTRAL, 5, 1);
                Entity theWorld = player.world.getEntityByID(props.getPlayerStand());
                if (theWorld instanceof TheWorldEntity) {
                    ((TheWorldEntity) theWorld).shouldDamageBeCancelled = false;
                    TheWorldEntity.getTheWorldList().remove(theWorld);
                    ((TheWorldEntity) theWorld).getBrokenBlocks().forEach(pos -> {
                        theWorld.world.getBlockState(pos).getBlock().harvestBlock(theWorld.world, player, pos, theWorld.world.getBlockState(pos), null, player.getActiveItemStack());
                        theWorld.world.removeBlock(pos, false);
                    });
                    ((TheWorldEntity) theWorld).getBrokenBlocks().clear();
                }
                TheWorldEntity.dayTime = -1;
                TheWorldEntity.gameTime = -1;

                if (!player.world.isRemote)
                    player.world.getServer().getWorld(player.dimension).getEntities()
                            .filter(entity -> entity != player)
                            .forEach(entity -> Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                                if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                    entity.setNoGravity(false);
                                } else if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                if (entity instanceof MobEntity)
                                    ((MobEntity) entity).setNoAI(false);
                                entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                entity.velocityChanged = true;
                                entity.fallDistance = props2.getFallDistance();
                                entity.setInvulnerable(false);
                                props2.getDamage().forEach((source, amount) -> {
                                    DamageSource damageSource = DamageSource.GENERIC;
                                    String newSource = source.replaceAll("[0123456789]", "");
                                    switch (newSource) {
                                        case "inFire": {
                                            damageSource = DamageSource.IN_FIRE;
                                            break;
                                        }
                                        case "onFire": {
                                            damageSource = DamageSource.ON_FIRE;
                                            break;
                                        }
                                        case "lightningBolt": {
                                            damageSource = DamageSource.LIGHTNING_BOLT;
                                            break;
                                        }
                                        case "lava": {
                                            damageSource = DamageSource.LAVA;
                                            break;
                                        }
                                        case "hotFloor": {
                                            damageSource = DamageSource.HOT_FLOOR;
                                            break;
                                        }
                                        case "inWall": {
                                            damageSource = DamageSource.IN_WALL;
                                            break;
                                        }
                                        case "cramming": {
                                            damageSource = DamageSource.CRAMMING;
                                            break;
                                        }
                                        case "drown": {
                                            damageSource = DamageSource.DROWN;
                                            break;
                                        }
                                        case "starve": {
                                            damageSource = DamageSource.STARVE;
                                            break;
                                        }
                                        case "cactus": {
                                            damageSource = DamageSource.CACTUS;
                                            break;
                                        }
                                        case "fall": {
                                            damageSource = DamageSource.FALL;
                                            break;
                                        }
                                        case "flyIntoWall": {
                                            damageSource = DamageSource.FLY_INTO_WALL;
                                            break;
                                        }
                                        case "outOfWorld": {
                                            damageSource = DamageSource.OUT_OF_WORLD;
                                            break;
                                        }
                                        case "magic": {
                                            damageSource = DamageSource.MAGIC;
                                            break;
                                        }
                                        case "wither": {
                                            damageSource = DamageSource.WITHER;
                                            break;
                                        }
                                        case "anvil": {
                                            damageSource = DamageSource.ANVIL;
                                            break;
                                        }
                                        case "fallingBlock": {
                                            damageSource = DamageSource.FALLING_BLOCK;
                                            break;
                                        }
                                        case "dragonBreath": {
                                            damageSource = DamageSource.DRAGON_BREATH;
                                            break;
                                        }
                                        case "fireworks": {
                                            damageSource = DamageSource.FIREWORKS;
                                            break;
                                        }
                                        case "dryout": {
                                            damageSource = DamageSource.DRYOUT;
                                            break;
                                        }
                                        case "sweetBerryBush": {
                                            damageSource = DamageSource.SWEET_BERRY_BUSH;
                                            break;
                                        }
                                    }
                                    entity.attackEntityFrom(damageSource, amount);
                                    entity.hurtResistantTime = 0;
                                });
                                props2.clear();
                            }));
            } else if (props.getStandID() == Util.StandID.STAR_PLATINUM) {
                if (props.getAbility() && props.getTimeLeft() > 900)
                    player.world.playSound(null, player.getPosition(), SoundInit.RESUME_TIME_STAR_PLATINUM.get(), SoundCategory.NEUTRAL, 5, 1);
                Entity starPlatinum = player.world.getEntityByID(props.getPlayerStand());
                if (starPlatinum instanceof StarPlatinumEntity) {
                    ((StarPlatinumEntity) starPlatinum).shouldDamageBeCancelled = false;
                    StarPlatinumEntity.getStarPlatinumList().remove(starPlatinum);
                    ((StarPlatinumEntity) starPlatinum).getBrokenBlocks().forEach(pos -> {
                        starPlatinum.world.getBlockState(pos).getBlock().harvestBlock(starPlatinum.world, player, pos, starPlatinum.world.getBlockState(pos), null, player.getActiveItemStack());
                        starPlatinum.world.removeBlock(pos, false);
                    });
                    ((StarPlatinumEntity) starPlatinum).getBrokenBlocks().clear();
                }
                StarPlatinumEntity.dayTime = -1;
                StarPlatinumEntity.gameTime = -1;
                if (!player.world.isRemote)
                    player.world.getServer().getWorld(player.dimension).getEntities()
                            .filter(entity -> entity != player)
                            .forEach(entity -> Timestop.getLazyOptional(entity).ifPresent(props2 -> {
                                if ((entity instanceof IProjectile || entity instanceof ItemEntity || entity instanceof DamagingProjectileEntity) && (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)) {
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                    entity.setNoGravity(false);
                                } else if (props2.getMotionX() != 0 && props2.getMotionY() != 0 && props2.getMotionZ() != 0)
                                    entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                if (entity instanceof MobEntity)
                                    ((MobEntity) entity).setNoAI(false);
                                entity.setMotion(props2.getMotionX(), props2.getMotionY(), props2.getMotionZ());
                                entity.velocityChanged = true;
                                entity.fallDistance = props2.getFallDistance();
                                entity.setInvulnerable(false);
                                if (props2.getDamage().size() > 0)
                                    props2.getDamage().forEach((source, amount) -> {
                                        DamageSource damageSource = DamageSource.GENERIC;
                                        String newSource = source.replaceAll("[0123456789]", "");
                                        switch (newSource) {
                                            case "inFire": {
                                                damageSource = DamageSource.IN_FIRE;
                                                break;
                                            }
                                            case "onFire": {
                                                damageSource = DamageSource.ON_FIRE;
                                                break;
                                            }
                                            case "lightningBolt": {
                                                damageSource = DamageSource.LIGHTNING_BOLT;
                                                break;
                                            }
                                            case "lava": {
                                                damageSource = DamageSource.LAVA;
                                                break;
                                            }
                                            case "hotFloor": {
                                                damageSource = DamageSource.HOT_FLOOR;
                                                break;
                                            }
                                            case "inWall": {
                                                damageSource = DamageSource.IN_WALL;
                                                break;
                                            }
                                            case "cramming": {
                                                damageSource = DamageSource.CRAMMING;
                                                break;
                                            }
                                            case "drown": {
                                                damageSource = DamageSource.DROWN;
                                                break;
                                            }
                                            case "starve": {
                                                damageSource = DamageSource.STARVE;
                                                break;
                                            }
                                            case "cactus": {
                                                damageSource = DamageSource.CACTUS;
                                                break;
                                            }
                                            case "fall": {
                                                damageSource = DamageSource.FALL;
                                                break;
                                            }
                                            case "flyIntoWall": {
                                                damageSource = DamageSource.FLY_INTO_WALL;
                                                break;
                                            }
                                            case "outOfWorld": {
                                                damageSource = DamageSource.OUT_OF_WORLD;
                                                break;
                                            }
                                            case "magic": {
                                                damageSource = DamageSource.MAGIC;
                                                break;
                                            }
                                            case "wither": {
                                                damageSource = DamageSource.WITHER;
                                                break;
                                            }
                                            case "anvil": {
                                                damageSource = DamageSource.ANVIL;
                                                break;
                                            }
                                            case "fallingBlock": {
                                                damageSource = DamageSource.FALLING_BLOCK;
                                                break;
                                            }
                                            case "dragonBreath": {
                                                damageSource = DamageSource.DRAGON_BREATH;
                                                break;
                                            }
                                            case "fireworks": {
                                                damageSource = DamageSource.FIREWORKS;
                                                break;
                                            }
                                            case "dryout": {
                                                damageSource = DamageSource.DRYOUT;
                                                break;
                                            }
                                            case "sweetBerryBush": {
                                                damageSource = DamageSource.SWEET_BERRY_BUSH;
                                                break;
                                            }
                                        }
                                        entity.attackEntityFrom(damageSource, amount);
                                        entity.hurtResistantTime = 0;
                                    });
                                props2.clear();
                            }));
            }
        });
    }

    @SubscribeEvent
    public static void standPunchEntityEvent(StandAttackEvent.EntityHit event) {
        if (!JojoBizarreSurvivalConfig.COMMON.standPunchDamage.get())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void standPunchBlockEvent(StandAttackEvent.BlockHit event) {
        if (!JojoBizarreSurvivalConfig.COMMON.standPunchBlockBreaking.get())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void cancelDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (TheWorldEntity.getTheWorldList().size() > 0)
            TheWorldEntity.getTheWorldList().forEach(theWorldEntity -> {
                if (theWorldEntity.shouldDamageBeCancelled) {
                    Timestop.getLazyOptional(entity).ifPresent(props -> {
                        if (!props.getDamage().containsKey(event.getSource().getDamageType()))
                            props.getDamage().put(event.getSource().getDamageType(), event.getAmount());
                        else
                            for (int i = 0; i < 1000; i++) {
                                if (!props.getDamage().containsKey(event.getSource().getDamageType() + i)) {
                                    props.getDamage().put(event.getSource().getDamageType() + i, event.getAmount());
                                    break;
                                }
                            }
                    });
                    event.setCanceled(true);
                }
            });
        if (StarPlatinumEntity.getStarPlatinumList().size() > 0)
            StarPlatinumEntity.getStarPlatinumList().forEach(starPlatinumEntity -> {
                if (starPlatinumEntity.shouldDamageBeCancelled) {
                    Timestop.getLazyOptional(entity).ifPresent(props -> {
                        if (!props.getDamage().containsKey(event.getSource().getDamageType()))
                            props.getDamage().put(event.getSource().getDamageType(), event.getAmount());
                        else
                            for (int i = 0; i < 1000; i++) {
                                if (!props.getDamage().containsKey(event.getSource().getDamageType() + i)) {
                                    props.getDamage().put(event.getSource().getDamageType() + i, event.getAmount());
                                    break;
                                }
                            }
                    });
                    event.setCanceled(true);
                }
            });
        if (entity instanceof PlayerEntity)
            Stand.getLazyOptional((PlayerEntity) entity).ifPresent(stand -> {
                if (stand.getStandID() == Util.StandID.KILLER_QUEEN && stand.getGameTime() != -1 && entity.getHealth() <= entity.getMaxHealth() / 4) {
                    entity.world.setGameTime(stand.getGameTime());
                    entity.world.setDayTime(stand.getDayTime());
                    stand.setGameTime(-1);
                    stand.setDayTime(-1);
                    entity.setHealth(entity.getMaxHealth());
                    entity.getServer().getWorld(entity.dimension).getEntities().forEach(entity1 -> {
                        if (entity1 instanceof PlayerEntity && !entity1.world.isRemote)
                            StandPlayerEffects.getLazyOptional((PlayerEntity) entity1).ifPresent(standPlayerEffects -> {
                                ((PlayerEntity) entity1).inventory.clear();
                                for (int i = 0; i < standPlayerEffects.getMainInventory().size(); i++) {
                                    ItemStack stack = standPlayerEffects.getMainInventory().get(i);
                                    ((PlayerEntity) entity1).inventory.setInventorySlotContents(i, stack);
                                    standPlayerEffects.getMainInventory().set(i, ItemStack.EMPTY);
                                }
                                for (int i = 0; i < standPlayerEffects.getArmorInventory().size(); i++) {
                                    ItemStack stack = standPlayerEffects.getArmorInventory().get(i);
                                    ((PlayerEntity) entity1).inventory.setInventorySlotContents(i + 36, stack);
                                    standPlayerEffects.getArmorInventory().set(i, ItemStack.EMPTY);
                                }
                                for (int i = 0; i < standPlayerEffects.getOffHandInventory().size(); i++) {
                                    ItemStack stack = standPlayerEffects.getOffHandInventory().get(i);
                                    ((PlayerEntity) entity1).inventory.setInventorySlotContents(i + 40, stack);
                                    standPlayerEffects.getOffHandInventory().set(i, ItemStack.EMPTY);
                                }
                            });
                        StandEffects.getLazyOptional(entity1).ifPresent(standEffects -> {
                            if (standEffects.isShouldBeRemoved())
                                entity1.remove();
                            if (entity1 instanceof ItemEntity && standEffects.getBitesTheDustPos() == BlockPos.ZERO)
                                entity.remove();
                            if (!standEffects.getDestroyedBlocks().isEmpty())
                                standEffects.getDestroyedBlocks().forEach((pos, list) ->
                                        list.forEach((blockPos, blockState) -> {
                                            if (entity.world.getChunkProvider().isChunkLoaded(pos))
                                                entity.world.getChunkProvider().forceChunk(pos, true);
                                            entity.world.setBlockState(blockPos, blockState);
                                        }));
                            if (standEffects.getBitesTheDustPos() != BlockPos.ZERO) {
                                entity1.setPositionAndUpdate(standEffects.getBitesTheDustPos().getX(), standEffects.getBitesTheDustPos().getY(), standEffects.getBitesTheDustPos().getZ());
                                standEffects.setBitesTheDustPos(BlockPos.ZERO);
                            }
                        });
                    });
                }
                if (stand.getStandID() == Util.StandID.TWENTIETH_CENTURY_BOY && stand.getAbilityActive()) {
                    if (!entity.world.isRemote)
                        entity.getServer().getWorld(entity.dimension).getEntities()
                                .filter(entity1 -> entity1.getDistance(entity) <= 3)
                                .filter(entity1 -> !entity1.equals(entity))
                                .forEach(entity1 -> {
                                    if (entity1 instanceof AbstractStandEntity && !((AbstractStandEntity) entity1).getMaster().equals(entity))
                                        return;
                                    entity1.attackEntityFrom(event.getSource(), event.getAmount() / 1.4f);
                                });
                    event.setCanceled(true);
                } else if (stand.getInvulnerableTicks() > 0) {
                    event.setCanceled(true);
                    Entity source = event.getSource().getTrueSource();
                    if (source != null) {
                        Vec3d pos = source.getLookVec().mul(-0.5, 1, -0.5).add(source.getPositionVec());
                        if (!entity.world.isRemote) {
                            entity.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                            entity.lookAt(EntityAnchorArgument.Type.FEET, source.getPositionVec());
                        }
                        switch (stand.getStandID()) {
                            case Util.StandID.KING_CRIMSON: {
                                source.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1);
                                entity.world.playSound(null, entity.getPosition(), SoundInit.SPAWN_KING_CRIMSON.get(), SoundCategory.VOICE, 1, 1);
                                break;
                            }
                            case Util.StandID.STAR_PLATINUM:
                            case Util.StandID.THE_WORLD: {
                                source.attackEntityFrom(DamageSource.OUT_OF_WORLD, 1);
                                entity.world.playSound(null, entity.getPosition(), SoundInit.THE_WORLD_TELEPORT.get(), SoundCategory.HOSTILE, 1, 1);
                                break;
                            }
                            case Util.StandID.MADE_IN_HEAVEN: {
                                source.attackEntityFrom(DamageSource.OUT_OF_WORLD, 5);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            });
    }

    @SubscribeEvent
    public static void itemPickup(EntityItemPickupEvent event) {
        ItemEntity entity = event.getItem();
        if (entity == null) return;
        if (entity.world.isRemote) return;
        StandEffects.getLazyOptional(entity).ifPresent(props -> {
            if (props.isBomb()) {
                entity.world.createExplosion(entity, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 2.3f, Explosion.Mode.DESTROY);
                PlayerEntity player = entity.world.getPlayerByUuid(props.getStandUser());
                if (player != null)
                    Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(0));
                entity.remove();
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void itemExpire(ItemExpireEvent event) {
        ItemEntity entity = event.getEntityItem();
        if (entity == null) return;
        if (entity.getItem().getOrCreateTag().getBoolean("bomb"))
            StandEffects.getLazyOptional(entity).ifPresent(props -> {
                PlayerEntity player = entity.world.getPlayerByUuid(props.getStandUser());
                Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(0));
            });
    }

    @SubscribeEvent
    public static void noClip(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity)
            Stand.getLazyOptional((PlayerEntity) event.getEntityLiving()).ifPresent(props -> {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                if (!player.isSpectator())
                    player.noClip = props.getNoClip();
                else
                    player.noClip = true;
            });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getWorld() instanceof World)) return;
        World world = (World) event.getWorld();
        if (world.isRemote) return;
        Chunk chunk = world.getChunkAt(event.getPos());
        StandChunkEffects.getLazyOptional(chunk).ifPresent(props -> {
            props.getBombs().forEach((uuid, blockPos) -> {
                if (blockPos.equals(event.getPos())) {
                    PlayerEntity player = world.getPlayerByUuid(uuid);
                    world.createExplosion(null, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), 3, Explosion.Mode.DESTROY);
                    Stand.getLazyOptional(player).ifPresent(stand -> {
                        stand.setBlockPos(BlockPos.ZERO);
                        stand.setAbilityUseCount(0);
                    });
                    props.removeBombPos(player);
                }
            });
            props.getSoundEffects().forEach((uuid, list) -> {
                if (list.size() > 0) {
                    List<BlockPos> removalList = new ArrayList<>();
                    list.forEach(blockPos -> {
                        if (blockPos.equals(event.getPos())) {
                            PlayerEntity player = world.getPlayerByUuid(uuid);
                            Util.activateEchoesAbility(world, event.getPlayer(), event.getPos(), props, player, removalList);
                        }
                    });
                    list.removeAll(removalList);
                }
            });
        });
    }

    @SubscribeEvent
    public static void explosionEvent(ExplosionEvent.Detonate event) {
        World world = event.getWorld();
        if (world == null || world.isRemote) return;
        event.getAffectedBlocks().forEach(blockPos ->
                StandChunkEffects.getLazyOptional(world.getChunkAt(blockPos)).ifPresent(props -> {
                    props.getBombs().forEach((uuid, pos) -> {
                        if (pos.equals(blockPos)) {
                            PlayerEntity player = world.getPlayerByUuid(uuid);
                            Stand.getLazyOptional(player).ifPresent(stand -> {
                                stand.setBlockPos(BlockPos.ZERO);
                                stand.setAbilityUseCount(0);
                            });
                            props.removeBombPos(player);
                        }
                    });
                    props.getSoundEffects().forEach((uuid, list) -> {
                        if (list.size() > 0) {
                            List<BlockPos> removalList = new ArrayList<>();
                            list.forEach(pos -> {
                                if (pos.equals(blockPos)) {
                                    PlayerEntity player = world.getPlayerByUuid(uuid);
                                    Stand.getLazyOptional(player).ifPresent(stand -> stand.setAbilityUseCount(stand.getAbilityUseCount() - 1));
                                    removalList.add(pos);
                                }
                            });
                            list.removeAll(removalList);
                        }
                    });
                }));
    }

    @SubscribeEvent
    public static void playerEatEvent(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.world.isRemote) return;
        if (event.getItem().getItem() instanceof HoneyBottleItem)
            Stand.getLazyOptional(player).ifPresent(props -> {
                if ((props.getStandID() == Util.StandID.TUSK_ACT_2 || props.getStandID() == Util.StandID.TUSK_ACT_3 || props.getStandID() == Util.StandID.TUSK_ACT_4) && props.getCooldown() > 0)
                    props.setCooldown(props.getCooldown() > 40 ? props.getCooldown() - 40 : 1);
            });
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof AbstractStandEntity) return;
        StandEffects.getLazyOptional(entity).ifPresent(props -> {
            if (props.getSoundEffect() != 0) {
                if (entity.world.rand.nextInt(40) == 1) {
                    SoundEvent soundEvent = SoundEvents.ENTITY_GENERIC_EXPLODE;
                    switch (props.getSoundEffect()) {
                        case 1: {
                            soundEvent = SoundEvents.BLOCK_ANVIL_BREAK;
                            break;
                        }
                        case 2: {
                            soundEvent = SoundEvents.BLOCK_BELL_USE;
                            break;
                        }
                        case 3: {
                            soundEvent = SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST;
                            break;
                        }
                        case 4: {
                            soundEvent = SoundEvents.ENTITY_GENERIC_BURN;
                            break;
                        }
                        case 6: {
                            soundEvent = SoundEvents.BLOCK_GLASS_BREAK;
                            break;
                        }
                        case 7: {
                            soundEvent = SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON;
                            break;
                        }
                    }
                    if (props.getSoundEffect() != 2 && props.getSoundEffect() % 2 == 0) {
                        for (int i = 0; i < 2; i++) {
                            entity.world.playSound(null, entity.getPosition(), soundEvent, SoundCategory.BLOCKS, 1, 1);
                            entity.attackEntityFrom(DamageSource.DROWN, 2);
                        }
                    } else {
                        entity.world.playSound(null, entity.getPosition(), soundEvent, SoundCategory.BLOCKS, 1, 1);
                        entity.attackEntityFrom(DamageSource.DROWN, 2);
                    }
                }
            }
            if (props.isRotating()) {
                entity.rotationYaw += 30;
                if (entity.rotationYaw > 180)
                    entity.rotationYaw = -180;
                entity.attackEntityFrom(DamageSource.OUT_OF_WORLD, 2);
            }
            if (props.isThreeFreeze()) {
                PlayerEntity playerEntity = entity.world.getPlayerByUuid(props.getStandUser());
                if (playerEntity == null || !playerEntity.isAlive()) return;
                float distance = entity.getDistance(playerEntity);
                if (distance < 2)
                    entity.setMotion(0, -1000, 0);
                else if (distance < 3)
                    entity.setMotion(0, -100, 0);
                else if (distance < 6)
                    entity.setMotion(0, -50, 0);
                else if (distance < 10)
                    entity.setMotion(0, -10, 0);
                else if (distance < 15)
                    entity.setMotion(0, -5, 0);
                else
                    return;
                entity.velocityChanged = true;
            }
            if (props.getTimeNearFlames() > 0) {
                entity.setFireTimer((int) (props.getTimeNearFlames() * 2));
                props.setTimeNearFlames(props.getTimeNearFlames() - 0.25);
                if (entity.world.rand.nextInt(6) == 1)
                    entity.attackEntityFrom(DamageSource.IN_FIRE, 2);
            }
            if (props.getStandUser() != null && entity.world.getPlayerByUuid(props.getStandUser()) != null && entity.world.getPlayerByUuid(props.getStandUser()).isAlive() && props.getTimeOfDeath() != -1 && props.getTimeOfDeath() <= entity.world.getGameTime()) {
                if (entity instanceof MobEntity) {
                    Explosion explosion = new Explosion(entity.world, entity.world.getPlayerByUuid(props.getStandUser()), entity.getPosX(), entity.getPosY(), entity.getPosZ(), 4, true, Explosion.Mode.NONE);
                    ((MobEntity) entity).spawnExplosionParticle();
                    explosion.doExplosionB(true);
                    entity.world.playSound(null, entity.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                    entity.remove();
                } else if (entity instanceof PlayerEntity) {
                    Stand.getLazyOptional((PlayerEntity) entity).ifPresent(bombProps -> {
                        if (bombProps.getStandID() != Util.StandID.GER) {
                            Explosion explosion = new Explosion(entity.world, null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 4, true, Explosion.Mode.NONE);
                            ((PlayerEntity) entity).spawnSweepParticles();
                            explosion.doExplosionB(true);
                            entity.attackEntityFrom(DamageSource.FIREWORKS, Float.MAX_VALUE);
                        }
                    });
                }
            }
        });
        StandChunkEffects.getLazyOptional(entity.world.getChunkAt(entity.getPosition())).ifPresent(props ->
                props.getSoundEffects().forEach((uuid, list) -> {
                    if (list.size() > 0) {
                        List<BlockPos> removalList = new ArrayList<>();
                        list.forEach(blockPos -> {
                            if (blockPos.equals(entity.getPosition().add(0, -1, 0)))
                                Util.activateEchoesAbility(entity.world, entity, blockPos, props, entity.world.getPlayerByUuid(uuid), removalList);
                        });
                        list.removeAll(removalList);
                    }
                }));
    }
}