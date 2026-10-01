package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Full anchor aura: places an anchor near the target, charges it, then detonates it. */
public class AutoAnchor extends Module {
    private final NumSetting targetRange = add(new NumSetting("Target Range", 8, 2, 20, 1));
    private final NumSetting placeRange  = add(new NumSetting("Place Range", 4.5, 1, 6, 0.5));
    private final NumSetting delay       = add(new NumSetting("Delay", 2, 0, 20, 1));
    private final NumSetting minDamage   = add(new NumSetting("Min Damage", 6, 0, 36, 0.5));
    private final NumSetting maxSelf     = add(new NumSetting("Max Self Damage", 4, 0, 36, 0.5));
    private final NumSetting charges     = add(new NumSetting("Charges", 1, 1, 4, 1));
    private final NumSetting explodeSlot = add(new NumSetting("Explode Slot", 1, 1, 9, 1));
    private final BoolSetting antiSuicide= add(new BoolSetting("Anti Suicide", true));

    private BlockPos anchor;
    private int timer;

    public AutoAnchor() { super("AutoAnchor", "Places, charges and explodes respawn anchors on the nearest player", Category.COMBAT); }

    @Override public void onDisable() { anchor = null; }

    @Override public void onTick() {
        if (mc.screen != null) return;
        if (timer > 0) { timer--; return; }
        if (mc.level.dimension() == Level.NETHER) return;

        Player target = Targets.nearest(targetRange.get());
        if (target == null) { anchor = null; return; }
        Vec3 eye = mc.player.getEyePosition();
        double r = placeRange.get();
        var inv = mc.player.getInventory();
        int prev = Compat.slot(inv);

        if (anchor != null) {
            var st = mc.level.getBlockState(anchor);
            if (!st.is(Blocks.RESPAWN_ANCHOR) || eye.distanceToSqr(Vec3.atCenterOf(anchor)) > r * r) { anchor = null; return; }
            int charge = st.getValue(RespawnAnchorBlock.CHARGE);
            if (charge < charges.getInt()) {
                int g = InvUtil.findHotbar(Items.GLOWSTONE);
                if (g < 0) return;
                Compat.setSlot(inv, g);
                PlaceUtil.interact(anchor, Direction.UP, InteractionHand.MAIN_HAND);
            } else {
                float self = DamageUtil.explosion(Vec3.atCenterOf(anchor), mc.player, 5f);
                if (!DamageUtil.safe(self, maxSelf.get(), antiSuicide.get())) return;
                Compat.setSlot(inv, explodeSlot.getInt() - 1);
                PlaceUtil.interact(anchor, Direction.UP, InteractionHand.MAIN_HAND);
            }
            Compat.setSlot(inv, prev);
            timer = delay.getInt();
            return;
        }

        int a = InvUtil.findHotbar(Items.RESPAWN_ANCHOR);
        if (a < 0 || InvUtil.findHotbar(Items.GLOWSTONE) < 0) return;

        BlockPos tb = target.blockPosition();
        BlockPos best = null;
        float bestScore = -999;
        for (BlockPos bp : BlockPos.betweenClosed(tb.offset(-2, -1, -2), tb.offset(2, 2, 2))) {
            if (!mc.level.getBlockState(bp).canBeReplaced()) continue;
            BlockPos below = bp.below();
            if (!mc.level.getBlockState(below).isFaceSturdy(mc.level, below, Direction.UP)) continue;
            Vec3 c = Vec3.atCenterOf(bp);
            if (eye.distanceToSqr(c) > r * r) continue;
            if (!mc.level.getEntities((Entity) null, new AABB(bp)).isEmpty()) continue;
            float dmg = DamageUtil.explosion(c, target, 5f);
            float self = DamageUtil.explosion(c, mc.player, 5f);
            if (dmg < minDamage.get() || !DamageUtil.safe(self, maxSelf.get(), antiSuicide.get())) continue;
            float score = dmg - self * 0.5f;
            if (score > bestScore) { bestScore = score; best = bp.immutable(); }
        }
        if (best != null) {
            Compat.setSlot(inv, a);
            if (PlaceUtil.interact(best.below(), Direction.UP, InteractionHand.MAIN_HAND)) anchor = best;
            Compat.setSlot(inv, prev);
            timer = delay.getInt();
        }
    }
}
