package com.watcher;

import java.text.Normalizer;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayerMP;

/** "IA" offline: sempre uma palavra so, sempre parece entender. */
public class Brain {
    private static final Random R = new Random();
    private static final String[] DEFAULT = {"Continue.", "Atrás.", "Observando.", "Olhe.", "Sim.", "Mais.", "Escute.", "Cave.", "Sempre."};
    private static final String[] YESNO = {"Sim.", "Não.", "Talvez.", "Sempre.", "Nunca."};

    public static String reply(EntityPlayerMP p, String raw, String activity) {
        String m = norm(raw);
        String nick = p.getName();
        boolean question = raw.trim().endsWith("?");

        if (has(m, " o que estou fazendo ", " o que eu to fazendo ", " o que eu estou fazendo ", " oq eu to fazendo ",
                " oq estou fazendo ", " what am i doing ", " o que to fazendo ", " oq eu tou fazendo "))
            return cap(activity) + ".";
        if (has(m, " meu nome ", " meu nick ", " quem sou ", " who am i ", " my name ", " como me chamo ", " nick "))
            return nick + ".";
        if (has(m, " quem e voce ", " quem e vc ", " quem es ", " who are you ", " quem ta ai ", " quem e voce ", " quem eh ", " quem e "))
            return "Ninguém.";
        if (has(m, " onde voce ", " onde vc ", " where are you ", " onde ta ", " onde esta "))
            return "Atrás.";
        if (has(m, " onde estou ", " onde eu to ", " onde eu estou ", " where am i "))
            return p.posY < 50 ? "Embaixo." : "Superfície.";
        if (has(m, " altura ", " y "))
            return String.valueOf((int) p.posY) + ".";
        if (has(m, " por que ", " porque ", " pq ", " why ", " por quê "))
            return "Curiosidade.";
        if (has(m, " o que voce quer ", " oq vc quer ", " what do you want ", " o que quer ", " oq quer "))
            return "Você.";
        if (has(m, " skin ", " pele "))
            return "Familiar.";
        if (has(m, " horas ", " hora ", " dia ", " noite ", " time "))
            return p.worldObj.isDaytime() ? "Dia." : "Noite.";
        if (has(m, " vida ", " life ", " health ", " hp "))
            return String.valueOf((int) p.getHealth()) + ".";
        if (has(m, " fome ", " hungry ", " comida "))
            return String.valueOf(p.getFoodStats().getFoodLevel()) + ".";
        if (has(m, "medo", "scared", "afraid", "assust", "susto"))
            return "Correto.";
        if (has(m, " para ", " pare ", " stop ", " sai ", " vai embora ", " leave ", " go away ", " some ", " chega "))
            return "Não.";
        if (has(m, " ajuda ", " help ", " socorro "))
            return "Impossível.";
        if (has(m, " real ", " existe ", " existe mesmo "))
            return "Sim.";
        if (has(m, " matar ", " mata ", " kill ", " morrer ", " die "))
            return "Talvez.";
        if (has(m, "herobrine", " notch ", " criador ", " dev ", " mod "))
            return "Não.";
        if (has(m, " me ve ", " me observa ", " me olha ", " watching ", " me ve "))
            return "Sempre.";
        if (has(m, " sabe ", " know "))
            return "Tudo.";
        if (has(m, " obrigado ", " obrigada ", " valeu ", " thanks ", " thx "))
            return "Sempre.";
        if (has(m, " oi ", " ola ", " eai ", " e ai ", " hello ", " hi ", " hey ", " opa ", " fala ", " salve "))
            return nick + ".";
        if (has(m, "kkk", "haha", " lol ", " rs "))
            return "Pare.";
        if (question)
            return YESNO[R.nextInt(YESNO.length)];
        if (R.nextInt(7) == 0)
            return nick + ".";
        return DEFAULT[R.nextInt(DEFAULT.length)];
    }

    private static boolean has(String m, String... keys) {
        for (String k : keys) if (m.contains(k)) return true;
        return false;
    }

    private static String norm(String s) {
        String n = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.replaceAll("[^a-z0-9]+", " ").trim();
        return " " + n + " ";
    }

    private static String cap(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
