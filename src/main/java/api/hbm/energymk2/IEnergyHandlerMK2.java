package api.hbm.energymk2;

import api.hbm.tile.ILoadedTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

/** DO NOT USE DIRECTLY! This is simply the common ancestor to providers and receivers, because all this behavior has to be excluded from conductors! */
public interface IEnergyHandlerMK2 extends IEnergyConnectorMK2, ILoadedTile {

    public long getPower();
    public void setPower(long power);
    public long getMaxPower();

    public static final boolean particleDebug = false;

    public default Vec3d getDebugParticlePosMK2() {
        TileEntity te = (TileEntity) this;
        Vec3d vec = new Vec3d(te.getPos().getX() + 0.5, te.getPos().getY() + 1, te.getPos().getZ() + 0.5);
        return vec;
    }
}