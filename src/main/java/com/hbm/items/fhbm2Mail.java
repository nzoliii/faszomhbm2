package com.hbm.items;

import com.hbm.lib.ModDamageSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.common.MinecraftForge;

public class fhbm2Mail extends Item {

    public fhbm2Mail(String s) {
        // Set item name and registry name
        this.setUnlocalizedName(s);
        this.setRegistryName(s);

        // Adding this item to the game
        ModItems.ALL_ITEMS.add(this);

        // Register item with the event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Event handler to catch when the player picks up the item
    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {

        // Check if the player is picking up our custom item
        if (event.getItem().getItem().getItem() == this) {
            EntityPlayer player = event.getEntityPlayer();
            ItemStack itemStack = event.getItem().getItem();

            // Immediately remove the item from the player's inventory
            itemStack.shrink(1);

            World world = player.world;

            if (!world.isRemote) {
                // Create a strong explosion at the player's position
                float strength = 100.0F;
                world.createExplosion(player, player.posX, player.posY, player.posZ, strength, true);

                // Apply damage from the custom damage source
                player.attackEntityFrom(ModDamageSource.unabomber, 40);
            }
        }
    }
}
