package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.BoolSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

/** ESP via the vanilla glow outline (client-side only). */
public class Glow extends Module {
    private final BoolSetting players  = add(new BoolSetting("Players", true));
    private final BoolSetting crystals = add(new BoolSetting("Crystals", true));

    public Glow() { super("ESP", "Outlines players and crystals through walls", Category.CLIENT); }

    @Override public void onTick() {
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof Player p && p != mc.player) e.setGlowingTag(players.get());
            else if (e instanceof EndCrystal) e.setGlowingTag(crystals.get());
        }
    }

    @Override public void onDisable() {
        if (mc.level == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof Player || e instanceof EndCrystal) e.setGlowingTag(false);
        }
    }
}
