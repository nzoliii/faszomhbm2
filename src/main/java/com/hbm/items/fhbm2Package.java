package com.hbm.items;

import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class fhbm2Package extends Item {

    public fhbm2Package(String s) {
        this.setUnlocalizedName(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.weaponTab);
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if(this == ModItems.fhbm2_package)
        {
            if (!world.isRemote)
            {
                float strength = 50.0F;
                world.createExplosion(player, player.posX, player.posY, player.posZ, strength, true);
                player.attackEntityFrom(ModDamageSource.unabomber, 40);

                player.getHeldItem(hand).shrink(1);
            }
        }
        return ActionResult.<ItemStack> newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (this == ModItems.fhbm2_package) {
            tooltip.add("A mysterious package.");
        }
    }
}