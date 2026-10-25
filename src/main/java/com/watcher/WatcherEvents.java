package com.watcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemFood;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.block.SoundType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class WatcherEvents {
    static final String FLAG = "watcherChat";

    private final Random rand = new Random();
    private final Map<UUID, PState> states = new HashMap<UUID, PState>();
    private final List<Pending> pending = new ArrayList<Pending>();
    private long tick = 0;

    private static class PState {
        double lx, lz;
        int walkTicks, stillTicks, stepDelay = -1;
        boolean moving;
        long lastBreak = -9999, lastPlace = -9999, lastAttack = -9999;
        Material lastMat = Material.AIR;
        long nextTalk = 0, nextMorse = 0;
    }

    private static class Pending {
        final UUID id; final long due; final String text;
        Pending(UUID id, long due, String text) { this.id = id; this.due = due; this.text = text; }
    }

    // ------------------------------------------------------------------ helpers

    static NBTTagCompound persisted(EntityPlayer p) {
        NBTTagCompound d = p.getEntityData();
        if (!d.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) d.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        return d.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    static boolean isChatOn(EntityPlayer p) { return persisted(p).getBoolean(FLAG); }

    /** Chamado quando o player olha pra figura (ela some e a "coisa" passa a falar no chat). */
    public static void markSeen(EntityPlayer p) { persisted(p).setBoolean(FLAG, true); }

    static void setChat(EntityPlayer p, boolean on) { persisted(p).setBoolean(FLAG, on); }

    private PState state(EntityPlayer p) {
        PState s = states.get(p.getUniqueID());
        if (s == null) {
            s = new PState();
            s.lx = p.posX; s.lz = p.posZ;
            states.put(p.getUniqueID(), s);
        }
        return s;
    }

    private void say(EntityPlayerMP p, String text) {
        p.addChatMessage(new TextComponentString("<> " + text));
    }

    // ------------------------------------------------------------------ events

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        tick++;

        if (WatcherMod.crashNick != null) {
            String nick = WatcherMod.crashNick;
            WatcherMod.crashNick = null;
            CrashReport r = new CrashReport("Unexpected system error: EXCEPTION_ACCESS_VIOLATION (0xC0000005)",
                    new Error("The instruction at 0x00007FFE4B2C1A90 referenced memory at 0x0000000000000000. The memory could not be read."));
            CrashReportCategory c = r.makeCategory("Observador");
            c.addCrashSection("Alvo", nick);
            c.addCrashSection("Distancia", "0");
            throw new ReportedException(r);
        }

        if (pending.isEmpty()) return;
        MinecraftServer srv = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (srv == null) return;
        Iterator<Pending> it = pending.iterator();
        while (it.hasNext()) {
            Pending pd = it.next();
            if (tick < pd.due) continue;
            it.remove();
            EntityPlayerMP pl = srv.getPlayerList().getPlayerByUUID(pd.id);
            if (pl != null) say(pl, pd.text);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.side != Side.SERVER) return;
        if (!(e.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP p = (EntityPlayerMP) e.player;
        if (p.isSpectator()) return;
        PState s = state(p);
        footsteps(p, s);
        if (tick % 20 == p.getEntityId() % 20) slowTick(p, s);
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent e) {
        EntityPlayerMP p = e.getPlayer();
        if (!isChatOn(p)) return;
        PState s = state(p);
        String word = Brain.reply(p, e.getMessage(), activity(p, s));
        pending.add(new Pending(p.getUniqueID(), tick + 25 + rand.nextInt(35), word));
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent e) {
        if (e.getWorld().isRemote || e.getPlayer() == null) return;
        PState s = state(e.getPlayer());
        s.lastBreak = tick;
        s.lastMat = e.getState().getMaterial();
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent e) {
        if (e.getWorld().isRemote || e.getPlayer() == null) return;
        state(e.getPlayer()).lastPlace = tick;
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (e.getEntityPlayer().worldObj.isRemote) return;
        state(e.getEntityPlayer()).lastAttack = tick;
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        states.remove(e.player.getUniqueID());
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone e) {
        persisted(e.getEntityPlayer()).merge(persisted(e.getOriginal()));
    }

    // ------------------------------------------------------------------ passos atras de voce

    private void footsteps(EntityPlayerMP p, PState s) {
        double dx = p.posX - s.lx, dz = p.posZ - s.lz;
        boolean moved = dx * dx + dz * dz > 0.002D;
        s.lx = p.posX;
        s.lz = p.posZ;
        s.moving = moved;

        if (moved) {
            if (p.onGround) s.walkTicks++;
            s.stillTicks = 0;
            s.stepDelay = -1;
            return;
        }

        s.stillTicks++;
        if (s.stillTicks == 3) {
            if (s.walkTicks >= 20 && p.onGround && !p.isInWater() && rand.nextFloat() < Config.stepChance) {
                s.stepDelay = 12 + rand.nextInt(18);
            }
            s.walkTicks = 0;
        }

        if (s.stepDelay > 0) {
            s.stepDelay--;
        } else if (s.stepDelay == 0) {
            s.stepDelay = -1;
            playStepBehind(p);
        }
    }

    private void playStepBehind(EntityPlayerMP p) {
        World w = p.worldObj;
        BlockPos under = new BlockPos(p.posX, p.posY - 0.2D, p.posZ);
        IBlockState bs = w.getBlockState(under);
        if (bs.getMaterial() == Material.AIR) return;
        SoundType t = bs.getBlock().getSoundType();
        SoundEvent snd = t.getStepSound();
        double yaw = Math.toRadians(p.rotationYaw);
        double bx = p.posX + Math.sin(yaw) * 2.5D;   // oposto de onde voce olha
        double bz = p.posZ - Math.cos(yaw) * 2.5D;
        w.playSound(null, bx, p.posY, bz, snd, SoundCategory.PLAYERS, t.getVolume() * 0.5F, t.getPitch());
    }

    // ------------------------------------------------------------------ tick de 1 segundo por player

    private void slowTick(EntityPlayerMP p, PState s) {
        World w = p.worldObj;
        if (w.provider.getDimension() != 0) return;

        int px = (int) Math.floor(p.posX), py = (int) Math.floor(p.posY), pz = (int) Math.floor(p.posZ);
        Labyrinth lab = Labyrinths.inRegion(w.getSeed(), px, pz);
        boolean inside = lab != null && lab.inside(px, py, pz);

        // figura observando de longe
        if (!inside && rand.nextFloat() < Config.spawnChance) trySpawnWatcher(p);

        // figura da sala grande
        if (lab != null) ensureGuard(p, lab);

        // morse com o nick do player
        if (inside) {
            if (s.nextMorse == 0) {
                s.nextMorse = tick + 100;
            } else if (tick >= s.nextMorse) {
                say(p, Morse.encode(p.getName()));
                s.nextMorse = tick + 20L * (10 + rand.nextInt(30));
            }
        } else {
            s.nextMorse = 0;
        }

        // o mod assume o controle (bem raro)
        if (rand.nextFloat() < Config.controlChance && p.openContainer == p.inventoryContainer
                && !p.isPlayerSleeping() && !p.isRiding()) {
            takeControl(p);
        }

        // a "coisa" fala sozinha
        if (isChatOn(p)) {
            if (s.nextTalk == 0) {
                s.nextTalk = tick + 20L * (40 + rand.nextInt(80));
            } else if (tick >= s.nextTalk) {
                say(p, talkLine(p, s));
                s.nextTalk = tick + 20L * (60 + rand.nextInt(180));
            }
        }
    }

    private String talkLine(EntityPlayerMP p, PState s) {
        int r = rand.nextInt(10);
        if (r < 7) return "Você está " + activity(p, s) + ".";
        if (r == 7) return "Você ainda tem " + (int) p.getHealth() + " de vida.";
        if (r == 8) return "Atrás de você.";
        return p.getName() + ".";
    }

    String activity(EntityPlayerMP p, PState s) {
        if (p.openContainer instanceof ContainerWorkbench) return "craftando";
        if (p.openContainer instanceof ContainerFurnace) return "fundindo";
        if (p.openContainer instanceof ContainerChest) return "vasculhando";
        if (p.openContainer instanceof ContainerEnchantment) return "encantando";
        if (p.openContainer instanceof ContainerRepair) return "consertando";
        if (p.openContainer instanceof ContainerBrewingStand) return "preparando poções";
        if (p.openContainer instanceof ContainerMerchant) return "negociando";
        if (tick - s.lastAttack < 60) return "lutando";
        if (tick - s.lastBreak < 80) {
            if (s.lastMat == Material.ROCK || s.lastMat == Material.IRON) return "minerando";
            if (s.lastMat == Material.WOOD) return "cortando madeira";
            if (s.lastMat == Material.GROUND || s.lastMat == Material.SAND || s.lastMat == Material.GRASS
                    || s.lastMat == Material.CLAY) return "cavando";
            return "quebrando blocos";
        }
        if (tick - s.lastPlace < 80) return "construindo";
        if (p.isPlayerSleeping()) return "dormindo";
        if (p.isHandActive() && p.getActiveItemStack() != null && p.getActiveItemStack().getItem() instanceof ItemFood) return "comendo";
        if (p.isInWater()) return "nadando";
        if (p.isRiding()) return "montando";
        if (s.moving && p.isSprinting()) return "correndo";
        if (s.moving) return "andando";
        if (p.isSneaking()) return "agachado";
        return "parado";
    }

    // ------------------------------------------------------------------ controle do player

    private void takeControl(EntityPlayerMP p) {
        float yaw = p.rotationYaw, pitch = p.rotationPitch;
        switch (rand.nextInt(6)) {
            case 0: // vira pra tras
                p.playerNetServerHandler.setPlayerLocation(p.posX, p.posY, p.posZ, yaw + 180F, pitch);
                break;
            case 1: // olha pros proprios pes
                p.playerNetServerHandler.setPlayerLocation(p.posX, p.posY, p.posZ, yaw, 85F);
                break;
            case 2: // olha pro ceu
                p.playerNetServerHandler.setPlayerLocation(p.posX, p.posY, p.posZ, yaw, -85F);
                break;
            case 3: // da uns passos pra frente
                double r = Math.toRadians(yaw);
                p.motionX += -Math.sin(r) * 0.5D;
                p.motionZ += Math.cos(r) * 0.5D;
                p.velocityChanged = true;
                break;
            case 4: // pula
                if (p.onGround) p.jump();
                break;
            default: // congela por 1.5s
                p.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 10, false, false));
                break;
        }
    }

    // ------------------------------------------------------------------ spawn das figuras

    public boolean trySpawnWatcher(EntityPlayerMP p) {
        World w = p.worldObj;
        AxisAlignedBB area = p.getEntityBoundingBox().expandXyz(110.0D);
        for (EntityWatcher e : w.getEntitiesWithinAABB(EntityWatcher.class, area)) {
            if (!e.guard) return false; // ja tem uma observando
        }

        Vec3d eye = new Vec3d(p.posX, p.posY + p.getEyeHeight(), p.posZ);
        for (int attempt = 0; attempt < 10; attempt++) {
            double dist = 20.0D + rand.nextDouble() * 18.0D;
            double ang = Math.toRadians(p.rotationYaw + (rand.nextDouble() - 0.5D) * 140.0D);
            double x = p.posX - Math.sin(ang) * dist;
            double z = p.posZ + Math.cos(ang) * dist;
            int gy = findGround(w, (int) Math.floor(x), (int) Math.floor(z), (int) p.posY);
            if (gy < 0) continue;
            Vec3d target = new Vec3d(x, gy + 1.5D, z);
            if (w.rayTraceBlocks(eye, target, false, true, false) != null) continue;

            EntityWatcher e = new EntityWatcher(w);
            e.setLocationAndAngles(x, gy + 1, z, 0F, 0F);
            w.spawnEntityInWorld(e);
            return true;
        }
        return false;
    }

    private int findGround(World w, int x, int z, int startY) {
        for (int y = startY + 8; y >= startY - 8; y--) {
            BlockPos b = new BlockPos(x, y, z);
            if (w.getBlockState(b).getMaterial().isSolid() && w.isAirBlock(b.up()) && w.isAirBlock(b.up(2))) return y;
        }
        return -1;
    }

    private void ensureGuard(EntityPlayerMP p, Labyrinth lab) {
        World w = p.worldObj;
        int cx = lab.roomX(), cy = lab.floorY + 1, cz = lab.roomZ();
        double dx = p.posX - cx, dz = p.posZ - cz;
        if (dx * dx + dz * dz > 40 * 40 || Math.abs(p.posY - cy) > 25) return;
        BlockPos c = new BlockPos(cx, cy, cz);
        if (!w.isBlockLoaded(c)) return;
        if (w.getBlockState(c.down()).getBlock() != Blocks.BRICK_BLOCK || !w.isAirBlock(c)) return;

        AxisAlignedBB bb = new AxisAlignedBB(cx - 12, cy - 3, cz - 12, cx + 12, cy + 9, cz + 12);
        for (EntityWatcher e : w.getEntitiesWithinAABB(EntityWatcher.class, bb)) if (e.guard) return;

        EntityWatcher g = new EntityWatcher(w);
        g.guard = true;
        g.setLocationAndAngles(cx + 0.5D, cy, cz + 0.5D, 0F, 0F);
        w.spawnEntityInWorld(g);
    }
}
