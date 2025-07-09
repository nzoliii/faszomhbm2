package com.hbm.items.tool;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.PlayerInformPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemRangefinder extends Item {
    public static final int META_POLARIZED = 1;

    public ItemRangefinder(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.missileTab);

        ModItems.ALL_ITEMS.add(this);
    }


    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @NotNull EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            Vec3d start = new Vec3d(playerIn.posX, playerIn.posY + playerIn.getEyeHeight(), playerIn.posZ);
            Vec3d end = start.add(playerIn.getLookVec().normalize().scale(200));
            RayTraceResult result = worldIn.rayTraceBlocks(start, end, false, true, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                double dist = start.distanceTo(result.hitVec);
                String msg = ((int) (dist * 10D)) / 10D + "m";
                if (stack.getMetadata() == META_POLARIZED) {
                    msg = TextFormatting.LIGHT_PURPLE + msg + TextFormatting.RESET;
                }
                PacketDispatcher.sendTo(new PlayerInformPacket(msg), (EntityPlayerMP) playerIn);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @NotNull
    @Override
    public String getItemStackDisplayName(@NotNull ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        if (stack.getMetadata() == META_POLARIZED) {
            name = TextFormatting.LIGHT_PURPLE + name + TextFormatting.RESET;
        }
        return name;
    }
}
