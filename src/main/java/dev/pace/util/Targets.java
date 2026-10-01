package dev.pace.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class Targets {
    private static final Minecraft mc = Minecraft.getInstance();

    public static Player nearest(double range) {
        Player best = null;
        double bestDist = range * range;
        for (Player p : mc.level.players()) {
            if (p == mc.player || p.isDeadOrDying() || p.isSpectator() || Friends.is(p.getName().getString())) continue;
            double d = mc.player.distanceToSqr(p);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }
}
