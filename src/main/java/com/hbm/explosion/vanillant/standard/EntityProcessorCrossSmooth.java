package com.hbm.explosion.vanillant.standard;

public class EntityProcessorCrossSmooth extends EntityProcessorCross {

    protected float fixedDamage;
    protected float pierceDT = 0;
    protected float pierceDR = 0;
    //protected DamageClass clazz = DamageClass.EXPLOSIVE;

    public EntityProcessorCrossSmooth(double nodeDist, float fixedDamage) {
        super(nodeDist);
        this.fixedDamage = fixedDamage;
        this.setAllowSelfDamage();
    }

    public EntityProcessorCrossSmooth setupPiercing(float pierceDT, float pierceDR) {
        this.pierceDT = pierceDT;
        this.pierceDR = pierceDR;
        return this;
    }

//    public EntityProcessorCrossSmooth setDamageClass(DamageClass clazz) {
//        this.clazz = clazz;
//        return this;
//    }
//
//    @Override
//    public void attackEntity(Entity entity, ExplosionVNT source, float amount) {
//        if(!entity.isEntityAlive()) return;
//        if(source.exploder == entity) amount *= 0.5F;
//        DamageSource dmg = BulletConfig.getDamage(null, source.exploder instanceof EntityLivingBase ? (EntityLivingBase) source.exploder : null, clazz);
//        if(!(entity instanceof EntityLivingBase)) {
//            entity.attackEntityFrom(dmg, amount);
//        } else {
//            EntityDamageUtil.attackEntityFromNT((EntityLivingBase) entity, dmg, amount, true, false, 0F, pierceDT, pierceDR);
//            if(!entity.isEntityAlive()) ConfettiUtil.decideConfetti((EntityLivingBase) entity, dmg);
//        }
//    }
//
//    @Override
//    public float calculateDamage(double distanceScaled, double density, double knockback, float size) {
//        return (float) (fixedDamage * (1 - distanceScaled));
//    }
}
