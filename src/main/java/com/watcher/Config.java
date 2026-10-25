package com.watcher;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class Config {
    public static float spawnChance = 0.03F;     // chance por segundo de aparecer uma figura
    public static float stepChance = 0.45F;      // chance de ouvir o passo extra ao parar de andar
    public static float controlChance = 0.0012F; // chance por segundo de "controlar" o player (~1x a cada 14 min)
    public static int labyrinthChance = 70;      // % de regioes (256x256) que tem labirinto
    public static boolean crash = true;          // crashar ao chegar perto da figura da sala

    public static void load(File f) {
        Configuration c = new Configuration(f);
        spawnChance = c.getFloat("spawnChance", "general", spawnChance, 0F, 1F, "");
        stepChance = c.getFloat("stepChance", "general", stepChance, 0F, 1F, "");
        controlChance = c.getFloat("controlChance", "general", controlChance, 0F, 1F, "");
        labyrinthChance = c.getInt("labyrinthChance", "general", labyrinthChance, 0, 100, "");
        crash = c.getBoolean("crash", "general", crash, "");
        if (c.hasChanged()) c.save();
    }
}
