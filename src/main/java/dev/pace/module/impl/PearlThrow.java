package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.BoolSetting;
import dev.pace.util.Compat;
import dev.pace.util.InvUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

/** Bind a key to this module: pressing it throws an ender pearl and switches back. */
public class PearlThrow extends Module {
    private final BoolSetting switchBack = add(new BoolSetting("Switch Back", true));

    public PearlThrow() { super("PearlThrow", "Keybind: throw an ender pearl instantly", Category.COMBAT); }

    @Override public void toggle() {
        if (mc.screen != null || mc.player == null) return;
        if (mc.player.getOffhandItem().is(Items.ENDER_PEARL)) {
            mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            mc.player.swing(InteractionHand.OFF_HAND);
            return;
        }
        int s = InvUtil.findHotbar(Items.ENDER_PEARL);
        if (s < 0) return;
        var inv = mc.player.getInventory();
        int prev = Compat.slot(inv);
        Compat.setSlot(inv, s);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
        if (switchBack.get()) Compat.setSlot(inv, prev);
    }
    @Override public void setEnabled(boolean v) { /* action module, never stays enabled */ }
}
