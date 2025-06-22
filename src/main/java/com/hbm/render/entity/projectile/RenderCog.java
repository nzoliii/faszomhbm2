package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityCog;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.Nullable;

public class RenderCog extends Render<EntityCog> {

    public static final IRenderFactory<EntityCog> FACTORY = RenderCog::new;

    protected RenderCog(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityCog cog, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        int orientation = cog.getOrientation();
        switch (orientation % 6) {
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
        }

        GlStateManager.translate(0, 0, -1);

        if (orientation < 6) {
            double angle = (System.currentTimeMillis() % (360 * 3)) / 3.0;
            GlStateManager.rotate((float) angle, 0F, 0F, -1F);
        }

        GlStateManager.translate(0, -1.375, 0);

        this.bindEntityTexture(cog);
        ResourceManager.stirling.renderPart("Cog");

        GlStateManager.popMatrix();
    }

    @Override
    protected @Nullable ResourceLocation getEntityTexture(EntityCog entity) {
        int meta = entity.getMeta();
        return meta == 0 ? ResourceManager.stirling_tex : ResourceManager.stirling_steel_tex;
    }
}
