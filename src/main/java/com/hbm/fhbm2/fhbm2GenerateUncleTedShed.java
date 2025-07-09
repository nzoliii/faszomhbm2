package com.hbm.fhbm2;

import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class fhbm2GenerateUncleTedShed implements IWorldGenerator {

    private static final int BASE_SPAWN_CHANCE = 1;

    private static final Set<String> ALLOWED_BIOMES = new HashSet<>();
    static {
        ALLOWED_BIOMES.add("minecraft:taiga");
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, net.minecraft.world.gen.IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        BlockPos position = new BlockPos(x, 0, z);

        if (random.nextInt(100) >= BASE_SPAWN_CHANCE) return;

        Biome biome = world.getBiome(position);
        if (biome.getRegistryName() != null && ALLOWED_BIOMES.contains(biome.getRegistryName().toString())) {
            position = findGround(world, position);

            if (position != null && isGrassBlock(world, position.down())) {
                placeStructure(world, position);
            }
        }
    }

    private BlockPos findGround(World world, BlockPos position) {
        for (int y = world.getHeight(position.getX(), position.getZ()); y > 0; y--) {
            BlockPos checkPos = new BlockPos(position.getX(), y, position.getZ());
            if (world.getBlockState(checkPos).getMaterial().isSolid() &&
                    world.getBlockState(checkPos.up()).getMaterial().isReplaceable()) {
                return checkPos.up();
            }
        }
        return null;
    }

    private boolean isGrassBlock(World world, BlockPos position) {
        return world.getBlockState(position).getBlock().getRegistryName().toString().contains("grass");
    }

    private void placeStructure(World world, BlockPos position) {
        System.out.println("Placing Unabomber Shed at: " + position);

        TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
        ResourceLocation location = new ResourceLocation("hbm", "unabomber_shed");
        Template template = manager.getTemplate(world.getMinecraftServer(), location);

        if (template != null) {
            PlacementSettings settings = new PlacementSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(Rotation.NONE)
                    .setIgnoreEntities(false);

            template.addBlocksToWorld(world, position, settings);
        } else {
            System.err.println("Structure template 'unabomber_shed' not found! Make sure the file is in assets/hbm/structures/");
        }
    }
}
