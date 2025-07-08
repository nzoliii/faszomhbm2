package com.hbm.packet;

import com.hbm.interfaces.Spaghetti;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.bomb.TileEntityCompactLauncher;
import com.hbm.tileentity.bomb.TileEntityLaunchPad;
import com.hbm.tileentity.bomb.TileEntityLaunchTable;
import com.hbm.tileentity.bomb.TileEntityRailgun;
import com.hbm.tileentity.machine.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Spaghetti("Changing all machiines to use TileEntityMachineBase will reduce the total chaos in this class")
public class AuxGaugePacket implements IMessage {

    int x;
    int y;
    int z;
    int value;
    int id;

    public AuxGaugePacket() {

    }

    public AuxGaugePacket(int x, int y, int z, int value, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
        this.id = id;
    }

    public AuxGaugePacket(BlockPos pos, int value, int id) {
        this(pos.getX(), pos.getY(), pos.getZ(), value, id);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        value = buf.readInt();
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(value);
        buf.writeInt(id);
    }

    public static class Handler implements IMessageHandler<AuxGaugePacket, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(AuxGaugePacket m, MessageContext ctx) {

            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(m.x, m.y, m.z));

                    if (te instanceof TileEntityMachineDiesel) {
                        TileEntityMachineDiesel selenium = (TileEntityMachineDiesel) te;

                        selenium.powerCap = m.value;
                    } else if (te instanceof TileEntityMachineGasCent) {
                        TileEntityMachineGasCent cent = (TileEntityMachineGasCent) te;

                        if (m.id == 0)
                            cent.progress = m.value;
                        if (m.id == 1)
                            cent.isProgressing = m.value == 1;
                    } else if (te instanceof TileEntityMachineCentrifuge) {
                        TileEntityMachineCentrifuge cent = (TileEntityMachineCentrifuge) te;

                        if (m.id == 0)
                            cent.progress = m.value;
                        if (m.id == 1)
                            cent.isProgressing = m.value == 1;
                    } else if (te instanceof TileEntityMachineElectricFurnace) {
                        TileEntityMachineElectricFurnace furn = (TileEntityMachineElectricFurnace) te;

                        if (m.id == 0)
                            furn.dualCookTime = m.value;
                    } else if (te instanceof TileEntityMachineArcFurnace) {
                        TileEntityMachineArcFurnace furn = (TileEntityMachineArcFurnace) te;

                        if (m.id == 0)
                            furn.dualCookTime = m.value;
                    } else if (te instanceof TileEntityCompactLauncher) {
                        TileEntityCompactLauncher launcher = (TileEntityCompactLauncher) te;

                        if (m.id == 0)
                            launcher.solid = m.value;
                        if (m.id == 1)
                            launcher.clearingTimer = m.value;
                    } else if (te instanceof TileEntityLaunchPad) {
                        TileEntityLaunchPad launcher = (TileEntityLaunchPad) te;

                        launcher.clearingTimer = m.value;
                    } else if (te instanceof TileEntityLaunchTable) {
                        TileEntityLaunchTable launcher = (TileEntityLaunchTable) te;

                        if (m.id == 0)
                            launcher.solid = m.value;
                        if (m.id == 1)
                            launcher.padSize = PartSize.values()[m.value];
                        if (m.id == 2)
                            launcher.clearingTimer = m.value;
                    } else if (te instanceof TileEntityRailgun) {

                        TileEntityRailgun gen = (TileEntityRailgun) te;

                        if (m.id == 0) {
                            Vec3 vec = Vec3.createVectorHelper(5.5, 0, 0);
                            vec.rotateAroundZ((float) (gen.pitch * Math.PI / 180D));
                            vec.rotateAroundY((float) (gen.yaw * Math.PI / 180D));

                            double fX = gen.getPos().getX() + 0.5 + vec.xCoord;
                            double fY = gen.getPos().getY() + 1 + vec.yCoord;
                            double fZ = gen.getPos().getZ() + 0.5 + vec.zCoord;

                            MainRegistry.proxy.spawnSFX(gen.getWorld(), fX, fY, fZ, 0, vec.normalize());
                        }

                    } else if (te instanceof TileEntityCoreEmitter) {
                        if (m.id == 0)
                            ((TileEntityCoreEmitter) te).beam = m.value;
                        if (m.id == 1)
                            ((TileEntityCoreEmitter) te).watts = m.value;
                    } else if (te instanceof TileEntityCoreInjector) {
                        if (m.id == 0)
                            ((TileEntityCoreInjector) te).beam = m.value;
                    } else if (te instanceof TileEntityCoreStabilizer) {
                        if (m.id == 0)
                            ((TileEntityCoreStabilizer) te).beam = m.value;
                    } else if (te instanceof TileEntitySlidingBlastDoor) {
                        ((TileEntitySlidingBlastDoor) te).shouldUseBB = m.value == 1 ? true : false;
                    }
                } catch (Exception x) {
                }
            });

            return null;
        }
    }
}
