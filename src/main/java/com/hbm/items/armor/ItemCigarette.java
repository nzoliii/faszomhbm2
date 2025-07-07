package com.hbm.items.armor;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.packet.AuxParticlePacketNT;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemCigarette extends Item {

    public ItemCigarette(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public @NotNull EnumAction getItemUseAction(@NotNull ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(@NotNull ItemStack stack) {
        return 30;
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, EntityPlayer playerIn, @NotNull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public @NotNull ItemStack onItemUseFinish(ItemStack stack, @NotNull World worldIn, @NotNull EntityLivingBase entityLiving) {
        stack.shrink(1);

        if(!worldIn.isRemote) {

            EntityPlayer player = (EntityPlayer) entityLiving;

            if(this == ModItems.cigarette) {
                HbmLivingProps.incrementBlackLung(player, 2000);
                HbmLivingProps.incrementAsbestos(player, 2000);
                HbmLivingProps.incrementRadiation(player, 100F);

                //ItemStack helmet = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                // TODO: Add no9 item
//                if(helmet.getItem() == ModItems.no9) {
//                    AdvancementManager.grantAchievement(player, AdvancementManager.achNo9);
//                }
            }

            if(this == ModItems.crackpipe) {
                HbmLivingProps.incrementBlackLung(player, 500);
                player.addPotionEffect(new PotionEffect(Objects.requireNonNull(MobEffects.NAUSEA), 200, 0));
                player.heal(10F);
            }

            worldIn.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.cough, SoundCategory.PLAYERS, 1.0F, 1.0F);

            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("type", "vomit");
            nbt.setString("mode", "smoke");
            nbt.setInteger("count", 30);
            nbt.setInteger("entity", player.getEntityId());
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));
        }

        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {

        if(this == ModItems.cigarette) {
            tooltip.add(ChatFormatting.RED + "✓ Asbestos filter");
            tooltip.add(ChatFormatting.RED + "✓ High in tar");
            tooltip.add(ChatFormatting.RED + "✓ Tobacco contains 100% Polonium-210");
            tooltip.add(ChatFormatting.RED + "✓ Yum");
        } else {
            String[] colors = new String[] {
                    ChatFormatting.RED + "",
                    ChatFormatting.GOLD + "",
                    ChatFormatting.YELLOW + "",
                    ChatFormatting.GREEN + "",
                    ChatFormatting.AQUA + "",
                    ChatFormatting.BLUE + "",
                    ChatFormatting.DARK_PURPLE + "",
                    ChatFormatting.LIGHT_PURPLE + "",
            };
            int len = 2000;
            tooltip.add("This can't be good for me, but I feel " + colors[(int)(System.currentTimeMillis() % len * colors.length / len)] + "GREAT");
        }
    }
}
