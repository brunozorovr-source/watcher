package com.watcher;

import java.util.Random;

/** Labirinto aleatorio (backtracker) com uma sala grande longe da entrada. */
public class Labyrinth {
    public static final int N = 35;            // celulas por lado
    public static final int SIZE = 2 * N + 1;  // 71 blocos

    public final int x0, z0, floorY;
    public final boolean[][] wall = new boolean[SIZE][SIZE];
    public final int rx0, rz0, rx1, rz1;       // interior da sala (indices do grid)

    public Labyrinth(Random r, int x0, int z0, int floorY) {
        this.x0 = x0;
        this.z0 = z0;
        this.floorY = floorY;

        for (int i = 0; i < SIZE; i++) for (int j = 0; j < SIZE; j++) wall[i][j] = true;

        boolean[][] vis = new boolean[N][N];
        int[] stack = new int[N * N];
        int sp = 0;
        int[] dx = {1, -1, 0, 0};
        int[] dz = {0, 0, 1, -1};
        stack[sp++] = 0;
        vis[0][0] = true;
        wall[1][1] = false;
        while (sp > 0) {
            int cur = stack[sp - 1];
            int cx = cur % N, cz = cur / N;
            int[] opts = new int[4];
            int n = 0;
            for (int d = 0; d < 4; d++) {
                int nx = cx + dx[d], nz = cz + dz[d];
                if (nx >= 0 && nz >= 0 && nx < N && nz < N && !vis[nx][nz]) opts[n++] = d;
            }
            if (n == 0) { sp--; continue; }
            int d = opts[r.nextInt(n)];
            int nx = cx + dx[d], nz = cz + dz[d];
            vis[nx][nz] = true;
            wall[1 + 2 * cx + dx[d]][1 + 2 * cz + dz[d]] = false;
            wall[1 + 2 * nx][1 + 2 * nz] = false;
            stack[sp++] = nx + nz * N;
        }

        int rw = 6, cxr, czr;
        do {
            cxr = r.nextInt(N - rw + 1);
            czr = r.nextInt(N - rw + 1);
        } while (cxr + czr < 30);
        rx0 = 2 * cxr + 1;
        rz0 = 2 * czr + 1;
        rx1 = 2 * (cxr + rw) - 1;
        rz1 = 2 * (czr + rw) - 1;
        for (int i = rx0; i <= rx1; i++) for (int j = rz0; j <= rz1; j++) wall[i][j] = false;
    }

    /** Sala + anel de parede ao redor (teto mais alto). */
    public boolean roomArea(int gx, int gz) {
        return gx >= rx0 - 1 && gx <= rx1 + 1 && gz >= rz0 - 1 && gz <= rz1 + 1;
    }

    public int roomX() { return x0 + (rx0 + rx1) / 2; }
    public int roomZ() { return z0 + (rz0 + rz1) / 2; }

    public boolean inside(int x, int y, int z) {
        return x >= x0 && x < x0 + SIZE && z >= z0 && z < z0 + SIZE && y >= floorY && y <= floorY + 6;
    }
}
