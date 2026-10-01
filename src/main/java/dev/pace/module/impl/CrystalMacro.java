package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.PlaceUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Crosshair crystal macro: hits the crystal you look at and places on the obsidian you look at. */
public class CrystalMacro extends Module {
    private final BoolSetting hit       = add(new BoolSetting("Hit Crystals", true));
    private final BoolSetting placeC    = add(new BoolSetting("Place Crystals", true));
    private final BoolSetting needUse   = add(new BoolSetting("Require Right Click", true));
    private final NumSetting hitDelay   = add(new NumSetting("Hit Delay", 1, 0, 10, 1));
    private final NumSetting placeDelay = add(new NumSetting("Place Delay", 1, 0, 10, 1));

    private int hitT, placeT;

    public CrystalMacro() { super("CrystalMacro", "Auto hit/place crystals under your crosshair", Category.COMBAT); }

    @Override public void onTick() {
        if (mc.screen != null) return;
        if (hitT > 0) hitT--;
        if (placeT > 0) placeT--;
        boolean use = !needUse.get() || mc.options.keyUse.isDown();
        HitResult hr = mc.hitResult;
        if (hr == null) return;

        if (hit.get() && use && hitT == 0 && hr instanceof EntityHitResult ehr && ehr.getEntity() instanceof EndCrystal c) {
            mc.gameMode.attack(mc.player, c);
            mc.player.swing(InteractionHand.MAIN_HAND);
            hitT = hitDelay.getInt();
            return;
        }
        if (placeC.get() && use && placeT == 0 && hr instanceof BlockHitResult bhr && hr.getType() == HitResult.Type.BLOCK) {
            var state = mc.level.getBlockState(bhr.getBlockPos());
            if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) return;
            InteractionHand hand = mc.player.getOffhandItem().is(Items.END_CRYSTAL) ? InteractionHand.OFF_HAND
                    : mc.player.getMainHandItem().is(Items.END_CRYSTAL) ? InteractionHand.MAIN_HAND : null;
            if (hand == null) return;
            var up = bhr.getBlockPos().above();
            if (!mc.level.getBlockState(up).isAir()) return;
            if (!mc.level.getEntities((Entity) null, new AABB(up).expandTowards(0, 1, 0)).isEmpty()) return;
            if (PlaceUtil.interact(bhr.getBlockPos(), bhr.getDirection(), hand)) placeT = placeDelay.getInt();
        }
    }
}
