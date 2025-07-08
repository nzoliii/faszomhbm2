package com.hbm.handler;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.VersatileConfig;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.armor.JetpackFueledBase;
import com.hbm.items.weapon.ItemGunBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ItemStackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class ConsumableHandler {

    private static final Map<Item, Consumer<Context>> itemActions = new HashMap<>();
    private static final Map<Item, TriConsumer<ItemStack, EntityPlayer, EntityLivingBase>> hitActions = new HashMap<>();

    static {
        itemActions.put(ModItems.syringe_antidote, ConsumableHandler::handleAntidote);
        itemActions.put(ModItems.syringe_awesome, ConsumableHandler::handleAwesomeSyringe);
        itemActions.put(ModItems.syringe_poison, ConsumableHandler::handlePoisonSyringe);
        itemActions.put(ModItems.syringe_metal_stimpak, ConsumableHandler::handleMetalStimpak);
        itemActions.put(ModItems.syringe_metal_medx, ConsumableHandler::handleMetalMedX);
        itemActions.put(ModItems.syringe_metal_psycho, ConsumableHandler::handleMetalPsycho);
        itemActions.put(ModItems.syringe_metal_super, ConsumableHandler::handleMetalSuper);
        itemActions.put(ModItems.med_bag, ConsumableHandler::handleMedBag);
        itemActions.put(ModItems.syringe_taint, ConsumableHandler::handleTaintSyringe);
        itemActions.put(ModItems.jetpack_tank, ConsumableHandler::handleJetpackTank);
        itemActions.put(ModItems.gun_kit_1, ConsumableHandler::handleGunKit1);
        itemActions.put(ModItems.gun_kit_2, ConsumableHandler::handleGunKit2);
        itemActions.put(ModItems.cbt_device, ConsumableHandler::handleCbtDevice);
        itemActions.put(ModItems.syringe_mkunicorn, ConsumableHandler::handleMkUnicornSyringe);
        itemActions.put(ModItems.euphemium_stopper, ConsumableHandler::handleEupheriumStopper);

        hitActions.put(ModItems.syringe_antidote, hitAction(ConsumableHandler::handleAntidote));
        hitActions.put(ModItems.syringe_awesome, hitAction(ConsumableHandler::handleAwesomeSyringe));
        hitActions.put(ModItems.syringe_poison, hitAction(ConsumableHandler::handlePoisonSyringe));
        hitActions.put(ModItems.syringe_metal_stimpak, hitAction(ConsumableHandler::handleMetalStimpak));
        hitActions.put(ModItems.syringe_metal_medx, hitAction(ConsumableHandler::handleMetalMedX));
        hitActions.put(ModItems.syringe_metal_psycho, hitAction(ConsumableHandler::handleMetalPsycho));
        hitActions.put(ModItems.syringe_metal_super, hitAction(ConsumableHandler::handleMetalSuper));
        hitActions.put(ModItems.syringe_mkunicorn, hitAction(ConsumableHandler::handleMkUnicornSyringe));
        hitActions.put(ModItems.syringe_taint, hitAction(ConsumableHandler::handleTaintSyringe));
    }

    private static TriConsumer<ItemStack, EntityPlayer, EntityLivingBase> hitAction(final Consumer<Context> consumer) {
        return (stack, attacker, target) -> {
            consumer.accept(new Context(target.world, attacker, target, EnumHand.MAIN_HAND));
        };
    }


    public static ActionResult<ItemStack> handleItemUse(final World world, final EntityPlayer player, final EnumHand hand, final Item item) {
        if (!itemActions.containsKey(item)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        final Context context = new Context(world, player, hand);
        itemActions.get(item).accept(context);
        return context.getActionResult();
    }

    public static boolean handleHit(final ItemStack stack, final EntityPlayer player, final EntityLivingBase target) {
        if (!hitActions.containsKey(stack.getItem()))
            return false;
        hitActions.get(stack.getItem()).accept(stack, player, target);

        return false;
    }


    //I wanted to handle this more uniformly, but that was fool's errand
    private static void handleAntidote(final Context context) {
        if (!VersatileConfig.hasPotionSickness(context.target)) {
            context.target.clearActivePotions();
            VersatileConfig.applyPotionSickness(context.target, 5);
            context.playSound(HBMSoundHandler.syringeUse);
            context.shrinkAndReplaceItem(ModItems.syringe_empty);
        }
    }

    private static void handleEupheriumStopper(final Context context) {
        context.addPotionEffects(
                new PotionEffect(MobEffects.WEAKNESS, 30 * 20, 9),
                new PotionEffect(MobEffects.SLOWNESS, 30 * 20, 9),
                new PotionEffect(MobEffects.MINING_FATIGUE, 30 * 20, 9)
        );
    }


    private static void handleAwesomeSyringe(final Context context) {
        context.addPotionEffects(
                new PotionEffect(MobEffects.REGENERATION, 50 * 20, 9),
                new PotionEffect(MobEffects.RESISTANCE, 50 * 20, 9),
                new PotionEffect(MobEffects.FIRE_RESISTANCE, 50 * 20, 0),
                new PotionEffect(MobEffects.STRENGTH, 50 * 20, 24),
                new PotionEffect(MobEffects.HASTE, 50 * 20, 9),
                new PotionEffect(MobEffects.SPEED, 50 * 20, 6),
                new PotionEffect(MobEffects.JUMP_BOOST, 50 * 20, 9),
                new PotionEffect(MobEffects.HEALTH_BOOST, 50 * 20, 9),
                new PotionEffect(MobEffects.ABSORPTION, 50 * 20, 4),
                new PotionEffect(MobEffects.NAUSEA, 5 * 20, 4)
        );
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_empty);
    }

    private static void handlePoisonSyringe(final Context context) {
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_empty);
        if (context.rand.nextInt(2) == 0) {
            context.target.attackEntityFrom(ModDamageSource.euthanizedSelf, 30);
        } else {
            context.target.attackEntityFrom(ModDamageSource.euthanizedSelf2, 30);
        }
    }

    private static void handleMetalStimpak(final Context context) {
        context.target.heal(5);
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_metal_empty);
    }

    private static void handleMetalMedX(final Context context) {
        context.addPotionEffects(new PotionEffect(MobEffects.RESISTANCE, 4 * 60 * 20, 2));
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_metal_empty);
    }

    private static void handleMetalPsycho(final Context context) {
        context.addPotionEffects(
                new PotionEffect(MobEffects.RESISTANCE, 2 * 60 * 20, 0),
                new PotionEffect(MobEffects.STRENGTH, 2 * 60 * 20, 0)
        );
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_metal_empty);
    }

    private static void handleMetalSuper(final Context context) {
        context.target.heal(25);
        context.addPotionEffects(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 0));
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_metal_empty);
    }

    private static void handleMedBag(final Context context) {
        context.target.setHealth(context.target.getMaxHealth());
        context.removePotionEffects(
                MobEffects.BLINDNESS, MobEffects.NAUSEA, MobEffects.MINING_FATIGUE,
                MobEffects.HUNGER, MobEffects.SLOWNESS, MobEffects.POISON,
                MobEffects.WEAKNESS, MobEffects.WITHER, HbmPotion.radiation
        );
        context.shrinkCurrentItem();
    }

    private static void handleTaintSyringe(final Context context) {
        context.addPotionEffects(
                new PotionEffect(HbmPotion.taint, 60 * 20, 0),
                new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0)
        );
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkAndReplaceItem(ModItems.syringe_metal_empty, ModItems.bottle2_empty);
    }

    private static void handleJetpackTank(final Context context) {
        final ItemStack jetpack = context.target.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (jetpack.getItem() instanceof JetpackFueledBase jetItem) {
            if (jetItem.fuel != Fluids.KEROSENE) return;

            final int newFuel = Math.min(JetpackFueledBase.getFuel(jetpack) + 1000, jetItem.maxFuel);
            if (JetpackFueledBase.getFuel(jetpack) != newFuel) {
                JetpackFueledBase.setFuel(jetpack, newFuel);
                context.playSound(HBMSoundHandler.jetpackTank);
                context.shrinkCurrentItem();
            }
        }
    }

    private static void handleGunKit1(final Context context) {
        handleGunKit(context, 0.1F, HBMSoundHandler.spray);
    }

    private static void handleGunKit2(final Context context) {
        handleGunKit(context, 0.5F, HBMSoundHandler.repair);
    }

    private static void handleGunKit(final Context context, final float repairFactor, final SoundEvent sound) {
        for (int i = 0; i < 10; i++) {
            final ItemStack gun = (i == 9) ? context.target.getHeldItemOffhand() : context.user.inventory.mainInventory.get(i);
            if (gun.getItem() instanceof ItemGunBase) {
                final int fullDurability = ((ItemGunBase) gun.getItem()).mainConfig.durability;
                final int newWear = Math.max(ItemGunBase.getItemWear(gun) - (int) (fullDurability * repairFactor), 0);
                ItemGunBase.setItemWear(gun, newWear);
            }
        }
        context.playSound(sound);
        context.shrinkCurrentItem();
    }

    private static void handleCbtDevice(final Context context) {
        context.addPotionEffects(new PotionEffect(HbmPotion.bang, 30, 0));
        context.shrinkCurrentItem();
        context.playSound(HBMSoundHandler.vice);
    }

    private static void handleMkUnicornSyringe(final Context context) {
        HbmLivingProps.setContagion(context.target, 3 * 60 * 60 * 20);
        context.playSound(HBMSoundHandler.syringeUse);
        context.shrinkCurrentItem();
    }

    // Static class cause idk what other data structure to use
    public static class Context {
        public final World world;
        public final EntityLivingBase target;
        public final EntityPlayer user;
        public final EnumHand hand;

        public final Random rand = new Random();

        public Context(final World world, final EntityPlayer player, final EnumHand hand) {
            this.world = world;
            this.target = player;
            this.user = player;
            this.hand = hand;

        }

        public Context(final World world, final EntityPlayer player, final EntityLivingBase target, final EnumHand hand) {
            this.world = world;
            this.target = target;
            this.user = player;
            this.hand = hand;

        }

        public void shrinkCurrentItem() {
            this.user.getHeldItem(hand).shrink(1);
        }

        public void shrinkAndReplaceItem(final Item... replacements) {
            shrinkCurrentItem();
            if (user.getHeldItem(hand).isEmpty()) {
                user.setHeldItem(hand, ItemStackUtil.itemStackFrom(replacements[rand.nextInt(replacements.length)]));
            } else {
                for (final Item replacement : replacements) {
                    final ItemStack toReplace = ItemStackUtil.itemStackFrom(replacement);
                    if (!user.addItemStackToInventory(toReplace)) user.dropItem(toReplace, false);
                }
            }
        }

        public void playSound(final SoundEvent sound) {
            world.playSound(null, target.posX, target.posY, target.posZ, sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        public void addPotionEffects(final PotionEffect... effects) {
            for (final PotionEffect effect : effects) {
                target.addPotionEffect(effect);
            }
        }

        public void removePotionEffects(final Potion... potions) {
            for (final Potion potion : potions) {
                target.removePotionEffect(potion);
            }
        }

        public ActionResult<ItemStack> getActionResult() {
            return new ActionResult<>(EnumActionResult.SUCCESS, target.getHeldItem(hand));
        }
    }
}


