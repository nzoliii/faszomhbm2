package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class TileEntityMachineAutosaw extends TileEntityLoadedBase
    implements IBufPacketReceiver, IFluidStandardReceiver, IFluidCopiable, ITickable {

  public static final HashSet<FluidType> acceptedFuels = new HashSet<>();

  static {
    acceptedFuels.add(Fluids.WOODOIL);
    acceptedFuels.add(Fluids.ETHANOL);
    acceptedFuels.add(Fluids.FISHOIL);
    acceptedFuels.add(Fluids.HEAVYOIL);
  }

  public FluidTankNTM tank;

  public boolean isOn;
  public boolean isSuspended;
  private int forceSkip;
  public float syncYaw;
  public float rotationYaw;
  public float prevRotationYaw;
  public float syncPitch;
  public float rotationPitch;
  public float prevRotationPitch;

  // 0: searching, 1: extending, 2: retracting
  private int state = 0;

  private int turnProgress;

  public float spin;
  public float lastSpin;

  public TileEntityMachineAutosaw() {
    this.tank = new FluidTankNTM(Fluids.WOODOIL, 100);
  }

  @Override
  public void update() {

    if (!world.isRemote) {

      if (!isSuspended && world.getTotalWorldTime() % 20 == 0) {
        if (tank.getFill() > 0) {
          tank.setFill(tank.getFill() - 1);
          this.isOn = true;
        } else {
          this.isOn = false;
        }

        this.subscribeToAllAround(tank.getTankType(), this);
      }

      if (isOn && !isSuspended) {
        Vec3d pivot = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5);
        Vec3d upperArm = new Vec3d(0, 0, -4);
        upperArm = upperArm.rotatePitch((float) Math.toRadians(80 - rotationPitch));
        upperArm = upperArm.rotateYaw(-(float) Math.toRadians(rotationYaw));
        Vec3d lowerArm = new Vec3d(0, 0, -4);
        lowerArm = lowerArm.rotatePitch((float) -Math.toRadians(80 - rotationPitch));
        lowerArm = lowerArm.rotateYaw(-(float) Math.toRadians(rotationYaw));
        Vec3d armTip = new Vec3d(0, 0, -2);
        armTip = armTip.rotateYaw(-(float) Math.toRadians(rotationYaw));

        double cX = pivot.x + upperArm.x + lowerArm.x + armTip.x;
        double cY = pivot.y;
        double cZ = pivot.z + upperArm.z + lowerArm.z + armTip.z;

        List<EntityLivingBase> affected =
            world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                new AxisAlignedBB(cX - 1, cY - 0.25, cZ - 1, cX + 1, cY + 0.25, cZ + 1));

        for (EntityLivingBase e : affected) {
          if (e.isEntityAlive() && e.attackEntityFrom(ModDamageSource.turbofan, 100)) {
            world.playSound(
                null,
                e.posX,
                e.posY,
                e.posZ,
                SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD,
                SoundCategory.HOSTILE,
                2.0F,
                0.95F + world.rand.nextFloat() * 0.2F);
            int count = Math.min((int) Math.ceil(e.getMaxHealth() / 4), 250);
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaburst");
            data.setInteger("count", count * 4);
            data.setDouble("motion", 0.1D);
            data.setString("mode", "blockdust");
            data.setInteger(
                "block", Block.getIdFromBlock(Objects.requireNonNull(Blocks.REDSTONE_BLOCK)));
            PacketThreading.createAllAroundThreadedPacket(
                new AuxParticlePacketNT(data, e.posX, e.posY + e.height * 0.5, e.posZ),
                new TargetPoint(e.dimension, e.posX, e.posY, e.posZ, 50));
          }
        }

        if (state == 0) {

          this.rotationYaw += 1;

          if (this.rotationYaw >= 360) {
            this.rotationYaw -= 360;
          }

          if (forceSkip > 0) {
            forceSkip--;
          } else {
            final double CUT_ANGLE = Math.toRadians(5);
            double rotationYawRads = Math.toRadians((rotationYaw + 270) % 360);

            outer:
            for (int dx = -9; dx <= 9; dx++) {
              for (int dz = -9; dz <= 9; dz++) {
                int sqrDst = dx * dx + dz * dz;

                if (sqrDst <= 4 || sqrDst > 81) continue;

                double angle = Math.atan2(dz, dx);
                double relAngle = Math.abs(angle - rotationYawRads);
                relAngle = Math.abs((relAngle + Math.PI) % (2 * Math.PI) - Math.PI);

                if (relAngle > CUT_ANGLE) continue;

                int x = pos.getX() + dx;
                int y = pos.getY() + 1;
                int z = pos.getZ() + dz;

                IBlockState blockState = world.getBlockState(new BlockPos(x, y, z));
                Block block = blockState.getBlock();
                if (!(blockState.getMaterial() == Material.WOOD
                    || blockState.getMaterial() == Material.LEAVES
                    || blockState.getMaterial() == Material.PLANTS)) continue;

                int meta = block.getMetaFromState(blockState);
                if (shouldIgnore(world, x, y, z, block, meta)) continue;

                state = 1;
                break outer;
              }
            }
          }
        }

        int hitY = (int) Math.floor(cY);
        int hitX0 = (int) Math.floor(cX - 0.5);
        int hitZ0 = (int) Math.floor(cZ - 0.5);
        int hitX1 = (int) Math.floor(cX + 0.5);
        int hitZ1 = (int) Math.floor(cZ + 0.5);

        this.tryInteract(hitX0, hitY, hitZ0);
        this.tryInteract(hitX1, hitY, hitZ0);
        this.tryInteract(hitX0, hitY, hitZ1);
        this.tryInteract(hitX1, hitY, hitZ1);

        if (state == 1) {
          this.rotationPitch += 2;

          if (this.rotationPitch > 80) {
            this.rotationPitch = 80;
            state = 2;
          }
        }

        if (state == 2) {
          this.rotationPitch -= 2;

          if (this.rotationPitch <= 0) {
            this.rotationPitch = 0;
            state = 0;
          }
        }
      }

      PacketThreading.createAllAroundThreadedPacket(
          new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this),
          new TargetPoint(
              this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
    } else {

      this.lastSpin = this.spin;

      if (isOn && !isSuspended) {
        this.spin += 15F;

        Vec3d vec = new Vec3d(0.625, 0, 1.625);
        vec = vec.rotateYaw(-(float) Math.toRadians(rotationYaw));

        world.spawnParticle(
            EnumParticleTypes.SMOKE_NORMAL,
            pos.getX() + 0.5 + vec.x,
            pos.getY() + 2.0625,
            pos.getZ() + 0.5 + vec.z,
            0,
            0,
            0);
      }

      if (this.spin >= 360F) {
        this.spin -= 360F;
        this.lastSpin -= 360F;
      }

      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;

      if (this.turnProgress > 0) {
        double d0 = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
        double d1 = MathHelper.wrapDegrees(this.syncPitch - (double) this.rotationPitch);
        this.rotationYaw = (float) ((double) this.rotationYaw + d0 / (double) this.turnProgress);
        this.rotationPitch =
            (float) ((double) this.rotationPitch + d1 / (double) this.turnProgress);
        --this.turnProgress;
      } else {
        this.rotationYaw = this.syncYaw;
        this.rotationPitch = this.syncPitch;
      }
    }
  }

  /**
   * Anything additionally that the detector nor the blades should pick up on, like non-mature
   * willows
   */
  public static boolean shouldIgnore(World world, int x, int y, int z, Block block, int meta) {
    if (block == ModBlocks.plant_tall) {
      return meta == EnumTallFlower.CD2.ordinal() + 8 || meta == EnumTallFlower.CD3.ordinal() + 8;
    }

    BlockPos pos = new BlockPos(x, y, z);

    if ((block instanceof IGrowable)) {
      return ((IGrowable) block).canGrow(world, pos, world.getBlockState(pos), world.isRemote);
    }

    return false;
  }

  protected void tryInteract(int x, int y, int z) {

    BlockPos pos = new BlockPos(x, y, z);
    IBlockState blockState = world.getBlockState(pos);
    Block block = blockState.getBlock();
    int meta = block.getMetaFromState(blockState);

    if (!shouldIgnore(world, x, y, z, block, meta)) {
      if (blockState.getMaterial() == Material.LEAVES
          || blockState.getMaterial() == Material.PLANTS) {
        cutCrop(x, y, z);
      } else if (blockState.getMaterial() == Material.WOOD) {
        fellTree(x, y, z);
        if (state == 1) {
          state = 2;
        }
      }
    }

    // Return when hitting a wall
    if (state == 1 && world.getBlockState(new BlockPos(x, y, z)).isNormalCube()) {
      state = 2;
      forceSkip = 5;
    }
  }

  protected void cutCrop(int x, int y, int z) {

    BlockPos pos = new BlockPos(x, y, z);
    BlockPos soilPos = pos.down();
    IBlockState soilState = world.getBlockState(soilPos);
    Block soil = soilState.getBlock();

    IBlockState blockState = world.getBlockState(pos);
    Block block = blockState.getBlock();
    int meta = block.getMetaFromState(blockState);

    world.playEvent(2001, pos, Block.getIdFromBlock(block) + (meta << 12));

    IBlockState replacementState = Objects.requireNonNull(Blocks.AIR).getDefaultState();

    if (!world.isRemote && !world.restoringBlockSnapshots) {
      NonNullList<ItemStack> drops = NonNullList.create();

      block.getDrops(drops, world, pos, blockState, 0);
      boolean replanted = false;

      for (ItemStack drop : drops) {
        if (!replanted && drop.getItem() instanceof IPlantable seed) {
          if (soil.canSustainPlant(soilState, world, soilPos, EnumFacing.UP, seed)) {
            replacementState = seed.getPlant(world, pos);
            replanted = true;
            drop.shrink(1);
          }
        }

        float delta = 0.7F;
        double dx = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;
        double dy = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;
        double dz = (double) (world.rand.nextFloat() * delta) + (double) (1.0F - delta) * 0.5D;

        EntityItem entityItem = new EntityItem(world, x + dx, y + dy, z + dz, drop);
        entityItem.setPickupDelay(10);
        world.spawnEntity(entityItem);
      }
    }

    world.setBlockState(pos, replacementState, 3);
  }

  protected void fellTree(int x, int y, int z) {

    if (world.getBlockState(new BlockPos(x, y - 1, z)).getMaterial() == Material.WOOD) {
      y--;
      if (world.getBlockState(new BlockPos(x, y - 2, z)).getMaterial() == Material.WOOD) {
        y--;
      }
    }

    int meta = -1;

    for (int i = y; i < y + 10; i++) {

      int[][] dir = new int[][] {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

      for (int[] d : dir) {
        IBlockState blockState = world.getBlockState(new BlockPos(x + d[0], i, z + d[1]));

        if (blockState.getMaterial() == Material.WOOD) {
          world.destroyBlock(new BlockPos(x + d[0], i, z + d[1]), true);
        } else if (blockState.getBlock() instanceof BlockLeaves) {
          meta = blockState.getBlock().getMetaFromState(blockState) & 3;
          world.destroyBlock(new BlockPos(x + d[0], i, z + d[1]), true);
        }
      }
    }

    if (meta >= 0) {
      BlockPos saplingPos = new BlockPos(x, y, z);
      if (Objects.requireNonNull(Blocks.SAPLING).canPlaceBlockAt(world, saplingPos)) {
        world.setBlockState(saplingPos, Blocks.SAPLING.getDefaultState(), 3);
      }
    }
  }

  @Override
  public void serialize(ByteBuf buf) {
    buf.writeBoolean(this.isOn);
    buf.writeBoolean(this.isSuspended);
    buf.writeFloat(this.rotationYaw);
    buf.writeFloat(this.rotationPitch);
    this.tank.serialize(buf);
  }

  @Override
  public void deserialize(ByteBuf buf) {
    this.isOn = buf.readBoolean();
    this.isSuspended = buf.readBoolean();
    this.syncYaw = buf.readFloat();
    this.syncPitch = buf.readFloat();
    this.turnProgress = 3; // use 3-ply for extra smoothness
    this.tank.deserialize(buf);
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.isOn = nbt.getBoolean("isOn");
    this.isSuspended = nbt.getBoolean("isSuspended");
    this.forceSkip = nbt.getInteger("skip");
    this.rotationYaw = nbt.getFloat("yaw");
    this.rotationPitch = nbt.getFloat("pitch");
    this.state = nbt.getInteger("state");
    this.tank.readFromNBT(nbt, "t");
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);

    nbt.setBoolean("isOn", this.isOn);
    nbt.setBoolean("isSuspended", this.isSuspended);
    nbt.setInteger("skip", this.forceSkip);
    nbt.setFloat("yaw", this.rotationYaw);
    nbt.setFloat("pitch", this.rotationPitch);
    nbt.setInteger("state", this.state);
    tank.writeToNBT(nbt, "t");
    return nbt;
  }

  @Override
  public FluidTankNTM[] getAllTanks() {
    return new FluidTankNTM[] {tank};
  }

  @Override
  public FluidTankNTM[] getReceivingTanks() {
    return new FluidTankNTM[] {tank};
  }

  AxisAlignedBB bb = null;

  @Override
  public @NotNull AxisAlignedBB getRenderBoundingBox() {

    if (bb == null) {
      bb =
          new AxisAlignedBB(
              pos.getX() - 12,
              pos.getY(),
              pos.getZ() - 12,
              pos.getX() + 13,
              pos.getY() + 10,
              pos.getZ() + 13);
    }

    return bb;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public double getMaxRenderDistanceSquared() {
    return 65536.0D;
  }

  @Override
  public FluidTankNTM getTankToPaste() {
    return tank;
  }
}
