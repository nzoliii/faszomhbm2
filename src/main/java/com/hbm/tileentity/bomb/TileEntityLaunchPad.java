package com.hbm.tileentity.bomb;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.inventory.container.ContainerLaunchPadTier1;
import com.hbm.inventory.gui.GUILaunchPadTier1;
import com.hbm.lib.DirPos;
import com.hbm.lib.Library;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class TileEntityLaunchPad extends TileEntityLaunchPadBase {

    public int clearingTimer = 0;

    public TileEntityLaunchPad() {
        super(3);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (this.redstonePower > 0 && this.prevRedstonePower <= 0) {
                this.launchFromDesignator();
            }
            this.prevRedstonePower = this.redstonePower;
            if (clearingTimer > 0) {
                clearingTimer--;
            }
            this.power = Library.chargeTEFromItems(inventory, 2, power, maxPower);
            this.networkPackNT(250);
        }
    }

    @Override
    public boolean hasFuel() {
        return this.power >= 75_000;
    }

    @Override
    public boolean isReadyForLaunch() {
        return this.clearingTimer <= 0;
    }

    @Override
    public double getLaunchOffset() {
        return 1.5D;
    }

    @Override
    public void finalizeLaunch(Entity missile) {
        super.finalizeLaunch(missile);
        this.clearingTimer = 100;
    }

    @Override
    public DirPos[] getConPos() {
        return new DirPos[]{new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X), new DirPos(pos.getX(), pos.getY(),
                pos.getZ() + 1, Library.POS_Z), new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y)};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.clearingTimer = nbt.getInteger("clearingTimer");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("clearingTimer", this.clearingTimer);
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerLaunchPadTier1(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUILaunchPadTier1(player.inventory, this);
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "launchpad";
    }

    @Callback(doc = "setTarget(x:int, z:int):boolean; Sets coordinates in the installed designator item.")
    @Optional.Method(modid = "opencomputers")
    public Object[] setTarget(Context context, Arguments args) {
        ItemStack designatorStack = inventory.getStackInSlot(1);
        if (!designatorStack.isEmpty() && designatorStack.getItem() instanceof IDesignatorItem) {
            NBTTagCompound nbt = designatorStack.hasTagCompound() ? designatorStack.getTagCompound() :
                    new NBTTagCompound();
            nbt.setInteger("xCoord", args.checkInteger(0));
            nbt.setInteger("zCoord", args.checkInteger(1));
            designatorStack.setTagCompound(nbt);
            return new Object[]{true};
        }
        return new Object[]{false, "No valid designator installed"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{"getEnergyInfo", "canLaunch", "launch", "setTarget"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getEnergyInfo" -> getEnergyInfo(context, args);
            case "canLaunch" -> canLaunch(context, args);
            case "launch" -> launch(context, args);
            case "setTarget" -> setTarget(context, args);
            default -> throw new NoSuchMethodException();
        };
    }
}