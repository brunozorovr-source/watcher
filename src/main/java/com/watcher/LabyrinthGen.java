package com.watcher;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class LabyrinthGen implements IWorldGenerator {
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator gen, IChunkProvider prov) {
        if (world.provider.getDimension() != 0) return;
        int bx = chunkX << 4, bz = chunkZ << 4;
        Labyrinth lab = Labyrinths.inRegion(world.getSeed(), bx, bz);
        if (lab == null) return;

        int xs = Math.max(bx, lab.x0), xe = Math.min(bx + 15, lab.x0 + Labyrinth.SIZE - 1);
        int zs = Math.max(bz, lab.z0), ze = Math.min(bz + 15, lab.z0 + Labyrinth.SIZE - 1);
        if (xs > xe || zs > ze) return;

        IBlockState brick = Blocks.BRICK_BLOCK.getDefaultState();
        IBlockState air = Blocks.AIR.getDefaultState();
        int fy = lab.floorY;

        for (int x = xs; x <= xe; x++) {
            for (int z = zs; z <= ze; z++) {
                int gx = x - lab.x0, gz = z - lab.z0;
                boolean wall = lab.wall[gx][gz];
                int h = lab.roomArea(gx, gz) ? 5 : 3;
                world.setBlockState(new BlockPos(x, fy, z), brick, 2);
                for (int i = 1; i <= h; i++) {
                    world.setBlockState(new BlockPos(x, fy + i, z), wall ? brick : air, 2);
                }
                world.setBlockState(new BlockPos(x, fy + h + 1, z), brick, 2);
            }
        }
    }
}
