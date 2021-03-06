package io.github.novarch129.jojomod.entity.stand;

import io.github.novarch129.jojomod.capability.*;
import io.github.novarch129.jojomod.entity.stand.attack.KillerQueenPunchEntity;
import io.github.novarch129.jojomod.init.SoundInit;
import io.github.novarch129.jojomod.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ConstantConditions")
public class KillerQueenEntity extends AbstractStandEntity {
    public LivingEntity bombEntity;
    protected int shaCount;
    private SheerHeartAttackEntity sheerHeartAttack;

    public KillerQueenEntity(EntityType<? extends AbstractStandEntity> type, World world) {
        super(type, world);
    }

    @Override
    public SoundEvent getSpawnSound() {
        return SoundInit.SPAWN_KILLER_QUEEN.get();
    }

    public void detonate() {
        if (getMaster() == null) return;
        Stand stand = Stand.getCapabilityFromPlayer(master);
        if (stand.getCooldown() == 0 && master.isCrouching() && !world.isRemote && stand.getAbilitiesUnlocked() > 1 && master.dimension == DimensionType.OVERWORLD) {
            if (master.isCrouching() && stand.getGameTime() == -1) {
                stand.setGameTime(world.getGameTime());
                stand.setDayTime(world.getDayTime());
                world.loadedTileEntityList
                        .forEach(tileEntity -> StandTileEntityEffects.getLazyOptional(tileEntity).ifPresent(standTileEntityEffects -> {
                            if (tileEntity instanceof ChestTileEntity)
                                for (int i = 0; i < ((ChestTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((ChestTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getChestInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof AbstractFurnaceTileEntity)
                                for (int i = 0; i < ((AbstractFurnaceTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((AbstractFurnaceTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getFurnaceInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof BrewingStandTileEntity)
                                for (int i = 0; i < ((BrewingStandTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((BrewingStandTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getBrewingInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof BarrelTileEntity)
                                for (int i = 0; i < ((BarrelTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((BarrelTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getBarrelInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof DispenserTileEntity)
                                for (int i = 0; i < ((DispenserTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((DispenserTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getDispenserInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof HopperTileEntity)
                                for (int i = 0; i < ((HopperTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((HopperTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getHopperInventory().set(i, stack.copy());
                                }
                            else if (tileEntity instanceof ShulkerBoxTileEntity)
                                for (int i = 0; i < ((ShulkerBoxTileEntity) tileEntity).getSizeInventory(); i++) {
                                    ItemStack stack = ((ShulkerBoxTileEntity) tileEntity).getStackInSlot(i);
                                    if (!stack.isEmpty())
                                        standTileEntityEffects.getShulkerBoxInventory().set(i, stack.copy());
                                }
                        }));
                getServer().getWorld(dimension).getEntities().forEach(entity -> {
                    if (entity instanceof PlayerEntity)
                        StandPlayerEffects.getLazyOptional((PlayerEntity) entity).ifPresent(standPlayerEffects -> {
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.mainInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.mainInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getMainInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.armorInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.armorInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getArmorInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).inventory.offHandInventory.size(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).inventory.offHandInventory.get(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getOffHandInventory().set(i, stack.copy());
                            }
                            for (int i = 0; i < ((PlayerEntity) entity).getInventoryEnderChest().getSizeInventory(); i++) {
                                ItemStack stack = ((PlayerEntity) entity).getInventoryEnderChest().getStackInSlot(i);
                                if (!stack.isEmpty())
                                    standPlayerEffects.getEnderChestInventory().set(i, stack.copy());
                            }
                        });
                    StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                        standEffects.setBitesTheDustPos(entity.getPosition());
                        standEffects.setStandUser(master.getUniqueID());
                    });
                });
                master.sendStatusMessage(new StringTextComponent("Set checkpoint for\u00A7e Bites the Dust\u00A7f!"), true);
            } else if (stand.getGameTime() != -1) {
                world.setGameTime(stand.getGameTime());
                world.setDayTime(stand.getDayTime());
                stand.setGameTime(-1);
                stand.setDayTime(-1);
                master.setHealth(master.getMaxHealth());
                world.loadedTileEntityList.stream()
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
                getServer().getWorld(dimension).getEntities().forEach(entity -> {
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
                            for (int i = 0; i < standPlayerEffects.getEnderChestInventory().size(); i++) {
                                ItemStack stack = standPlayerEffects.getEnderChestInventory().get(i);
                                ((PlayerEntity) entity).getInventoryEnderChest().setInventorySlotContents(i, stack);
                                standPlayerEffects.getEnderChestInventory().set(i, ItemStack.EMPTY);
                            }
                        });
                    StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                        if (!standEffects.getAlteredTileEntities().isEmpty())
                            standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                    blockPosList.forEach(blockPos -> {
                                        if (world.getChunkProvider().isChunkLoaded(pos))
                                            world.getChunkProvider().forceChunk(pos, true);
                                        TileEntity tileEntity = world.getTileEntity(blockPos);
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
                                    if (world.getChunkProvider().isChunkLoaded(pos))
                                        world.getChunkProvider().forceChunk(pos, true);
                                    world.setBlockState(blockPos, blockState);
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
            return;
        }
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (world.getBlockState(stand.getBlockPos()).isAir(world, stand.getBlockPos())) {
                stand.setBlockPos(BlockPos.ZERO);
                StandChunkEffects.getLazyOptional(world.getChunkAt(master.getPosition())).ifPresent(standChunkEffects -> standChunkEffects.removeBombPos(master));
                stand.setAbilityUseCount(0);
            }
            if (stand.getCooldown() <= 0) {
                if (!world.isRemote)
                    getServer().getWorld(dimension).getEntities()
                            .filter(entity -> entity instanceof ItemEntity)
                            .forEach(entity ->
                                    StandEffects.getLazyOptional(entity).ifPresent(effects -> {
                                        if (effects.isBomb()) {
                                            PlayerEntity player = world.getPlayerByUuid(effects.getStandUser());
                                            if (player != null && player.equals(master)) {
                                                entity.world.createExplosion(entity, entity.getPosX(), entity.getPosY(), entity.getPosZ(), 2.3f, Explosion.Mode.DESTROY);
                                                stand.setAbilityUseCount(0);
                                                entity.remove();
                                            }
                                        }
                                    })
                            );
                if (stand.getBlockPos() != BlockPos.ZERO) {
                    if (!world.getChunkProvider().isChunkLoaded(world.getChunkAt(stand.getBlockPos()).getPos())) return;
                    world.createExplosion(this, stand.getBlockPos().getX(), stand.getBlockPos().getY(), stand.getBlockPos().getZ(), 3, Explosion.Mode.DESTROY);
                    StandChunkEffects.getLazyOptional(world.getChunkAt(master.getPosition())).ifPresent(standChunkEffects -> standChunkEffects.removeBombPos(master));
                    stand.setBlockPos(BlockPos.ZERO);
                    stand.setAbilityUseCount(0);
                }
                if (bombEntity != null) {
                    if (bombEntity.isAlive()) {
                        stand.setCooldown(140);
                        if (bombEntity instanceof MobEntity) {
                            Explosion explosion = new Explosion(bombEntity.world, master, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 4, true, Explosion.Mode.NONE);
                            ((MobEntity) bombEntity).spawnExplosionParticle();
                            explosion.doExplosionB(true);
                            world.playSound(null, master.getPosition(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1, 1);
                            if (stand.getGameTime() == -1)
                                bombEntity.remove();
                            else {
                                StandEffects.getLazyOptional(bombEntity).ifPresent(standEffects -> standEffects.setTimeOfDeath(world.getGameTime()));
                                world.setGameTime(stand.getGameTime());
                                world.setDayTime(stand.getDayTime());
                                stand.setGameTime(-1);
                                stand.setDayTime(-1);
                                master.setHealth(master.getMaxHealth());
                                world.loadedTileEntityList.stream()
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
                                getServer().getWorld(dimension).getEntities().forEach(entity -> {
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
                                            for (int i = 0; i < standPlayerEffects.getEnderChestInventory().size(); i++) {
                                                ItemStack stack = standPlayerEffects.getEnderChestInventory().get(i);
                                                ((PlayerEntity) entity).getInventoryEnderChest().setInventorySlotContents(i, stack);
                                                standPlayerEffects.getEnderChestInventory().set(i, ItemStack.EMPTY);
                                            }
                                        });
                                    StandEffects.getLazyOptional(entity).ifPresent(standEffects -> {
                                        if (!standEffects.getAlteredTileEntities().isEmpty())
                                            standEffects.getAlteredTileEntities().forEach((pos, blockPosList) ->
                                                    blockPosList.forEach(blockPos -> {
                                                        if (world.getChunkProvider().isChunkLoaded(pos))
                                                            world.getChunkProvider().forceChunk(pos, true);
                                                        TileEntity tileEntity = world.getTileEntity(blockPos);
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
                                                    if (world.getChunkProvider().isChunkLoaded(pos))
                                                        world.getChunkProvider().forceChunk(pos, true);
                                                    world.setBlockState(blockPos, blockState);
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
                        } else if (bombEntity instanceof PlayerEntity) {
                            Stand.getLazyOptional((PlayerEntity) bombEntity).ifPresent(bombProps -> {
                                if (bombProps.getStandID() != Util.StandID.GER) {
                                    Explosion explosion = new Explosion(bombEntity.world, master, bombEntity.getPosX(), bombEntity.getPosY(), bombEntity.getPosZ(), 4, true, Explosion.Mode.NONE);
                                    ((PlayerEntity) bombEntity).spawnSweepParticles();
                                    explosion.doExplosionB(true);
                                    bombEntity.attackEntityFrom(DamageSource.FIREWORKS, 15);
                                } else {
                                    Explosion explosion = new Explosion(master.world, master, master.getPosX(), master.getPosY(), master.getPosZ(), 4, true, Explosion.Mode.NONE);
                                    if (master.world.isRemote) {
                                        for (int i = 0; i < 20; ++i) {
                                            double d0 = master.world.rand.nextGaussian() * 0.02;
                                            double d1 = master.world.rand.nextGaussian() * 0.02;
                                            double d2 = master.world.rand.nextGaussian() * 0.02;
                                            master.world.addParticle(ParticleTypes.POOF, master.getPosXWidth(1) - d0 * 10, master.getPosYRandom() - d1 * 10, master.getPosZRandom(1) - d2 * 10, d0, d1, d2);
                                        }
                                    } else
                                        master.world.setEntityState(master, (byte) 20);
                                    explosion.doExplosionB(true);
                                    master.setHealth(0);
                                }
                            });
                        }
                        if (!master.isCreative() && !master.isSpectator())
                            master.getFoodStats().addStats(-2, 0);
                    }
                }
            }
        });
    }

    public void toggleSheerHeartAttack() {
        if (getMaster() == null || world.isRemote) return;
        if (sheerHeartAttack == null)
            sheerHeartAttack = new SheerHeartAttackEntity(world, this);
        Stand.getLazyOptional(getMaster()).ifPresent(stand -> {
            if (shaCount <= 0) {
                sheerHeartAttack.setPosition(getPosX(), getPosY(), getPosZ());
                world.addEntity(sheerHeartAttack);
                shaCount++;
                stand.setCooldown(300);
            } else {
                if (!world.isRemote)
                    world.getServer().getWorld(dimension).getEntities()
                            .filter(entity -> entity instanceof SheerHeartAttackEntity)
                            .filter(entity -> ((SheerHeartAttackEntity) entity).getMaster().getEntityId() == getMaster().getEntityId())
                            .forEach(Entity::remove);
            }
        });
    }

    public void turnItemOrBlockIntoBomb() {
        if (getMaster() == null || world.isRemote) return;
        Stand.getLazyOptional(master).ifPresent(props -> {
            if (!master.isCrouching() && master.getHeldItemMainhand() != ItemStack.EMPTY && props.getAbilityUseCount() == 0) {
                if (master.getHeldItemMainhand().getCount() > 1) {
                    if (master.inventory.getStackInSlot(master.inventory.getBestHotbarSlot()).isEmpty()) {
                        ItemStack stack = master.getHeldItemMainhand().copy();
                        stack.shrink(master.getHeldItemMainhand().getCount() - 1);
                        stack.getOrCreateTag().putBoolean("bomb", true);
                        master.getHeldItemMainhand().shrink(1);
                        stack.setDisplayName(new StringTextComponent("Bomb"));
                        master.inventory.add(master.inventory.getBestHotbarSlot(), stack);
                        master.sendStatusMessage(new StringTextComponent("Killer Queen has turned 1 " + new TranslationTextComponent(master.getHeldItemMainhand().getTranslationKey()).getFormattedText() + " into a bomb."), true);
                        props.setAbilityUseCount(1);
                    } else
                        master.sendStatusMessage(new StringTextComponent("Your hotbar is full!"), true);
                } else if (master.getHeldItemMainhand().getCount() == 1) {
                    master.getHeldItemMainhand().getOrCreateTag().putBoolean("bomb", true);
                    master.getHeldItemMainhand().setDisplayName(new StringTextComponent("Bomb"));
                    master.sendStatusMessage(new StringTextComponent("Killer Queen has turned your " + new TranslationTextComponent(master.getHeldItemMainhand().getTranslationKey()).getFormattedText() + " into a bomb."), true);
                    props.setAbilityUseCount(1);
                }
            } else if (master.isCrouching() && props.getAbilityUseCount() == 0) {
                BlockPos position = master.getPosition().add(0, -1, 0);
                props.setBlockPos(position);
                StandChunkEffects.getLazyOptional(world.getChunkAt(master.getPosition())).ifPresent(standChunkEffects -> standChunkEffects.addBombPos(master, position));
                master.sendStatusMessage(new StringTextComponent("Killer Queen has turned the block at X" + position.getX() + " Y" + position.getY() + " Z" + position.getZ() + " into a bomb."), true);
                props.setAbilityUseCount(1);
            }
        });
    }

    @Override
    public void attack(boolean special) {
        if (getMaster() == null) return;
        attackTick++;
        if (attackTick == 1)
            if (special)
                attackRush = true;
            else {
                world.playSound(null, getPosition(), SoundInit.PUNCH_MISS.get(), SoundCategory.NEUTRAL, 1, 0.6f / (rand.nextFloat() * 0.3f + 1) * 2);
                KillerQueenPunchEntity killerQueenPunchEntity = new KillerQueenPunchEntity(world, this, master);
                killerQueenPunchEntity.shoot(getMaster(), rotationPitch, rotationYaw, 2, 0.3f);
                world.addEntity(killerQueenPunchEntity);
            }
    }

    @Override
    public void tick() {
        super.tick();
        if (getMaster() != null) {
            Stand.getLazyOptional(master).ifPresent(stand -> stand.setAbility(false));

            followMaster();
            setRotationYawHead(master.rotationYawHead);
            setRotation(master.rotationYaw, master.rotationPitch);

            if (master.swingProgressInt == 0 && !attackRush)
                attackTick = 0;
            if (attackRush) {
                master.setSprinting(false);
                attackTicker++;
                if (attackTicker >= 10)
                    if (!world.isRemote) {
                        master.setSprinting(false);
                        KillerQueenPunchEntity killerQueen1 = new KillerQueenPunchEntity(world, this, master);
                        killerQueen1.randomizePositions();
                        killerQueen1.shoot(master, master.rotationPitch, master.rotationYaw, 2, 0.4f);
                        world.addEntity(killerQueen1);
                        KillerQueenPunchEntity killerQueen2 = new KillerQueenPunchEntity(world, this, master);
                        killerQueen2.randomizePositions();
                        killerQueen2.shoot(master, master.rotationPitch, master.rotationYaw, 2, 0.4f);
                        world.addEntity(killerQueen2);
                    }
                if (attackTicker >= 80) {
                    attackRush = false;
                    attackTicker = 0;
                }
            }
        }
    }
}