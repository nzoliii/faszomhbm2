package com.hbm.handler;

import com.hbm.particle.ParticleSpentCasing;
import com.hbm.particle.SpentCasing;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.HashMap;
import java.util.Random;

public class CasingEjector implements Cloneable{
    public static HashMap<Integer, CasingEjector> mappings = new HashMap<Integer, CasingEjector>();
    public static final Random rand = new Random();

    private int id;
    private static int nextId = 0;
    private Vec3 posOffset = Vec3.createVectorHelper(0, 0, 0);
    private Vec3 initialMotion = Vec3.createVectorHelper(0, 0, 0);
    @Deprecated private int casingAmount = 1;
    @Deprecated private boolean afterReload = false;
    @Deprecated private int delay = 0;
    private float randomYaw = 0F;
    private float randomPitch = 0F;

    public CasingEjector() {
        this.id = nextId;
        nextId++;

        mappings.put(id, this);
    }

    public CasingEjector setOffset(double x, double y, double z) {
        return setOffset(Vec3.createVectorHelper(x, y, z));
    }

    public CasingEjector setOffset(Vec3 vec) {
        this.posOffset = vec;
        return this;
    }

    public CasingEjector setMotion(double x, double y, double z) {
        return setMotion(Vec3.createVectorHelper(x, y, z));
    }

    public CasingEjector setMotion(Vec3 vec) {
        this.initialMotion = vec;
        return this;
    }

    @Deprecated public CasingEjector setAmount(int am) {
        this.casingAmount = am;
        return this;
    }

    @Deprecated public CasingEjector setAfterReload() {
        this.afterReload = true;
        return this;
    }

    @Deprecated public CasingEjector setDelay(int delay) {
        this.delay = delay;
        return this;
    }

    public CasingEjector setAngleRange(float yaw, float pitch) {
        this.randomYaw = yaw;
        this.randomPitch = pitch;
        return this;
    }

    public int getId() { return this.id; }
    public Vec3 getOffset() { return this.posOffset; }
    public Vec3 getMotion() { return this.initialMotion; }
    public int getAmount() { return this.casingAmount; }
    public boolean getAfterReload() { return this.afterReload; }
    public int getDelay() { return this.delay; }
    public float getYawFactor() { return this.randomYaw; }
    public float getPitchFactor() { return this.randomPitch; }

    @SideOnly(Side.CLIENT)
    public void spawnCasing(TextureManager textureManager, SpentCasing config, World world, double x, double y, double z, float pitch, float yaw, boolean crouched) {
        Vec3 rotatedMotionVec = rotateVector(getMotion(), pitch + (float) rand.nextGaussian() * getPitchFactor(), yaw + (float) rand.nextGaussian() * getPitchFactor(), getPitchFactor(), getPitchFactor());
        ParticleSpentCasing casing = new ParticleSpentCasing(textureManager, world, x, y, z, rotatedMotionVec.xCoord, rotatedMotionVec.yCoord, rotatedMotionVec.zCoord, (float) (getPitchFactor() * rand.nextGaussian()), (float) (getYawFactor() * rand.nextGaussian()), config, false, 0, 0, 0);

        offsetCasing(casing, getOffset(), pitch, yaw, crouched);

        casing.rotationPitch = (float) Math.toDegrees(pitch);
        casing.rotationYaw = (float) Math.toDegrees(yaw);

        Minecraft.getMinecraft().effectRenderer.addEffect(casing);
    }

    // Rotate a position
    @SideOnly(Side.CLIENT)
    private static void offsetCasing(ParticleSpentCasing casing, Vec3 offset, float pitch, float yaw, boolean crouched) {
        // x-axis offset, 0 if crouched to center
        final float oX = (float) (crouched ? 0 : offset.xCoord);
        // Create rotation matrices for pitch and yaw
        final Matrix4f pitchMatrix = new Matrix4f(), yawMatrix = new Matrix4f();

        pitchMatrix.rotate(pitch, new Vector3f(1, 0, 0)); // modify axis of rotation
        yawMatrix.rotate(-yaw, new Vector3f(0, 1, 0));

        // Multiply matrices to get combined rotation matrix
        final org.lwjgl.util.vector.Matrix4f rotMatrix = Matrix4f.mul(yawMatrix, pitchMatrix, null);
        // Create vector representing the offset and apply rotation
        final org.lwjgl.util.vector.Vector4f offsetVector = new org.lwjgl.util.vector.Vector4f(oX, (float) offset.yCoord, (float) offset.zCoord, 1); // set fourth coordinate to 1
        Matrix4f.transform(rotMatrix, offsetVector, offsetVector);
        final Vector3f result = new Vector3f(); // create result vector
        result.set(offsetVector.x, offsetVector.y, offsetVector.z); // set result vector using transformed coordinates
        // Apply rotation
        casing.setPosition(casing.getPosX() + result.x, casing.getPosY() + result.y, casing.getPosZ() + result.z);
    }

    private static Vec3 rotateVector(Vec3 vector, float pitch, float yaw, float pitchFactor, float yawFactor) {

        final org.lwjgl.util.vector.Matrix4f pitchMatrix = new org.lwjgl.util.vector.Matrix4f(), yawMatrix = new org.lwjgl.util.vector.Matrix4f();

        pitchMatrix.setIdentity();
        pitchMatrix.rotate(pitch, new Vector3f(1, 0, 0));

        yawMatrix.setIdentity();
        yawMatrix.rotate(-yaw, new Vector3f(0, 1, 0));

        // Apply randomness to vector
        final org.lwjgl.util.vector.Vector4f vector4f = new Vector4f((float) (vector.xCoord + rand.nextGaussian() * yawFactor), (float) (vector.yCoord + rand.nextGaussian() * pitchFactor), (float) (vector.zCoord + rand.nextGaussian() * yawFactor), 1);

        org.lwjgl.util.vector.Matrix4f.transform(pitchMatrix, vector4f, vector4f);
        org.lwjgl.util.vector.Matrix4f.transform(yawMatrix, vector4f, vector4f);

        return Vec3.createVectorHelper(vector4f.x, vector4f.y, vector4f.z);
    }

    public static CasingEjector fromId(int id) {
        return mappings.get(id);
    }

    @Override
    public CasingEjector clone() {
        try {
            return (CasingEjector) super.clone();
        } catch(CloneNotSupportedException e) {
            return new CasingEjector();
        }
    }
}
