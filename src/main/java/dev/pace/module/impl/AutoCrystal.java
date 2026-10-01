package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AutoCrystal extends Module {
    private final BoolSetting place        = add(new BoolSetting("Place", true));
    private final BoolSetting breakC       = add(new BoolSetting("Break", true));
    private final NumSetting targetRange   = add(new NumSetting("Target Range", 10, 2, 20, 1));
    private final NumSetting placeRange    = add(new NumSetting("Place Range", 4.5, 1, 6, 0.5));
    private final NumSetting breakRange    = add(new NumSetting("Break Range", 3.0, 1, 6, 0.5));
    private final NumSetting placeDelay    = add(new NumSetting("Place Delay", 2, 0, 20, 1));
    private final NumSetting breakDelay    = add(new NumSetting("Break Delay", 1, 0, 20, 1));
    private final NumSetting minDamage     = add(new NumSetting("Min Damage", 4, 0, 36, 0.5));
    private final NumSetting maxSelf       = add(new NumSetting("Max Self Damage", 6, 0, 36, 0.5));
    private final BoolSetting antiSuicide  = add(new BoolSetting("Anti Suicide", true));
    private final ModeSetting switchMode   = add(new ModeSetting("Switch", "Off", "Normal", "Silent"));

    private int placeT, breakT;

    public AutoCrystal() { super("AutoCrystal", "Places and breaks end crystals around the nearest player", Category.COMBAT); }

    @Override public void onTick() {
        if (mc.screen != null) return;
        if (placeT > 0) placeT--;
        if (breakT > 0) breakT--;

        Player target = Targets.nearest(targetRange.get());
        if (target == null) return;

        if (breakC.get() && breakT == 0) {
            EndCrystal c = bestBreak(target);
            if (c != null) {
                mc.gameMode.attack(mc.player, c);
                mc.player.swing(InteractionHand.MAIN_HAND);
                breakT = breakDelay.getInt();
            }
        }
        if (place.get() && placeT == 0) {
            BlockPos pos = bestPlace(target);
            if (pos != null && placeCrystal(pos)) placeT = placeDelay.getInt();
        }
    }

    private EndCrystal bestBreak(Player target) {
        double r = breakRange.get();
        Vec3 eye = mc.player.getEyePosition();
        EndCrystal best = null;
        float bestDmg = -1;
        for (EndCrystal c : mc.level.getEntitiesOfClass(EndCrystal.class, mc.player.getBoundingBox().inflate(r + 1))) {
            if (c.getBoundingBox().distanceToSqr(eye) > r * r) continue;
            Vec3 pos = c.position();
            float self = DamageUtil.explosion(pos, mc.player, 6f);
            if (!DamageUtil.safe(self, maxSelf.get(), antiSuicide.get())) continue;
            float dmg = DamageUtil.explosion(pos, target, 6f);
            if (dmg < minDamage.get()) continue;
            if (dmg > bestDmg) { bestDmg = dmg; best = c; }
        }
        return best;
    }

    private BlockPos bestPlace(Player target) {
        double r = placeRange.get();
        Vec3 eye = mc.player.getEyePosition();
        BlockPos base = mc.player.blockPosition();
        int ri = (int) Math.ceil(r);
        BlockPos best = null;
        float bestScore = -999;
        for (BlockPos bp : BlockPos.betweenClosed(base.offset(-ri, -ri, -ri), base.offset(ri, ri, ri))) {
            BlockState s = mc.level.getBlockState(bp);
            if (!s.is(Blocks.OBSIDIAN) && !s.is(Blocks.BEDROCK)) continue;
            BlockPos up = bp.above();
            if (!mc.level.getBlockState(up).isAir()) continue;
            if (eye.distanceToSqr(Vec3.atCenterOf(bp)) > r * r) continue;
            if (!mc.level.getEntities((Entity) null, new AABB(up).expandTowards(0, 1, 0)).isEmpty()) continue;
            Vec3 center = new Vec3(bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5);
            float self = DamageUtil.explosion(center, mc.player, 6f);
            if (!DamageUtil.safe(self, maxSelf.get(), antiSuicide.get())) continue;
            float dmg = DamageUtil.explosion(center, target, 6f);
            if (dmg < minDamage.get()) continue;
            float score = dmg - self * 0.5f;
            if (score > bestScore) { bestScore = score; best = bp.immutable(); }
        }
        return best;
    }

    private boolean placeCrystal(BlockPos pos) {
        var inv = mc.player.getInventory();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        int swapTo = -1;
        if (mc.player.getOffhandItem().is(Items.END_CRYSTAL)) hand = InteractionHand.OFF_HAND;
        else if (!mc.player.getMainHandItem().is(Items.END_CRYSTAL)) {
            if (switchMode.is("Off")) return false;
            swapTo = InvUtil.findHotbar(Items.END_CRYSTAL);
            if (swapTo < 0) return false;
        }
        int prev = Compat.slot(inv);
        if (swapTo >= 0) Compat.setSlot(inv, swapTo);
        boolean ok = PlaceUtil.interact(pos, Direction.UP, hand);
        if (swapTo >= 0 && switchMode.is("Silent")) Compat.setSlot(inv, prev);
        return ok;
    }
}
