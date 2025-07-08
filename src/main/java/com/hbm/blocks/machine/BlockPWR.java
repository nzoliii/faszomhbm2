package com.hbm.blocks.machine;

import api.hbm.fluid.IFluidConnector;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.ForgeDirection;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.machine.TileEntityPWRController;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

//MrNorwood: Oh my fucking god fristie,this dogshit should be thrown the fuck out
public class BlockPWR extends BlockContainerBakeable implements ILookOverlay {

    public static final PropertyBool IO_ENABLED = PropertyBool.create("io");
    private final BlockBakeFrame portFrame;

    public BlockPWR(Material mat, String name, String portTexture) {
        super(mat, name, new BlockBakeFrame(BlockBakeFrame.BlockForm.ALL, name));
        this.portFrame = new BlockBakeFrame(BlockBakeFrame.BlockForm.ALL, portTexture);
        this.setDefaultState(this.blockState.getBaseState().withProperty(IO_ENABLED, false));
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, IO_ENABLED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(IO_ENABLED) ? 1 : 0;
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(IO_ENABLED, meta != 0);
    }

    @NotNull
    @Override
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @NotNull
    @Override
    public Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return Items.AIR; // Does not drop itself; structure breaks.
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        return new TileEntityBlockPWR();
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);

        if (tile instanceof TileEntityBlockPWR pwr) {
            // If the block had a core, try to find the controller and mark it as disassembled.
            if (pwr.corePos != null) {
                worldIn.setBlockState(pos, pwr.originalBlockState, 3); // Restore original block
                TileEntity controller = worldIn.getTileEntity(pwr.corePos);
                if (controller instanceof TileEntityPWRController) {
                    ((TileEntityPWRController) controller).assembled = false;
                }
            }
        }
        // Ensure the tile entity is removed AFTER we've used it.
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        bakeStateModel(event, this.blockFrame, "io=false");
        bakeStateModel(event, this.portFrame, "io=true");
        try {
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
            IBakedModel bakedModel = model.retexture(ImmutableMap.of("all", blockFrame.getSpriteLoc(0).toString()))
                    .bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
            ModelResourceLocation invLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(invLocation, bakedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Block getBlock() {
        return this;
    }

//    @Override
//    public StateMapperBase getStateMapper(ResourceLocation loc) {
//        return super.getStateMapper(loc);
//    }

    private void bakeStateModel(ModelBakeEvent event, BlockBakeFrame frame, String variant) {
        try {
            IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(frame.getBaseModel()));
            IBakedModel bakedModel = model.retexture(ImmutableMap.of("all", frame.getSpriteLoc(0).toString()))
                    .bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
            ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), variant);
            event.getModelRegistry().putObject(modelLocation, bakedModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerSprite(TextureMap map){
        super.registerSprite(map);
        this.portFrame.registerBlockTextures(map);
    }

    @Override
    public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
        TileEntity tePWR = world.getTileEntity(new BlockPos(x, y, z));
        List<String> text = new ArrayList<>();
        if (!(tePWR instanceof TileEntityBlockPWR blockPWR)) return;
        if (!(blockPWR.corePos != null && world.getTileEntity(blockPWR.corePos) instanceof TileEntityPWRController controller)) {
            text.add("No core detected.");
        } else {
            text.add("Core: " + controller.getPos());
            text.add("Assembled: " + controller.assembled);
        }
        text.add("IO: " + world.getBlockState(blockPWR.getPos()).getValue(IO_ENABLED));
        text.add("originalBlockstate: " + blockPWR.originalBlockState);
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    public static class TileEntityBlockPWR extends TileEntity implements ITickable, IFluidHandler, IFluidConnector {

        public IBlockState originalBlockState;
        public BlockPos corePos;
        private TileEntityPWRController cachedCore;

        @Override
        public void update() {
            if (!world.isRemote && corePos != null && world.getTotalWorldTime() % 20 == 0) {
                TileEntityPWRController controller = getCore();
                if (controller == null || !controller.assembled) {
                    world.setBlockToAir(pos);
                }
            }
        }

        /**
         * Finds and caches the core TileEntityPWRController.
         */
        @Nullable
        private TileEntityPWRController getCore() {
            if (corePos == null) return null;
            if (cachedCore != null && !cachedCore.isInvalid() && cachedCore.getPos().equals(corePos)) {
                return cachedCore;
            }
            if (world.isBlockLoaded(corePos)) {
                TileEntity tile = world.getTileEntity(corePos);
                if (tile instanceof TileEntityPWRController) {
                    cachedCore = (TileEntityPWRController) tile;
                    return cachedCore;
                }
            }
            cachedCore = null;
            return null;
        }

        @Override
        public void readFromNBT(@NotNull NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            if (nbt.hasKey("originalBlockState")) {
                originalBlockState = NBTUtil.readBlockState(nbt.getCompoundTag("originalBlockState"));
            }
            if (nbt.hasKey("corePos")) {
                corePos = NBTUtil.getPosFromTag(nbt.getCompoundTag("corePos"));
            }
        }

        @NotNull
        @Override
        public NBTTagCompound writeToNBT(@NotNull NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            if (originalBlockState != null) {
                nbt.setTag("originalBlockState", NBTUtil.writeBlockState(new NBTTagCompound(), originalBlockState));
            }
            if (corePos != null) {
                nbt.setTag("corePos", NBTUtil.createPosTag(corePos));
            }
            return nbt;
        }

        @NotNull
        @Override
        public NBTTagCompound getUpdateTag() {
            return this.writeToNBT(new NBTTagCompound());
        }

        @Override
        public void handleUpdateTag(@NotNull NBTTagCompound tag) {
            this.readFromNBT(tag);
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            handleUpdateTag(pkt.getNbtCompound());
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            if (world.getBlockState(pos).getValue(IO_ENABLED)) {
                TileEntityPWRController core = getCore();
                if (core != null) {
                    return core.hasCapability(capability, facing);
                }
            }
            return super.hasCapability(capability, facing);
        }

        @Nullable
        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            if (world.getBlockState(pos).getValue(IO_ENABLED)) {
                TileEntityPWRController core = getCore();
                if (core != null) {
                    return core.getCapability(capability, facing);
                }
            }
            return super.getCapability(capability, facing);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            TileEntityPWRController core = getCore();
            if (core != null && world.getBlockState(pos).getValue(IO_ENABLED)) {
                IFluidHandler handler = core.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) return handler.getTankProperties();
            }
            return new IFluidTankProperties[0];
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            TileEntityPWRController core = getCore();
            if (core != null && world.getBlockState(pos).getValue(IO_ENABLED)) {
                IFluidHandler handler = core.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) return handler.fill(resource, doFill);
            }
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            TileEntityPWRController core = getCore();
            if (core != null && world.getBlockState(pos).getValue(IO_ENABLED)) {
                IFluidHandler handler = core.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) return handler.drain(resource, doDrain);
            }
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            TileEntityPWRController core = getCore();
            if (core != null && world.getBlockState(pos).getValue(IO_ENABLED)) {
                IFluidHandler handler = core.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) return handler.drain(maxDrain, doDrain);
            }
            return null;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public long transferFluid(FluidType type, int pressure, long fluid) {

            if(!world.getBlockState(pos).getValue(IO_ENABLED)) return fluid;

            if(getCore() != null) {
                return getCore().transferFluid(type, pressure, fluid);
            }
            return fluid;
        }

        @Override
        public long getDemand(FluidType type, int pressure) {
            if(!world.getBlockState(pos).getValue(IO_ENABLED)) return 0;
            if(getCore() != null) {
                return getCore().getDemand(type, pressure);
            }
            return 0;
        }

        @Override
        public boolean canConnect(FluidType type, ForgeDirection dir) {
            if(!world.getBlockState(pos).getValue(IO_ENABLED)) return false;
            if(getCore() != null) {
                return getCore().canConnect(type, dir);
            }
            return true;
        }
    }
}