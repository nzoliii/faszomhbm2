package com.hbm.util;

import com.hbm.api.energymk2.IEnergyHandlerMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidUser;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.tileentity.machine.TileEntityDummy;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * EXTERNAL COMPATIBILITY CLASS - DO NOT CHANGE METHOD NAMES/PARAMS ONCE CREATED
 * Is there a smarter way to do this? Most likely. Is there an easier one? Probably not.
 * @author hbm
 */
public class CompatExternal {

    /**
     * Gets the tile entity at that pos. If the tile entity is an mk1 or mk2 dummy, it will return the core instead.
     * This method will be updated in the event that other multiblock systems or dummies are added to retrain the intended functionality.
     * @return the core tile entity if the given position holds a dummy, the tile entity at that position if it doesn't or null if there is no tile entity
     */
    @Nullable
    public static TileEntity getCoreFromPos(IBlockAccess world, BlockPos pos) {

        Block b = world.getBlockState(pos).getBlock();

        //if the block at that pos is a Dummyable, use the mk2's system to find the core
        if(b instanceof BlockDummyable dummy) {
            int[] pos1 = dummy.findCore(world, pos.getX(), pos.getY(), pos.getZ());

            if(pos1 != null) {
                return world.getTileEntity(new BlockPos(pos1[0], pos1[1], pos1[2]));
            }
        }//  else MainRegistry.logger.info("{} is not instance of BlockDummyable", b.getClass().getSimpleName());

        TileEntity tile = world.getTileEntity(pos);

        //if the tile at that pos is an old dummy tile, use mk1
        if(tile instanceof TileEntityDummy) {
            TileEntityDummy dummy = (TileEntityDummy) tile;
            return world.getTileEntity(dummy.target);
        }

        //otherwise, return the tile at that position whihc could be null
        return tile;
    }

    /**
     * Returns the numeric value of the buffered energy held by that tile entity. Current implementation relies on IEnergyUser.
     * @param tile
     * @return power
     */
    public static long getBufferedPowerFromTile(TileEntity tile) {

        if(tile instanceof IEnergyHandlerMK2) {
            return ((IEnergyHandlerMK2) tile).getPower();
        }

        return 0L;
    }

    /**
     * Returns the numeric value of the energy capacity of this tile entity. Current implementation relies on IEnergyUser.
     * @param tile
     * @return max power
     */
    public static long getMaxPowerFromTile(TileEntity tile) {

        if(tile instanceof IEnergyHandlerMK2) {
            return ((IEnergyHandlerMK2) tile).getMaxPower();
        }

        return 0L;
    }

    /**
     * Returns the ordinal of the energy priority from the supplied tile entity. 0 = low, 1 = normal, 2 = high. Returns -1 if not applicable.
     * @param tile
     * @return priority
     */
    public static int getEnergyPriorityFromTile(TileEntity tile) {

        if(tile instanceof IEnergyReceiverMK2) {
            return ((IEnergyReceiverMK2) tile).getPriority().ordinal();
        }

        return -1;
    }

    /**
     * Returns a list of tank definitions from the supplied tile entity. Uses IFluidUser, if the tile is incompatible it returns an empty list.
     * @param tile
     * @return an ArrayList of Object arrays with each array representing a fluid tank.<br>
     * [0]: STRING - unlocalized name of the fluid, simply use I18n to get the translated name<br>
     * [1]: INT - the unique ID of this fluid<br>
     * [2]: INT - the hexadecimal color of this fluid<br>
     * [3]: INT - the amount of fluid in this tank in millibuckets<br>
     * [4]: INT - the capacity of this tank in millibuckets
     */
    public static ArrayList<Object[]> getFluidInfoFromTile(TileEntity tile) {
        ArrayList<Object[]> list = new ArrayList();

        if(!(tile instanceof IFluidUser)) {
            return list;
        }

        IFluidUser container = (IFluidUser) tile;

        for(FluidTankNTM tank : container.getAllTanks()) {
            FluidType type = tank.getTankType();
            list.add(new Object[] {
                    type.getConditionalName(),
                    type.getID(),
                    type.getColor(),
                    tank.getFill(),
                    tank.getMaxFill()
            });
        }

        return list;
    }

    public static Set<Class> turretTargetPlayer = new HashSet();
    public static Set<Class> turretTargetFriendly = new HashSet();
    public static Set<Class> turretTargetHostile = new HashSet();
    public static Set<Class> turretTargetMachine = new HashSet();

    /**
     * Registers a class for turret targeting
     * @param clazz is the class that should be targeted.
     * @param type determines what setting the turret needs to have enabled to target this class. 0 is player, 1 is friendly, 2 is hostile and 3 is machine.
     */
    public static void registerTurretTargetSimple(Class clazz, int type) {

        switch(type) {
            case 0: turretTargetPlayer.add(clazz); break;
            case 1: turretTargetFriendly.add(clazz); break;
            case 2: turretTargetHostile.add(clazz); break;
            case 3: turretTargetMachine.add(clazz); break;
        }
    }

    public static Set<Class> turretTargetBlacklist = new HashSet();

    /**
     * Registers a class to be fully ignored by turrets
     * @param clazz is the class that should be ignored.
     */
    public static void registerTurretTargetBlacklist(Class clazz) {
        turretTargetBlacklist.add(clazz);
    }

    public static HashMap<Class, BiFunction<Entity, Object, Integer>> turretTargetCondition = new HashMap();

    /**
     * Registers a BiFunction lambda for more complex targeting compatibility
     * @param clazz is the class that this rule should apply to
     * @param bi is the lambda. The function should return 0 to continue with other targeting checks (i.e. do nothing), -1 to ignore this entity or 1 to target it.
     * The params for this lambda are the entity and the turret in question. The type for the turret is omitted on purpose as to not require any reference of the tile entity
     * class on the side of whoever is adding compat, allowing the compat class to be used entirely with reflection.
     */
    public static void registerTurretTargetingCondition(Class clazz, BiFunction<Entity, Object, Integer> bi) {
        turretTargetBlacklist.add(clazz);
    }
}
