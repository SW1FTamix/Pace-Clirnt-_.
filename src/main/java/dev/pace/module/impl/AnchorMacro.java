package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Look at a respawn anchor: charges it with glowstone, then swaps to your explode slot and detonates it. */
public class AnchorMacro extends Module {
    private final BoolSetting needUse   = add(new BoolSetting("Require Right Click", true));
    private final NumSetting delay      = add(new NumSetting("Delay", 1, 0, 10, 1));
    private final NumSetting charges    = add(new NumSetting("Charges", 1, 1, 4, 1));
    private final NumSetting explodeSlot= add(new NumSetting("Explode Slot", 1, 1, 9, 1));
    private final BoolSetting switchBack= add(new BoolSetting("Switch Back", true));

    private int timer;

    public AnchorMacro() { super("AnchorMacro", "Auto charge and explode the anchor you look at", Category.COMBAT); }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (mc.screen != null) return;
        if (needUse.get() && !mc.options.keyUse.isDown()) return;
        if (!(mc.hitResult instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;
        var pos = bhr.getBlockPos();
        var state = mc.level.getBlockState(pos);
        if (!state.is(Blocks.RESPAWN_ANCHOR)) return;

        var inv = mc.player.getInventory();
        int prev = Compat.slot(inv);
        int charge = state.getValue(RespawnAnchorBlock.CHARGE);

        if (charge < charges.getInt()) {
            int g = InvUtil.findHotbar(Items.GLOWSTONE);
            if (g < 0) return;
            Compat.setSlot(inv, g);
            PlaceUtil.interact(pos, bhr.getDirection(), InteractionHand.MAIN_HAND);
        } else {
            if (mc.level.dimension() == Level.NETHER) return; // anchors only explode outside the Nether
            Compat.setSlot(inv, explodeSlot.getInt() - 1);
            PlaceUtil.interact(pos, bhr.getDirection(), InteractionHand.MAIN_HAND);
            if (!switchBack.get()) return;
        }
        if (switchBack.get() && charge >= charges.getInt()) Compat.setSlot(inv, prev);
        timer = delay.getInt();
    }
}
