package com.watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Decide, a partir da seed do mundo, onde ficam os labirintos (1 por regiao de 256x256, ou nenhum). */
public class Labyrinths {
    public static final int REGION = 256;
    private static final Map<String, Labyrinth> CACHE = new HashMap<String, Labyrinth>();

    public static synchronized Labyrinth get(long seed, int rx, int rz) {
        String key = seed + ":" + rx + ":" + rz;
        if (CACHE.containsKey(key)) return CACHE.get(key);
        long s = seed ^ (rx * 341873128712L) ^ (rz * 132897987541L) ^ 0x5DEECE66DL;
        Random r = new Random(s);
        r.nextInt();
        r.nextInt();
        Labyrinth l = null;
        if (r.nextInt(100) < Config.labyrinthChance) {
            int ox = rx * REGION + 8 + r.nextInt(REGION - Labyrinth.SIZE - 16);
            int oz = rz * REGION + 8 + r.nextInt(REGION - Labyrinth.SIZE - 16);
            int fy = 14 + r.nextInt(20);
            l = new Labyrinth(r, ox, oz, fy);
        }
        CACHE.put(key, l);
        return l;
    }

    public static Labyrinth inRegion(long seed, int blockX, int blockZ) {
        return get(seed, Math.floorDiv(blockX, REGION), Math.floorDiv(blockZ, REGION));
    }

    public static Labyrinth nearest(long seed, int blockX, int blockZ) {
        int rx = Math.floorDiv(blockX, REGION), rz = Math.floorDiv(blockZ, REGION);
        Labyrinth best = null;
        double bd = Double.MAX_VALUE;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Labyrinth l = get(seed, rx + i, rz + j);
                if (l == null) continue;
                double dx = l.roomX() - blockX, dz = l.roomZ() - blockZ;
                double d = dx * dx + dz * dz;
                if (d < bd) { bd = d; best = l; }
            }
        }
        return best;
    }
}
