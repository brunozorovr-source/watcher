package com.watcher;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = WatcherMod.MODID, name = "Watcher", version = "1.0", acceptedMinecraftVersions = "[1.10.2]")
public class WatcherMod {
    public static final String MODID = "watcher";

    @Mod.Instance(MODID)
    public static WatcherMod instance;

    @SidedProxy(clientSide = "com.watcher.ClientProxy", serverSide = "com.watcher.CommonProxy")
    public static CommonProxy proxy;

    public static WatcherEvents events;
    /** Quando preenchido, o proximo tick do servidor derruba o jogo. */
    public static volatile String crashNick = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        Config.load(e.getSuggestedConfigurationFile());
        EntityRegistry.registerModEntity(EntityWatcher.class, "watcher", 0, this, 128, 1, false);
        proxy.registerRenderers();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        events = new WatcherEvents();
        MinecraftForge.EVENT_BUS.register(events);
        GameRegistry.registerWorldGenerator(new LabyrinthGen(), 0);
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent e) {
        e.registerServerCommand(new CommandWatcher());
    }
}
