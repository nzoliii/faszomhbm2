package com.hbm.entity.projectile.rocketbehavior;

import com.hbm.entity.projectile.EntityArtilleryRocket;
import net.minecraft.entity.Entity;

public interface IRocketTargetingBehavior {

    /** Recalculates the position that should be steered towards. */
    void recalculateTargetPosition(EntityArtilleryRocket rocket, Entity target);
}
