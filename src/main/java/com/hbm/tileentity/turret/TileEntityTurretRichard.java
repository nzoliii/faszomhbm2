package com.hbm.tileentity.turret;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretRichard;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TileEntityTurretRichard extends TileEntityTurretBaseNT implements IGUIProvider {

	static List<Integer> configs = new ArrayList<>();

	static {
		configs.add(BulletConfigSyncingUtil.ROCKET_NORMAL);
		configs.add(BulletConfigSyncingUtil.ROCKET_HE);
		configs.add(BulletConfigSyncingUtil.ROCKET_INCENDIARY);
		configs.add(BulletConfigSyncingUtil.ROCKET_SHRAPNEL);
		configs.add(BulletConfigSyncingUtil.ROCKET_EMP);
		configs.add(BulletConfigSyncingUtil.ROCKET_GLARE);
		configs.add(BulletConfigSyncingUtil.ROCKET_SLEEK);
		configs.add(BulletConfigSyncingUtil.ROCKET_NUKE);
		configs.add(BulletConfigSyncingUtil.ROCKET_CHAINSAW);
		configs.add(BulletConfigSyncingUtil.ROCKET_TOXIC);
		configs.add(BulletConfigSyncingUtil.ROCKET_PHOSPHORUS);
		configs.add(BulletConfigSyncingUtil.ROCKET_CANISTER);
	}

	@Override
	protected List<Integer> getAmmoList(){
		return configs;
	}

	@Override
	public String getName(){
		return "container.turretRichard";
	}

	@Override
	public double getTurretDepression(){
		return 25D;
	}

	@Override
	public double getTurretElevation(){
		return 25D;
	}

	@Override
	public double getBarrelLength(){
		return 1.25D;
	}

	@Override
	public long getMaxPower(){
		return 10000;
	}

	@Override
	public double getDecetorGrace(){
		return 8D;
	}

	@Override
	public double getDecetorRange(){
		return 64D;
	}

	int timer;
	public int loaded;
	int reload;

	@Override
	public void update(){
		super.update();
		
		if(!world.isRemote) {
			
			if(reload > 0) {
				reload--;
				
				if(reload == 0)
					this.loaded = 17;
			}
			
			if(loaded <= 0 && reload <= 0 && this.getFirstConfigLoaded() != null) {
				reload = 100;
			}
			
			if(this.getFirstConfigLoaded() == null) {
				this.loaded = 0;
			}

			networkPackNT(250);
		}
	}

	@Override
	public void updateFiringTick(){
		if(reload > 0)
			return;
		
		timer++;
		
		if(timer > 0 && timer % 10 == 0) {
			
			BulletConfiguration conf = this.getFirstConfigLoaded();
			
			if(conf != null) {
				this.spawnBullet(conf);
				this.consumeAmmo(conf.ammo);
				this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.richard_fire, SoundCategory.BLOCKS, 2.0F, 1.0F);
				this.loaded--;
				
				if(conf.ammo == ModItems.ammo_rocket_nuclear)
					timer = -50;
				
			} else {
				this.loaded = 0;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.loaded);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.loaded = buf.readInt();
	}

	@Override
	public void spawnBullet(BulletConfiguration bullet){
		Vec3 pos = new Vec3(this.getTurretPos());
		Vec3 vec = Vec3.createVectorHelper(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZ((float) -this.rotationPitch);
		vec.rotateAroundY((float) -(this.rotationYaw + Math.PI * 0.5));
		
		EntityBulletBase proj = new EntityBulletBase(world, BulletConfigSyncingUtil.getKey(bullet));
		proj.setPositionAndRotation(pos.xCoord + vec.xCoord, pos.yCoord + vec.yCoord, pos.zCoord + vec.zCoord, 0.0F, 0.0F);
		
		proj.shoot(vec.xCoord, vec.yCoord, vec.zCoord, bullet.velocity * 0.75F, bullet.spread);
		world.spawnEntity(proj);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		this.loaded = nbt.getInteger("loaded");
		super.readFromNBT(nbt);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt){
		nbt.setInteger("loaded", this.loaded);
		return super.writeToNBT(nbt);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretRichard(player.inventory, this);
	}

}
