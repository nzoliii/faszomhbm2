package com.hbm.packet;

import com.hbm.entity.missile.EntityBobmazon;
import com.hbm.handler.BobmazonOfferFactory;
import com.hbm.inventory.gui.GUIScreenBobmazon.Offer;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Random;

public class ItemBobmazonPacket implements IMessage {

	int offer;

	public ItemBobmazonPacket()
	{
		
	}

	public ItemBobmazonPacket(EntityPlayer player, Offer offer)
	{
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_materials)
			this.offer = BobmazonOfferFactory.materials.indexOf(offer);
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_machines)
			this.offer = BobmazonOfferFactory.machines.indexOf(offer);
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_weapons)
			this.offer = BobmazonOfferFactory.weapons.indexOf(offer);
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_tools)
			this.offer = BobmazonOfferFactory.tools.indexOf(offer);
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_hidden)
			this.offer = BobmazonOfferFactory.special.indexOf(offer);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		offer = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(offer);
	}

	public static class Handler implements IMessageHandler<ItemBobmazonPacket, IMessage> {
		
		@Override
		public IMessage onMessage(ItemBobmazonPacket m, MessageContext ctx) {
			
			EntityPlayerMP p = ctx.getServerHandler().player;
			
			p.getServer().addScheduledTask(() -> {
				World world = p.world;

				Item mainHand = p.getHeldItemMainhand().getItem();
				Item offHand = p.getHeldItemOffhand().getItem();
				Offer offer = null;
				if(mainHand == ModItems.bobmazon_materials || offHand == ModItems.bobmazon_materials)
					offer = BobmazonOfferFactory.materials.get(m.offer);
				if(mainHand == ModItems.bobmazon_machines || offHand == ModItems.bobmazon_machines)
					offer = BobmazonOfferFactory.machines.get(m.offer);
				if(mainHand == ModItems.bobmazon_weapons || offHand == ModItems.bobmazon_weapons)
					offer = BobmazonOfferFactory.weapons.get(m.offer);
				if(mainHand == ModItems.bobmazon_tools || offHand == ModItems.bobmazon_tools)
					offer = BobmazonOfferFactory.tools.get(m.offer);
				if(mainHand == ModItems.bobmazon_hidden || offHand == ModItems.bobmazon_hidden)
					offer = BobmazonOfferFactory.special.get(m.offer);
				
				if(offer == null) {
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.failsafe.1"));
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.failsafe.2"));
					p.attackEntityFrom(ModDamageSource.nuclearBlast, 1000);
					p.motionY = 2.0D;
					return;
				}
				
				ItemStack stack = offer.offer;
				
				Advancement req = offer.requirement.getAchievement();
				
				if(req != null && p.getAdvancements().getProgress(req).isDone() || p.capabilities.isCreativeMode) {
					
					if(countCaps(p) >= offer.cost || p.capabilities.isCreativeMode) {
						
						payCaps(p, offer.cost);
						p.inventoryContainer.detectAndSendChanges();
						
						Random rand = world.rand;
						EntityBobmazon bob = new EntityBobmazon(world);
						bob.posX = p.posX + rand.nextGaussian() * 10;
						bob.posY = 300;
						bob.posZ = p.posZ + rand.nextGaussian() * 10;
						bob.payload = stack;
						
						world.spawnEntity(bob);
					} else {
						p.sendMessage(new TextComponentTranslation("chat.bobmazon.broke"));
					}
					
				} else {
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.noob"));
				}
			});
			
			
			return null;
		}
		
		private int countCaps(EntityPlayer player) {
			
			int count = 0;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null) {
					
					Item item = stack.getItem();
					
					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle ||
							item == ModItems.cap_star ||
							item == ModItems.cap_sunset)
						count += stack.getCount();
					
				}
			}
			
			return count;
		}
		
		private void payCaps(EntityPlayer player, int price) {
			
			if(price == 0)
				return;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null) {
					
					Item item = stack.getItem();
					
					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle ||
							item == ModItems.cap_star ||
							item == ModItems.cap_sunset) {
						
						int size = stack.getCount();
						for(int j = 0; j < size; j++) {
							
							player.inventory.decrStackSize(i, 1);
							price--;
							
							if(price == 0)
								return;
						}
					}
				}
			}
		}
	}
}