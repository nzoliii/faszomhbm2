package com.hbm.items.tool;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionData;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.items.ModItems;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.PlayerInformPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemPollutionDetector extends Item {
    public ItemPollutionDetector(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModItems.ALL_ITEMS.add(this);
    }
    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!(entityIn instanceof EntityPlayerMP) || worldIn.getTotalWorldTime() % 10 != 0) {
            return;
        }
        PollutionData data = PollutionHandler.getPollutionData(worldIn, entityIn.getPosition());
        if (data == null) {
            data = new PollutionData();
        }

        float soot = ((int) (data.pollution[PollutionType.SOOT.ordinal()] * 100)) / 100F;
        float poison = ((int) (data.pollution[PollutionType.POISON.ordinal()] * 100)) / 100F;
        float heavymetal = ((int) (data.pollution[PollutionType.HEAVYMETAL.ordinal()] * 100)) / 100F;

        sendTooltip((EntityPlayerMP) entityIn, "Soot: " + soot);
        sendTooltip((EntityPlayerMP) entityIn, "Poison: " + poison);
        sendTooltip((EntityPlayerMP) entityIn, "Heavy metal: " + heavymetal);
    }

    private void sendTooltip(EntityPlayerMP player, String message) {
        PacketDispatcher.sendTo(new PlayerInformPacket(TextFormatting.YELLOW + message + TextFormatting.RESET), player);
    }
}
