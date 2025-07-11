package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.machine.MachineCharger;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.packet.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

public class TileEntityCharger extends TileEntityLoadedBase implements IBufPacketReceiver, ITickable, IEnergyReceiverMK2 {

	public static final int range = 3;

	private List<EntityPlayer> players = new ArrayList<>();
	private long maxChargeRate;
	public long charge = 0;
	public long actualCharge = 0;
	public long totalCapacity = 0;
	public long totalEnergy = 0;
	private int lastOp = 0;

	public boolean isOn = false;
	public boolean pointingUp = true;

	@Override
	public void update() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

		if(!world.isRemote) {
			MachineCharger c = (MachineCharger)world.getBlockState(pos).getBlock();
			this.maxChargeRate = c.maxThroughput;
			this.pointingUp = c.pointingUp;

			this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir);

			players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + (pointingUp ? range : -range), pos.getZ() + 1));

			totalCapacity = 0;
			totalEnergy = 0;
			charge = 0;

			for(EntityPlayer player : players) {
				InventoryPlayer inv = player.inventory;
				for(int i = 0; i < inv.getSizeInventory(); i ++){

					ItemStack stack = inv.getStackInSlot(i);

					if(Library.isItemBattery(stack)) {
						if (stack.getItem() instanceof IBatteryItem battery) {
							totalCapacity += battery.getMaxCharge();
							totalEnergy += battery.getCharge(stack);
							charge += Math.min(battery.getMaxCharge() - battery.getCharge(stack), battery.getChargeRate());
						} else {
							IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
							if (cap != null && GeneralConfig.conversionRateHeToRF > 0) {
								long maxHe = (long) (cap.getMaxEnergyStored() / GeneralConfig.conversionRateHeToRF);
								long currentHe = (long) (cap.getEnergyStored() / GeneralConfig.conversionRateHeToRF);
								totalCapacity += maxHe;
								totalEnergy += currentHe;
								charge += maxHe - currentHe;
							}
						}
					}
				}
			}

			isOn = lastOp > 0;

			if(isOn) {
				lastOp--;
			}

			networkPackNT(50);
			actualCharge = 0;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean( isOn);
		buf.writeBoolean(pointingUp);
		buf.writeLong(totalCapacity);
		buf.writeLong(totalEnergy);
		buf.writeLong(charge);
		buf.writeLong(actualCharge);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.isOn = buf.readBoolean();
		this.pointingUp = buf.readBoolean();
		this.totalCapacity = buf.readLong();
		this.totalEnergy = buf.readLong();
		this.charge = buf.readLong();
		this.actualCharge = buf.readLong();
	}

	public void networkPackNT(int range) {
		if (!world.isRemote)
			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}

	@Override
	public long getPower() {
		return 0;
	}

	@Override
	public long getMaxPower() {
		return Math.min(charge, maxChargeRate);
	}

	@Override
	public void setPower(long power) { }

	@Override
	public long transferPower(long power, boolean simulate) {
		if(power == 0) return 0;
		if(!simulate) {
			actualCharge = 0;
		}
		long chargeBudget = maxChargeRate;
		long powerBudget = power;
		for(EntityPlayer player : players) {
			InventoryPlayer inv = player.inventory;
			for(int i = 0; i < inv.getSizeInventory(); i ++){
				if(chargeBudget <= 0 || powerBudget <= 0) break;
				ItemStack stack = inv.getStackInSlot(i);
				if(Library.isItemChargeableBattery(stack)) {
					long powerToOffer = Math.min(powerBudget, chargeBudget);
                    if (!simulate) {
                        long chargedAmount = Library.chargeBatteryIfValid(stack, powerToOffer, false);

                        if (chargedAmount > 0) {
                            actualCharge += chargedAmount;
                            powerBudget -= chargedAmount;
                            chargeBudget -= chargedAmount;
                            lastOp = 4;
                        }
                    } else {
                        long chargedAmount;
                        if (stack.getItem() instanceof IBatteryItem battery) {
                            chargedAmount = Math.min(battery.getMaxCharge() - battery.getCharge(stack), battery.getChargeRate());
                        } else {
							IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
                            chargedAmount = (long) ((cap.getMaxEnergyStored() - cap.getEnergyStored()) / GeneralConfig.conversionRateHeToRF);
                        }
                        chargedAmount = Math.min(chargedAmount, powerToOffer);
                        powerBudget -= chargedAmount;
                        chargeBudget -= chargedAmount;
                    }
                }
			}
			if(chargeBudget <= 0 || powerBudget <= 0) {
				break;
			}
		}
		return powerBudget;
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}