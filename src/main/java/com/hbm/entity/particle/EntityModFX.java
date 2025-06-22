package com.hbm.entity.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityModFX extends Entity {
    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;
    public int particleTextureIndexX;
    public int particleTextureIndexY;
    public float particleTextureJitterX;
    public float particleTextureJitterY;
    public int particleMaxAge;
    public float particleScale;
    public float particleGravity;
    /**
     * The red amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0.
     */
    public float particleRed;
    /**
     * The green amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0.
     */
    public float particleGreen;
    /**
     * The blue amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0.
     */
    public float particleBlue;
    /**
     * Particle alpha
     */
    public float particleAlpha;
    /**
     * The icon field from which the given particle pulls its texture.
     */
    public TextureAtlasSprite particleTexture;
    public int particleAge;
    public int maxAge;
    float smokeParticleScale;

    protected float normX;
    protected float normY;
    protected float normZ;

    public EntityModFX(World world) {
        super(world);
    }

    protected EntityModFX(World world, double x, double y, double z) {
        super(world);
        this.particleAlpha = 1.0F;
        this.setSize(0.2F, 0.2F);
        this.setPosition(x, y, z);
        this.lastTickPosX = x;
        this.lastTickPosY = y;
        this.lastTickPosZ = z;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
        this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
        this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
        //this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
        this.particleAge = 0;
        this.ignoreFrustumCheck = true;
    }

    public EntityModFX(World world, double sX, double sY, double sZ, double dX, double dY, double dZ) {
        this(world, sX, sY, sZ);
        this.motionX = dX + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionY = dY + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionZ = dZ + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
        float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
        this.motionX = this.motionX / f1 * f * 0.4000000059604645D;
        this.motionY = this.motionY / f1 * f * 0.4000000059604645D + 0.10000000149011612D;
        this.motionZ = this.motionZ / f1 * f * 0.4000000059604645D;
    }

    @Override
    public double getYOffset() {
        return this.height / 2.0F;
    }

    public EntityModFX multiplyVelocity(float velMultip) {
        this.motionX *= velMultip;
        this.motionY = (this.motionY - 0.10000000149011612D) * velMultip + 0.10000000149011612D;
        this.motionZ *= velMultip;
        return this;
    }

    public EntityModFX multipleParticleScaleBy(float scale) {
        this.setSize(0.2F * scale, 0.2F * scale);
        this.particleScale *= scale;
        return this;
    }

    public void setRBGColorF(float red, float green, float blue) {
        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
    }

    public void setRBGColorF(float red, float green, float blue, float opacity) {
        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
        this.particleAlpha = opacity;
    }

    /**
     * Sets the particle alpha (float)
     */
    public void setAlphaF(float opacity) {
        this.particleAlpha = opacity;
    }

    public float getRedColorF() {
        return this.particleRed;
    }

    public float getGreenColorF() {
        return this.particleGreen;
    }

    public float getBlueColorF() {
        return this.particleBlue;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void entityInit() {
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        //if (this.particleAge++ >= this.particleMaxAge)
        //{
        //    this.setDead();
        //}

        this.motionY -= 0.04D * this.particleGravity;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }


    public void renderParticle(BufferBuilder buffer, float partialTicks, float sX, float sY, float sZ, float dX, float dZ) {
        float f6 = this.particleTextureIndexX / 16.0F;
        float f7 = f6 + 0.0624375F;
        float f8 = this.particleTextureIndexY / 16.0F;
        float f9 = f8 + 0.0624375F;
        float f10 = 0.1F * this.particleScale;

        if (this.particleTexture != null) {
            f6 = this.particleTexture.getMinU();
            f7 = this.particleTexture.getMaxU();
            f8 = this.particleTexture.getMinV();
            f9 = this.particleTexture.getMaxV();
        }
        int i = this.getBrightnessForRender();
        int j = i >> 16 & 65535;
        int k = i & 65535;
        float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        buffer.pos(f11 - sX * f10 - dX * f10, f12 - sY * f10, f13 - sZ * f10 - dZ * f10).tex(f7, f9).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f11 - sX * f10 + dX * f10, f12 + sY * f10, f13 - sZ * f10 + dZ * f10).tex(f7, f8).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f11 + sX * f10 + dX * f10, f12 + sY * f10, f13 + sZ * f10 + dZ * f10).tex(f6, f8).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
        buffer.pos(f11 + sX * f10 - dX * f10, f12 - sY * f10, f13 + sZ * f10 - dZ * f10).tex(f6, f9).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j, k).endVertex();
    }

    public int getFXLayer() {
        return 0;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setShort("age", (short) this.particleAge);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_) {
        this.particleAge = p_70037_1_.getShort("age");
    }

    public void setParticleIcon(TextureAtlasSprite particleSprite) {
        if (this.getFXLayer() == 1) {
            this.particleTexture = particleSprite;
        } else {
            if (this.getFXLayer() != 2) {
                throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
            }

            this.particleTexture = particleSprite;
        }
    }

    /**
     * Public method to set private field particleTextureIndex.
     */
    public void setParticleTextureIndex(int index) {
        if (this.getFXLayer() != 0) {
            throw new RuntimeException("Invalid call to Particle.setMiscTex");
        } else {
            this.particleTextureIndexX = index % 16;
            this.particleTextureIndexY = index / 16;
        }
    }

    public void nextTextureIndexX() {
        ++this.particleTextureIndexX;
    }

    /**
     * If returns false, the item will not inflict any damage against entities.
     */
    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", Pos (" + this.posX + "," + this.posY + "," + this.posZ + "), RGBA (" + this.particleRed + "," + this.particleGreen + "," + this.particleBlue + "," + this.particleAlpha + "), Age " + this.particleAge;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 25000;
    }

    protected void setNormal(float x, float y, float z) {
        this.normX = x;
        this.normY = y;
        this.normZ = z;
    }
}
