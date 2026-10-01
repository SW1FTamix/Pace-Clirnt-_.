package dev.pace.module.impl;

import dev.pace.module.Category;
import dev.pace.module.Module;
import dev.pace.setting.*;
import dev.pace.util.Compat;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

/** Hover a totem in any inventory screen and it is instantly moved to your offhand / chosen hotbar slot. */
public class HoverTotem extends Module {
    private final BoolSetting offhand   = add(new BoolSetting("Offhand", true));
    private final NumSetting hotbarSlot = add(new NumSetting("Hotbar Slot", 9, 0, 9, 1)); // 0 = off
    private final NumSetting delay      = add(new NumSetting("Delay", 0, 0, 10, 1));

    private int timer;

    public HoverTotem() { super("HoverTotem", "Hover a totem to move it to offhand/hotbar", Category.COMBAT); }

    @Override public void onTick() {
        if (timer > 0) { timer--; return; }
        if (!(mc.screen instanceof AbstractContainerScreen<?> scr)) return;
        Slot slot = scr.getSlotUnderMouse();
        if (slot == null || !slot.getItem().is(Items.TOTEM_OF_UNDYING)) return;
        if (slot.container != mc.player.getInventory() || slot.getContainerSlot() >= 36) return;

        var menu = scr.getMenu();
        int idx = menu.slots.indexOf(slot);
        if (idx < 0) return;

        if (offhand.get() && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            Compat.click(menu.containerId, idx, 40, ClickType.SWAP);
            timer = delay.getInt();
            return;
        }
        int hs = hotbarSlot.getInt() - 1;
        if (hs >= 0 && slot.getContainerSlot() != hs && !mc.player.getInventory().getItem(hs).is(Items.TOTEM_OF_UNDYING)) {
            Compat.click(menu.containerId, idx, hs, ClickType.SWAP);
            timer = delay.getInt();
        }
    }
}
