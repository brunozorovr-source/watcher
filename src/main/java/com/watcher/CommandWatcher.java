package com.watcher;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/** Comandos de teste (so OP). /watcher spawn | chat | lab | tp | room */
public class CommandWatcher extends CommandBase {
    @Override public String getCommandName() { return "watcher"; }
    @Override public String getCommandUsage(ICommandSender s) { return "/watcher <spawn|chat|lab|tp|room>"; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP p = getCommandSenderAsPlayer(sender);
        String a = args.length > 0 ? args[0] : "";
        if (a.equals("spawn")) {
            boolean ok = WatcherMod.events.trySpawnWatcher(p);
            msg(p, ok ? "spawned" : "sem espaco / ja existe uma");
        } else if (a.equals("chat")) {
            boolean on = !WatcherEvents.isChatOn(p);
            WatcherEvents.setChat(p, on);
            msg(p, "chat " + (on ? "ON" : "OFF"));
        } else if (a.equals("lab") || a.equals("tp") || a.equals("room")) {
            Labyrinth l = Labyrinths.nearest(p.worldObj.getSeed(), (int) p.posX, (int) p.posZ);
            if (l == null) { msg(p, "nenhum labirinto por perto"); return; }
            if (a.equals("lab")) {
                msg(p, "labirinto: x=" + l.x0 + " z=" + l.z0 + " y=" + l.floorY + " | sala: x=" + l.roomX() + " z=" + l.roomZ());
            } else if (a.equals("tp")) {
                p.setPositionAndUpdate(l.x0 + 1.5D, l.floorY + 1, l.z0 + 1.5D);
            } else {
                p.setPositionAndUpdate(l.roomX() - 5.5D, l.floorY + 1, l.roomZ() + 0.5D);
            }
        } else {
            msg(p, getCommandUsage(sender));
        }
    }

    private void msg(EntityPlayerMP p, String s) { p.addChatMessage(new TextComponentString(s)); }
}
