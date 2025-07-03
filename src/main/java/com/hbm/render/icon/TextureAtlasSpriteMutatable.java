package com.hbm.render.icon;

import com.google.common.collect.Lists;
import com.hbm.main.MainRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.utils.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class TextureAtlasSpriteMutatable extends TextureAtlasSprite {

    private RGBMutator mutator;
    public String basePath = "textures";
    private int mipmap = 0;

    public TextureAtlasSpriteMutatable(String iconName, RGBMutator mutator) {
        super(iconName);
        this.mutator = mutator;
    }


    @Override
    public void loadSpriteFrames(IResource resource, int mipmapLevels) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(resource.getInputStream());
        if(bufferedImage == null) {
            MainRegistry.logger.warn("Failed to load texture " + this.getIconName() + " from " + resource.getResourceLocation());
        }
        AnimationMetadataSection animationMetadataSection = resource.getMetadata("animation");
        if(animationMetadataSection == null) {
            MainRegistry.logger.warn("No animation metadata found for " + this.getIconName());
        }
        int[][] frameData = new int[mipmapLevels][];
        frameData[0] = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), frameData[0], 0, bufferedImage.getWidth());

        // Apply mutator if it exists
        if (mutator != null) {
            for (int i = 0; i < mipmapLevels; i++) {
                if (frameData[i] != null) {
                    BufferedImage frame = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
                    frame.setRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), frameData[i], 0, bufferedImage.getWidth());
                    mutator.mutate(frame, i, mipmapLevels);
                    frame.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), frameData[i], 0, bufferedImage.getWidth());
                }
            }
        }

        if (animationMetadataSection == null) {
            this.framesTextureData.add(frameData);
        } else {
            int frameCount = bufferedImage.getHeight() / this.width;

            if (animationMetadataSection.getFrameCount() > 0) {
                for (int frameIndex : animationMetadataSection.getFrameIndexSet()) {
                    if (frameIndex >= frameCount) {
                        throw new RuntimeException("Invalid frame index " + frameIndex);
                    }

                    this.allocateFrameTextureData(frameIndex);
                    this.framesTextureData.set(frameIndex, getFrameTextureData(frameData, this.width, this.width, frameIndex));
                }

                this.animationMetadata = animationMetadataSection;
            } else {
                List<AnimationFrame> frames = Lists.newArrayList();

                for (int i = 0; i < frameCount; ++i) {
                    this.framesTextureData.add(getFrameTextureData(frameData, this.width, this.width, i));
                    frames.add(new AnimationFrame(i, -1));
                }

                this.animationMetadata = new AnimationMetadataSection(frames, this.width, this.height, animationMetadataSection.getFrameTime(), animationMetadataSection.isInterpolate());
            }
        }
    }

    private static int[][] getFrameTextureData(int[][] data, int width, int height, int frame) {
        int[][] result = new int[data.length][];
        for (int i = 0; i < data.length; ++i) {
            int[] pixels = data[i];
            if (pixels != null) {
                result[i] = new int[(width >> i) * (height >> i)];
                System.arraycopy(pixels, frame * result[i].length, result[i], 0, result[i].length);
            }
        }
        return result;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true; //YES!
    }

    @Override
    public boolean load(IResourceManager man, ResourceLocation resourcelocation, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {

        String pathName = resourcelocation.getPath();
        String undashedPath = pathName.substring(0, pathName.indexOf('-')); // remove the dash and everything trailing it
        String truncatedPath = undashedPath.substring(undashedPath.indexOf('/') + 1); // remove the slash and everything before it
        resourcelocation = new ResourceLocation(resourcelocation.getNamespace(), truncatedPath);
        ResourceLocation resourcelocation1 = this.completeResourceLocation(resourcelocation);
        MainRegistry.logger.info("Loading texture " + resourcelocation1);

        IResource iresource = null;
        try {
            iresource = man.getResource(resourcelocation1);

            PngSizeInfo pngSizeInfo = PngSizeInfo.makeFromResource(iresource);
            boolean hasAnimation = iresource.getMetadata("animation") != null;
            this.loadSprite(pngSizeInfo, hasAnimation);
            this.mipmap = Minecraft.getMinecraft().getTextureMapBlocks().getMipmapLevels()+1;

            // Load the sprite frames directly
            iresource = man.getResource(resourcelocation1);//Norwood: Yeah ofc the fucking stream needs to get closed automatically after every method I call, why not
            //TOTALLY HASN'T MADE ME SPEND 3 FUCKING DAYS DEBUGGING IT
            this.loadSpriteFrames(iresource, this.mipmap);

        } catch (RuntimeException | IOException e) {
            net.minecraftforge.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation1, e.getMessage());
            return true;
        } finally {
            IOUtils.closeQuietly(iresource);
        }

        return false; //FALSE! prevents vanilla loading (we just did that ourselves)
    }

    //whatever the fuck this is
    private ResourceLocation completeResourceLocation(ResourceLocation loc) {
        return new ResourceLocation(loc.getNamespace(), String.format("%s/%s%s", new Object[] { this.basePath, loc.getPath(), ".png" }));
    }

    private void allocateFrameTextureData(int index)
    {
        if (this.framesTextureData.size() <= index)
        {
            for (int i = this.framesTextureData.size(); i <= index; ++i)
            {
                this.framesTextureData.add(null);
            }
        }
    }
}
