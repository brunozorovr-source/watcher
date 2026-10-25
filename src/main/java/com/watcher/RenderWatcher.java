package com.watcher;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/** Figura preta lisa: sem nome, sem sombra, sem animacao de armadura. */
public class RenderWatcher extends RenderLiving<EntityWatcher> {
    private static final ResourceLocation TEX = new ResourceLocation(WatcherMod.MODID, "textures/entity/watcher.png");

    public RenderWatcher(RenderManager m) {
        super(m, new ModelBiped(), 0.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWatcher e) {
        return TEX;
    }

    @Override
    protected boolean canRenderName(EntityWatcher e) {
        return false;
    }
}
