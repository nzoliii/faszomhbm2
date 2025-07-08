package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.inventory.container.ContainerCoreStabilizer;
import com.hbm.inventory.gui.GUICoreStabilizer;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemLens;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityCoreStabilizer extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, SimpleComponent, IGUIProvider, CompatHandler.OCComponent {

    public static final long maxPower = 2500000000L;
    public static final int range = 15;
    public long power;
    public int watts;
    public int beam;
    public boolean isOn;

    public TileEntityCoreStabilizer() {
        super(1);

    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.updateConnections();

            watts = MathHelper.clamp(watts, 1, 100);
            int demand = (int) Math.pow(watts, 4);

            beam = 0;

            if (power >= demand && !getLensSlot().isEmpty() && getLensSlot().getItem() == ModItems.ams_lens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) ModItems.ams_lens).maxDamage) {

                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata());
                for (int i = 1; i <= range; i++) {

                    int x = pos.getX() + dir.offsetX * i;
                    int y = pos.getY() + dir.offsetY * i;
                    int z = pos.getZ() + dir.offsetZ * i;
                    BlockPos pos = new BlockPos(x, y, z);

                    TileEntity te = world.getTileEntity(pos);

                    if (te instanceof TileEntityCore) {

                        TileEntityCore core = (TileEntityCore) te;
                        core.field = Math.max(core.field, watts);
                        this.power -= demand;
                        beam = i;

                        long dmg = ItemLens.getLensDamage(getLensSlot());
                        dmg += watts;

                        if (dmg >= ((ItemLens) ModItems.ams_lens).maxDamage)
                            inventory.setStackInSlot(0, ItemStack.EMPTY);
                        else
                            ItemLens.setLensDamage(getLensSlot(), dmg);

                        break;
                    }

                    if (!world.isAirBlock(pos))
                        break;
                }
            }

            this.networkPackNT(250);
        }
    }

    private @NotNull ItemStack getLensSlot() {
        return inventory.getStackInSlot(0);
    }

    private void updateConnections() {

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
    }

    @Override
    public void networkUnpack(NBTTagCompound data) {
        power = data.getLong("power");
        watts = data.getInteger("watts");
        isOn = data.getBoolean("isOn");
    }

    @Override
    public String getName() {
        return "container.dfcStabilizer";
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    public int getWattsScaled(int i) {
        return (watts * i) / 100;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public void setPower(long i) {
        this.power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        power = compound.getLong("power");
        watts = compound.getInteger("watts");
        isOn = compound.getBoolean("isOn");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setInteger("watts", watts);
        compound.setBoolean("isOn", isOn);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return "dfc_stabilizer";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{getPower(), getMaxPower()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInput(Context context, Arguments args) {
        return new Object[]{watts};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getDurability(Context context, Arguments args) {
        if (getLensSlot() != null && getLensSlot().getItem() == ModItems.ams_lens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) ModItems.ams_lens).maxDamage) {
            return new Object[]{ItemLens.getLensDamage(getLensSlot())};
        }
        return new Object[]{"N/A"};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInfo(Context context, Arguments args) {
        Object lens_damage_buf;
        if (!getLensSlot().isEmpty() && getLensSlot().getItem() == ModItems.ams_lens && ItemLens.getLensDamage(getLensSlot()) < ((ItemLens) ModItems.ams_lens).maxDamage) {
            lens_damage_buf = ItemLens.getLensDamage(getLensSlot());
        } else {
            lens_damage_buf = "N/A";
        }
        return new Object[]{power, maxPower, watts, lens_damage_buf};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "OpenComputers")
    public Object[] setInput(Context context, Arguments args) {
        int newOutput = args.checkInteger(0);
        watts = MathHelper.clamp(newOutput, 0, 100);
        return new Object[]{};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCoreStabilizer(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICoreStabilizer(player, this);
    }
}
