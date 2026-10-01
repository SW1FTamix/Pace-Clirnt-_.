package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class Surround extends Module {
    private static final Direction[] DIRS = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
    private final NumSetting perTick   = add(new NumSetting("Blocks Per Tick", 2, 1, 4, 1));
    private final NumSetting delay     = add(new NumSetting("Delay", 0, 0, 10, 1));
    private final BoolSetting onGround = add(new BoolSetting("Only On Ground", true));

    private int timer;

    public Surround() { super("Surround", "Surrounds your feet with obsidian", Category.COMBAT); }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (mc.screen != null) return;
        if (onGround.get() && !mc.player.onGround()) return;
        int slot = InvUtil.findHotbar(Items.OBSIDIAN);
        if (slot < 0) return;

        var inv = mc.player.getInventory();
        int prev = Compat.slot(inv);
        BlockPos feet = mc.player.blockPosition();
        int placed = 0;
        for (Direction d : DIRS) {
            if (placed >= perTick.getInt()) break;
            BlockPos pos = feet.relative(d);
            BlockPos below = pos.below();
            if (!mc.level.getBlockState(pos).canBeReplaced()) continue;
            if (!mc.level.getBlockState(below).isFaceSturdy(mc.level, below, Direction.UP)) continue;
            if (!mc.level.getEntities((Entity) null, new AABB(pos)).isEmpty()) continue;
            Compat.setSlot(inv, slot);
            if (PlaceUtil.interact(below, Direction.UP, InteractionHand.MAIN_HAND)) placed++;
        }
        Compat.setSlot(inv, prev);
        if (placed > 0) timer = delay.getInt();
    }
}
