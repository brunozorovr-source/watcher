package com.watcher;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityWatcher extends EntityLiving {
    /** true = figura parada na sala grande do labirinto (nao some ao ser vista). */
    public boolean guard = false;

    public EntityWatcher(World w) {
        super(w);
        setSize(0.6F, 1.95F);
        isImmuneToFire = true;
        enablePersistence();
    }

    @Override protected boolean canDespawn() { return false; }
    @Override public boolean isEntityInvulnerable(DamageSource s) { return true; }
    @Override public boolean canBePushed() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override protected void collideWithEntity(Entity e) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound t) {
        super.writeEntityToNBT(t);
        t.setBoolean("Guard", guard);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound t) {
        super.readEntityFromNBT(t);
        guard = t.getBoolean("Guard");
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (worldObj.isRemote) return;

        EntityPlayer p = worldObj.getClosestPlayerToEntity(this, 96.0D);
        if (p == null) {
            if (!guard) setDead();
            return;
        }

        // sempre virada pra voce
        faceEntity(p, 360F, 360F);
        rotationYawHead = rotationYaw;
        renderYawOffset = rotationYaw;

        double dist = getDistanceToEntity(p);

        if (guard) {
            if (Config.crash && dist < 6.5D && Math.abs(p.posY - posY) < 4.0D && !p.isSpectator()) {
                WatcherMod.crashNick = p.getName();
            }
            return;
        }

        if (ticksExisted > 900 || dist < 7.0D) {
            setDead();
            return;
        }

        if (isLookedAt(p)) {
            setDead();
            WatcherEvents.markSeen(p);
        }
    }

    private boolean isLookedAt(EntityPlayer p) {
        Vec3d look = p.getLook(1.0F).normalize();
        Vec3d eye = new Vec3d(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        Vec3d target = new Vec3d(posX, posY + 0.9D, posZ);
        Vec3d to = target.subtract(eye).normalize();
        if (look.dotProduct(to) < 0.98D) return false;
        return worldObj.rayTraceBlocks(eye, target, false, true, false) == null;
    }
}
