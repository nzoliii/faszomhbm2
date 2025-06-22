package com.hbm.render.util;

import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class BeamPronter {

    private static boolean depthMask = false;

    public static void prontBeamwithDepth(Vec3 skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
        depthMask = true;
        prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness);
        depthMask = false;
    }

    public static void prontBeam(Vec3 skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {

        GL11.glPushMatrix();
        GL11.glDepthMask(depthMask);

        float sYaw = (float) (Math.atan2(skeleton.xCoord, skeleton.zCoord) * 180F / Math.PI);
        float sqrt = MathHelper.sqrt(skeleton.xCoord * skeleton.xCoord + skeleton.zCoord * skeleton.zCoord);
        float sPitch = (float) (Math.atan2(skeleton.yCoord, sqrt) * 180F / Math.PI);

        GL11.glRotatef(180, 0, 1F, 0);
        GL11.glRotatef(sYaw, 0, 1F, 0);
        GL11.glRotatef(sPitch - 90, 1F, 0, 0);

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (beam == EnumBeamType.SOLID) {
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Vec3 unit = Vec3.createVectorHelper(0, 1, 0);
        Random rand = new Random(start);
        double length = skeleton.length();
        double segLength = length / segments;
        double lastX = 0;
        double lastY = 0;
        double lastZ = 0;

        for (int i = 0; i <= segments; i++) {
            Vec3 spinner = Vec3.createVectorHelper(size, 0, 0);

            if (wave == EnumWaveType.SPIRAL) {
                spinner.rotateAroundY((float) Math.PI * (float) start / 180F);
                spinner.rotateAroundY((float) Math.PI * 45F / 180F * i);
            } else if (wave == EnumWaveType.RANDOM) {
                spinner.rotateAroundY((float) Math.PI * 2 * rand.nextFloat());
                spinner.rotateAroundY((float) Math.PI * 2 * rand.nextFloat());
            }

            double pX = unit.xCoord * segLength * i + spinner.xCoord;
            double pY = unit.yCoord * segLength * i + spinner.yCoord;
            double pZ = unit.zCoord * segLength * i + spinner.zCoord;

            if (beam == EnumBeamType.LINE && i > 0) {
                buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                addVertex(buffer, pX, pY, pZ, outerColor);
                addVertex(buffer, lastX, lastY, lastZ, outerColor);
                tessellator.draw();
            }

            if (beam == EnumBeamType.SOLID && i > 0) {
                float radius = thickness / layers;

                for (int j = 1; j <= layers; j++) {
                    float inter = (float) (j - 1) / (float) (layers - 1);

                    int r1 = (outerColor >> 16) & 0xFF;
                    int g1 = (outerColor >> 8) & 0xFF;
                    int b1 = outerColor & 0xFF;

                    int r2 = (innerColor >> 16) & 0xFF;
                    int g2 = (innerColor >> 8) & 0xFF;
                    int b2 = innerColor & 0xFF;

                    int r = (int) (r1 + (r2 - r1) * inter);
                    int g = (int) (g1 + (g2 - g1) * inter);
                    int b = (int) (b1 + (b2 - b1) * inter);

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                    // Quad 1
                    buffer.pos(lastX + radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX + radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 2
                    buffer.pos(lastX - radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 3
                    buffer.pos(lastX + radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ + radius * j).color(r, g, b, 255).endVertex();

                    // Quad 4
                    buffer.pos(lastX + radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(lastX - radius * j, lastY, lastZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX - radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();
                    buffer.pos(pX + radius * j, pY, pZ - radius * j).color(r, g, b, 255).endVertex();

                    tessellator.draw();
                }
            }

            lastX = pX;
            lastY = pY;
            lastZ = pZ;
        }

        if (beam == EnumBeamType.LINE) {
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            addVertex(buffer, 0, 0, 0, innerColor);
            addVertex(buffer, 0, skeleton.length(), 0, innerColor);
            tessellator.draw();
        }

        if (beam == EnumBeamType.SOLID) {
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glPopMatrix();

    }

    private static void addVertex(BufferBuilder buffer, double x, double y, double z, int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        buffer.pos(x, y, z).color(r, g, b, 255).endVertex();
    }

    public static enum EnumWaveType {
        RANDOM, SPIRAL
    }

    public static enum EnumBeamType {
        SOLID, LINE
    }


}
