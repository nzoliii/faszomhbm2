package com.hbm.entity.missile;

import com.hbm.api.entity.IRadarDetectableNT;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.logic.IChunkLoader;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.MissileStruct;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartSize;
import com.hbm.items.weapon.ItemMissile.WarheadType;
import com.hbm.main.MainRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityMissileCustom extends EntityMissileBaseNT implements IChunkLoader {

	public static final DataParameter<Integer> HEALTH = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> WARHEAD = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> FUSELAGE = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> FINS = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	public static final DataParameter<Integer> THRUSTER = EntityDataManager.createKey(EntityMissileCustom.class, DataSerializers.VARINT);
	public float fuel;
	public float consumption;

	public EntityMissileCustom(World world) {
		super(world);
	}

	public EntityMissileCustom(World world, float x, float y, float z, int a, int b, MissileStruct template) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.setLocationAndAngles(x, y, z, 0, 0);
		startX = (int) x;
		startZ = (int) z;
		targetX = a;
		targetZ = b;
		this.motionY = 2;

		Vec3d vector = new Vec3d(targetX - startX, 0, targetZ - startZ);
		accelXZ = decelY = 1 / vector.length();
		decelY *= 2;
		velocity = 0;

		this.dataManager.set(WARHEAD, Item.getIdFromItem(template.warhead));
		this.dataManager.set(FUSELAGE, Item.getIdFromItem(template.fuselage));
		this.dataManager.set(THRUSTER, Item.getIdFromItem(template.thruster));
		if(template.fins != null) {
			this.dataManager.set(FINS, Item.getIdFromItem(template.fins));
		} else {
			this.dataManager.set(FINS, Integer.valueOf(0));
		}

		ItemMissile fuselage = (ItemMissile) template.fuselage;
		ItemMissile thruster = (ItemMissile) template.thruster;

		this.fuel = (float) fuselage.getTankSize();
		this.consumption = (Float) thruster.attributes[1];

		this.setSize(1.5F, 1.5F);
	}

	@Override
	protected void killMissile() {
		if(!this.isDead) {
			this.setDead();
			ExplosionLarge.explode(world, posX, posY, posZ, 5, true, false, true);
			ExplosionLarge.spawnShrapnelShower(world, posX, posY, posZ, motionX, motionY, motionZ, 15, 0.075);
		}
	}

	@Override
	public void onUpdate() {

		ItemMissile part = (ItemMissile) Item.getItemById(this.dataManager.get(WARHEAD));
		WarheadType type = (WarheadType) part.attributes[0];
		if(type != null && type.updateCustom != null) {
			type.updateCustom.accept(this);
		}

		if(!world.isRemote) {
			if(this.hasPropulsion()) this.fuel -= this.consumption;
		}

		super.onUpdate();
	}

	@Override
	public boolean hasPropulsion() {
		return this.fuel > 0;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(HEALTH, this.health);
		this.dataManager.register(WARHEAD, 0);
		this.dataManager.register(FUSELAGE, 0);
		this.dataManager.register(FINS, 0);
		this.dataManager.register(THRUSTER, 0);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		fuel = nbt.getFloat("fuel");
		consumption = nbt.getFloat("consumption");
		this.dataManager.set(WARHEAD, nbt.getInteger("warhead"));
		this.dataManager.set(FUSELAGE, nbt.getInteger("fuselage"));
		this.dataManager.set(FINS, nbt.getInteger("fins"));
		this.dataManager.set(THRUSTER, nbt.getInteger("thruster"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("fuel", fuel);
		nbt.setFloat("consumption", consumption);
		nbt.setInteger("warhead", this.dataManager.get(WARHEAD));
		nbt.setInteger("fuselage", this.dataManager.get(FUSELAGE));
		nbt.setInteger("fins", this.dataManager.get(FINS));
		nbt.setInteger("thruster", this.dataManager.get(THRUSTER));
	}

	@Override
	protected void spawnContrail() {

		Vec3d v = new Vec3d(motionX, motionY, motionZ).normalize();
		String smoke = "";
		ItemMissile part = (ItemMissile) Item.getItemById(this.dataManager.get(FUSELAGE));
		FuelType type = (FuelType) part.attributes[0];

		switch(type) {
			case BALEFIRE: smoke = "exBalefire"; break;
			case HYDROGEN: smoke = "exHydrogen"; break;
			case KEROSENE: smoke = "exKerosene"; break;
			case SOLID: smoke = "exSolid"; break;
			case XENON: break;
			case HYDRAZINE:
			case METHALOX: smoke = "exKerosene"; break;
		}

		if(!smoke.isEmpty()) for(int i = 0; i < velocity; i++) MainRegistry.proxy.spawnParticle(posX - v.x * i, posY - v.y * i, posZ - v.z * i, smoke, null);
	}

	@Override
	public void onImpact() { //TODO: demolish this steaming pile of shit

		ItemMissile part = (ItemMissile) Item.getItemById(this.dataManager.get(WARHEAD));

		WarheadType type = (WarheadType) part.attributes[0];
		float strength = (Float) part.attributes[1];

		if(type.impactCustom != null) {
			type.impactCustom.accept(this);
			return;
		}

		switch(type) {
			case HE:
				ExplosionLarge.explode(world, posX, posY, posZ, strength, true, false, true);
				ExplosionLarge.jolt(world, posX, posY, posZ, strength, (int) (strength * 50), 0.25);
				break;
			case INC:
				ExplosionLarge.explodeFire(world, posX, posY, posZ, strength, true, false, true);
				ExplosionLarge.jolt(world, posX, posY, posZ, strength * 1.5, (int) (strength * 50), 0.25);
				break;
			case CLUSTER:
				break;
			case BUSTER:
				ExplosionLarge.buster(world, posX, posY, posZ, new Vec3d(motionX, motionY, motionZ), strength, strength * 4);
				break;
			case NUCLEAR:
			case TX:
				world.spawnEntity(EntityNukeExplosionMK5.statFac(world, (int) strength, posX, posY, posZ));
				EntityNukeTorex.statFac(world, posX, posY, posZ, strength);
				break;
			case BALEFIRE:
				EntityBalefire bf = new EntityBalefire(world);
				bf.posX = this.posX;
				bf.posY = this.posY;
				bf.posZ = this.posZ;
				bf.destructionRange = (int) strength;
				world.spawnEntity(bf);
				EntityNukeTorex.statFacBale(world, posX, posY, posZ, strength);
				break;
			case N2:
				world.spawnEntity(EntityNukeExplosionMK5.statFacNoRad(world, (int) strength, posX, posY, posZ));
				EntityNukeTorex.statFac(world, posX, posY, posZ, strength);
				break;
			case TAINT:
				int r = (int) strength;
				for(int i = 0; i < r * 10; i++) {
					int a = rand.nextInt(r) + (int) posX - (r / 2 - 1);
					int b = rand.nextInt(r) + (int) posY - (r / 2 - 1);
					int c = rand.nextInt(r) + (int) posZ - (r / 2 - 1);
					BlockPos pos = new BlockPos(a, b, c);
					if(world.getBlockState(pos).getMaterial().isReplaceable() && BlockTaint.hasPosNeightbour(world, pos)) {
						world.setBlockState(pos, ModBlocks.taint.getStateFromMeta(rand.nextInt(3) + 4), 2);
					}
				}
				break;
			case CLOUD:
				this.world.playEvent(2002, new BlockPos((int) Math.round(this.posX), (int) Math.round(this.posY), (int) Math.round(this.posZ)), 0);
				ExplosionChaos.spawnChlorine(world, posX - motionX, posY - motionY, posZ - motionZ, 750, 2.5, 2);
				break;
			/*case TURBINE:
				ExplosionLarge.explode(world, posX, posY, posZ, 10, true, false, true);
				int count = (int) strength;
				Vec3 vec = Vec3.createVectorHelper(0.5, 0, 0);

				for(int i = 0; i < count; i++) {
					EntityBulletBaseNT blade = new EntityBulletBaseNT(world, BulletConfigSyncingUtil.TURBINE);
					blade.setPositionAndRotation(this.posX - this.motionX, this.posY - this.motionY + rand.nextGaussian(), this.posZ - this.motionZ, 0, 0);
					blade.motionX = vec.xCoord;
					blade.motionZ = vec.zCoord;
					world.spawnEntityInWorld(blade);
					vec.rotateAroundY((float) (Math.PI * 2F / (float) count));
				}

				break;*/
			default:
				break;

		}
	}

	@Override
	public String getTranslationKey() {

		ItemMissile part = (ItemMissile) Item.getItemById(this.dataManager.get(FUSELAGE));
		PartSize top = part.top;
		PartSize bottom = part.bottom;

		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) return "radar.target.custom10";
		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) return "radar.target.custom1015";
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) return "radar.target.custom15";
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) return "radar.target.custom1520";
		if(top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) return "radar.target.custom20";

		return "radar.target.custom";
	}

	@Override
	public int getBlipLevel() {

		ItemMissile part = (ItemMissile) Item.getItemById(this.dataManager.get(FUSELAGE));
		PartSize top = part.top;
		PartSize bottom = part.bottom;

		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) return IRadarDetectableNT.TIER10;
		if(top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER10_15;
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) return IRadarDetectableNT.TIER15;
		if(top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER15_20;
		if(top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) return IRadarDetectableNT.TIER20;

		return IRadarDetectableNT.TIER1;
	}

	@Override public List<ItemStack> getDebris() { return new ArrayList<>(); }
	@Override public ItemStack getDebrisRareDrop() { return null; }

	@Override
	public ItemStack getMissileItemForInfo() {
		return new ItemStack(ModItems.missile_custom);
	}

}
