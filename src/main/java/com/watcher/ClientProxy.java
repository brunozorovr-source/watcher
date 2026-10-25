package com.watcher;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher.class, new IRenderFactory<EntityWatcher>() {
            @Override
            public Render<? super EntityWatcher> createRenderFor(RenderManager manager) {
                return new RenderWatcher(manager);
            }
        });
    }
}
